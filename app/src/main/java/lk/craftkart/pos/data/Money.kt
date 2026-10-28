package lk.craftkart.pos.data

object Money {
    fun format(cents: Long): String {
        val neg = cents < 0
        val c = kotlin.math.abs(cents)
        val rupees = c / 100
        val paisa = c % 100
        val grouped = rupees.toString().reversed().chunked(3).joinToString(",").reversed()
        return buildString {
            if (neg) append("-")
            append("Rs ")
            append(grouped)
            append(".")
            append("%02d".format(paisa))
        }
    }

    fun parseToCents(input: String): Long? {
        val cleaned = input.replace(",", "").replace(" ", "")
        if (cleaned.isEmpty()) return null
        return try {
            val d = cleaned.toDouble()
            if (d < 0) null else Math.round(d * 100).toLong()
        } catch (e: NumberFormatException) {
            null
        }
    }
}
