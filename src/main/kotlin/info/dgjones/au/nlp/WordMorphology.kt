package info.dgjones.au.nlp

class WordMorphologyBuilder(val root: String) {
    companion object {
        val VOWELS = setOf('a', 'e', 'i', 'o', 'u')
    }
    fun build(): List<WordMorphology> {
        return listOfNotNull(
            suffixEd(),
            suffixIng(),
            suffixLy(),
            suffixS()
        )
    }

    //     Used to form past tenses of (regular) verbs. In linguistics, it is used for the base form of any past form. See -t for a variant.
    //
    //        pointed (as in He pointed at the dog.)
    //
    //     Used to form past participles of (regular) verbs. See -en and -t for variants.
    //
    //        pointed (as in He has pointed at the dog.)
    //
    // Used to form adjectives from nouns, in the sense of having the object represented by the noun.
    //
    //    pointed (as in A needle has a pointed end. - the end of a needle has a point.)
    //    horned (as in a horned antelope - an antelope possessing horns)
    //    Antonym: -less
    //
    // As an extension of the above, when used along with an adjective preceding the noun, describes something that has an object of a particular quality.
    //
    //    red-haired (having red hair)
    //    left-handed (having a left hand as more dexterous hand)
    fun suffixEd(): WordMorphology? {
        var full = root + "ed"
        if (root.length >= 3 && root.endsWith("e")) {
            full = root + "d"
        } else if (root.length >= 3 && isVowel(root[root.length-2]) && isConsonant(root.last()) && root.last() != 'y') {
            full = root + root.last() + "ed"
        } else if (root.length >= 3 && isConsonant(root[root.length-2]) && root.last()== 'y') {
            full = root.dropLast(1) + "ied"
        }
        suffixEdIrregular[root]?.let {
            full = it
        }
        return WordMorphology(root, "ed", full)
    }

    private val suffixEdIrregular = mapOf(
        "have" to "had"
    )

    // 1.Used to form gerunds, a type of verbal nouns, from verbs.
    //        [ forging Trying]
    // 2. Used to form uncountable nouns from verbs denoting the act of doing something, an action.
    //
    //        A making of a film; The forging of the sword took several hours of planning, preparation, and metalwork
    //
    // 3. Used to form uncountable nouns from various parts of speech denoting materials or systems of objects considered collectively.
    //        Roofing is a material that covers a roof.
    //        Piping is a system of pipes considered collectively.
    // 1. Used to form present participles of verbs.
    //
    //    Rolling stones gather no moss.
    //    You are making a mess.
    // https://en.wiktionary.org/wiki/-ing#Etymology_1
    fun suffixIng(): WordMorphology? {
        var full = root + "ing"
        if (root.length >= 3 && isConsonant(root[root.length-2]) && root.last() == 'e') {
            full = root.dropLast(1) + "ing"
        } else if (root.length >= 3
            && isConsonant(root[root.length-3])
            && isVowel(root[root.length-2])
            && isConsonant(root.last())
            && root.last() != 'y' /* to not match "say" */) {
            full = root + root.last() + "ing"
        }

        return WordMorphology(root, "ing", full)
    }

    // 1. Used to form adjectives from nouns, the adjectives having the sense of "like or characteristic of what is
    //      denoted by the noun".
    //      [friendly, bloomly]
    // 2. Used to form adjectives from nouns specifying time intervals, the adjectives having the sense of "occurring at
    //      such intervals".
    //      [monthly, daily]
    // 3. Used to form adverbs from adjectives.
    //      [suddenly]
    // https://en.wiktionary.org/wiki/-ly
    fun suffixLy(): WordMorphology? {
        var full = root + "ly"
        if (root.endsWith("ble") || root.endsWith("ple") || root.endsWith("tle") || root.endsWith("gle") || root.endsWith("dle") || root.endsWith("kle")) {
            full = root.dropLast(1) + "y"
        } else if (root.last() == 'y' && root.length >= 4 /* hack for 2+ syllables */) {
            full = root.dropLast(1) + "ily"
        }
        // Special cases
        when (root) {
            "true" -> {
                full = "truly"
            }
            "due" -> {
                full = "duly"
            }
            "whole" -> {
                full = "wholly"
            }
            "day" -> {
                full = "daily"
            }
            "gay" -> {
                full = "gaily"
            }
        }
        return WordMorphology(root, "ly", full)
    }

    fun suffixS(): WordMorphology? {
        var full = root + "s"
        if (root.endsWith("s", ignoreCase = true) || root.endsWith("x", ignoreCase = true)
            || root.endsWith("sh", ignoreCase = true) || root.endsWith("ch", ignoreCase = true)) {
            full = root + "es"
        } else if (root.length >= 3 && isConsonant(root[root.length-2]) && root.last() == 'y') {
            full = root.dropLast(1)  + "ies"
        } else if (root.length >= 3 && isVowel(root[root.length-2]) && root.last() == 'y') {
            full = root  + "s"
        } else if (root.length >= 3 && root.endsWith("fe", ignoreCase = true)) {
            full = root.dropLast(2) + "ves"
        } else if (root.length >= 3 && (root[root.length - 2] == 'f').not() && root.endsWith("f", ignoreCase = true)) {
            full = root.dropLast(1) + "ves"
        }
        suffixSIrregular[root]?.let {
            full = it
        }
        // FIXME case insensitive
        if (suffixSIdem.contains(root)) {
            full = root
        }
        return WordMorphology(root, "s", full)
    }

    private val suffixSIrregular = mapOf(
        "child" to "children",
        "person" to "people",
        "man" to "men",
        "woman" to "women",
        "tooth" to "teeth",
        "foot" to "feet",
        "mouse" to "mice",
        "goose" to "geese",
        "ox" to "oxen"
    )
    private val suffixSIdem = setOf(
        "deer",
        "sheep",
        "fish",
        "means",
        "species",
        "series",
        "ice"
    )

    private fun isVowel(c: Char): Boolean = VOWELS.contains(c.toLowerCase())
    private fun isConsonant(c: Char): Boolean = !isVowel(c)
}

data class WordMorphology(val root: String, val suffix: String, val full: String) {
    constructor(word: String): this(word, "", word)
}