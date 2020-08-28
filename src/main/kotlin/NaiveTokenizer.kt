class NaiveTokenizer() {
    val token = mutableListOf<String>()

    val abbreviations = setOf("dr", "mr", "mrs",
    "ave", "blvd", "cyn", "dr", "ln", "rd", "st",
    "no", "tel", "temp", "vet", "vs", "misc", "min", "max", "est", "dept",
    "apt", "appt", "approx")

    fun tokenize(text: String):List<String> {
        val units = text.split("""(?U)\s+""".toRegex())
        val words = mutableListOf<String>()
        units.forEach {
            if (it.endsWith(",")) {
                words.add(it.substringBeforeLast(','))
                words.add(",")
            } else {
                words.add(it)
            }
        }
        val words2 = mutableListOf<String>()
        words.forEach {
            if (it.endsWith(".") && !isAbbrevation(it)) {
                words2.add(it.substringBeforeLast('.'))
                words2.add(".")
            } else {
                words2.add(it)
            }
        }
        return words2.map { it.trim() }.filter { !it.isEmpty() }
    }

    fun isAbbrevation(unit: String): Boolean {
        val unitWithoutPeriod = unit.substringBeforeLast(".").toLowerCase()
        return abbreviations.contains(unitWithoutPeriod)
    }
}