package info.dgjones.au.nlp

/* Implementation of the STANS stemmer based on:
 http://docsdrive.com/pdfs/ansinet/itj/2006/685-688.pdf
 */

class StansStemmer {
    val steps = listOf(
        StemmingStep(1, listOf(
            StemmingRule("US", "US"),
            StemmingRule("CEED", "CESS"),
            StemmingRule("EED", "EED"),
            StemmingRule("IES", "Y"),
            StemmingRule("IED", "Y"),
            StemmingRule("ING", "E"), // FIXME (*V*)ING
            StemmingRule("ED", "E")
        )),
        StemmingStep(2, listOf(
            StemmingRule("ANCY", "ANCE"),
            StemmingRule("ENCY", "ENCY"),
            StemmingRule("ABLY", "ABLY"),
            StemmingRule("OUSLY", "OUS"),
            StemmingRule("ALITY", "AL"),
            StemmingRule("IVITY", "IVE"),
            StemmingRule("BILITY", "BLE"),
            StemmingRule("FULLY", "FUL"),
            StemmingRule("FUL", ""),
            StemmingRule("LESSLY", "LESS"),
            StemmingRule("BLY", "BLE")
        )),
        StemmingStep(3, listOf(
            StemmingRule("LESS", ""),
            StemmingRule("ICITY", "IC")
        )),
        StemmingStep(4, listOf(
            StemmingRule("AL", "E"),
            StemmingRule("IABLE", "Y"),
            StemmingRule("SCOPIC", "SCOPE"),
            StemmingRule("FYE", "FY"),
            StemmingRule("ALLY", "AL"),
            StemmingRule("TLY", "T")
        ))
    )

    fun stemWord(word: String): String {
        return steps.fold(word) { current, step -> step.run(current)}
    }
}

class StemmingStep(val step: Int, val rules: List<StemmingRule>) {
    fun run(input: String): String {
        rules.forEach {
            val output = it.run(input)
            if (output != input) {
                println("applied.$step ${it.from}->${it.to} for $input->$output")
                return output
            }
        }
        return input
    }
}
class StemmingRule(val from: String, val to: String) {
    fun run(input: String): String {
        return if (input.endsWith(from, ignoreCase = true)) {
            input.substring(0, input.length - from.length) + to.toLowerCase()
        } else {
            input
        }
    }
}



