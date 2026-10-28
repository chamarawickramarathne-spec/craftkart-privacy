package lk.craftkart.pos.data

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import lk.craftkart.pos.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ReceiptConfig(
    val businessName: String,
    val contact: String,
    val thanks: String
)

object ReceiptGenerator {
    private val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")

    fun generate(context: Context, sale: SaleWithItems, config: ReceiptConfig): File? {
        return try {
            val dir = File(context.filesDir, "receipts").apply { mkdirs() }
            val file = File(dir, "receipt_${sale.sale.id}.pdf")
            val doc = PdfDocument()
            val page = doc.startPage(PdfDocument.PageInfo.Builder(340, 540, 1).create())
            val canvas = page.canvas

            val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                color = 0xFF3A2E26.toInt()
                textSize = 20f
            }
            val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                color = 0xFF3A2E26.toInt()
                textSize = 13f
            }
            val small = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                color = 0xFF8A7A6C.toInt()
                textSize = 11f
            }
            val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                color = 0xFFA6533B.toInt()
                textSize = 22f
            }
            val line = Paint().apply {
                color = 0xFFE3D5C6.toInt()
                strokeWidth = 1.5f
            }
            val margin = 22f
            val width = 340f - margin * 2

            var y = 40f

            fun center(text: String, paint: Paint) {
                canvas.drawText(text, (340f - paint.measureText(text)) / 2f, y, paint)
                y += paint.textSize + 6f
            }

            center(config.businessName.ifBlank { "CraftKart" }, bold)
            center("RECEIPT", small)
            val invoice = sale.sale.invoiceNo?.ifBlank { null } ?: "Ref #${sale.sale.id}"
            y += 2
            center("Invoice: $invoice", small)
            center(dateFmt.format(Instant.ofEpochMilli(sale.sale.createdAt).atZone(ZoneId.systemDefault())), small)
            center("Payment: " + sale.sale.paymentMethod.replaceFirstChar { it.uppercase() }, small)
            y += 4

            canvas.drawLine(margin, y, 340f - margin, y, line)
            y += 10

            fun textLeft(text: String, paint: Paint) {
                canvas.drawText(text, margin, y, paint)
            }

            fun textRight(text: String, paint: Paint) {
                canvas.drawText(text, 340f - margin - paint.measureText(text), y, paint)
            }

            fun drawLine(label: String, value: String, labelPaint: Paint, valuePaint: Paint) {
                textLeft(label, labelPaint)
                textRight(value, valuePaint)
                y += 20f
            }

            sale.items.forEach { line ->
                textLeft(line.itemName, normal)
                y += 16f
                val detail = "  ${Money.format(line.unitPriceCents)} x ${line.quantity}"
                textLeft(detail, small)
                textRight(Money.format(line.subtotalCents), normal)
                y += 20f
            }

            y += 4
            canvas.drawLine(margin, y, 340f - margin, y, line)
            y += 10

            drawLine("Subtotal", Money.format(sale.items.sumOf { it.subtotalCents }), normal, normal)
            if (sale.sale.discountAmountCents > 0) {
                drawLine("Discount", "-${Money.format(sale.sale.discountAmountCents)}", normal, normal)
            }
            if (sale.sale.taxAmountCents > 0) {
                drawLine("Tax", Money.format(sale.sale.taxAmountCents), normal, normal)
            }
            drawLine("TOTAL", Money.format(sale.sale.totalAmountCents), bold, accent)

            y += 4
            canvas.drawLine(margin, y, 340f - margin, y, line)
            y += 12

            if (config.contact.isNotBlank()) {
                textLeft(config.contact, small)
                y += 16f
            }
            y += 4
            for (l in wrap(config.thanks, normal, width)) {
                center(l, small)
            }

            doc.finishPage(page)
            file.outputStream().use { doc.writeTo(it) }
            doc.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_receipt)))
    }

    private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        words.forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }
}