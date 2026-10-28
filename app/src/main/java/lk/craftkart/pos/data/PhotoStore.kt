package lk.craftkart.pos.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object PhotoStore {
    fun save(context: Context, uri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "item_images").apply { mkdirs() }
            val out = File(dir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            out.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun delete(context: Context, path: String?) {
        if (path == null) return
        try {
            val file = File(path)
            if (file.exists()) file.delete()
        } catch (_: Exception) {
        }
    }
}
