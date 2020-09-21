package info.dgjones.au.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WordMorphologyBuilderTest {
    @Nested
    inner class SuffixEd {
        @Test
        fun edForBasicWords() {
            assertEquals("helped", WordMorphologyBuilder("help").suffixEd()?.full)
        }

        @Test
        fun edForWordsEndingInE() {
            assertEquals("baked", WordMorphologyBuilder("bake").suffixEd()?.full)
            assertEquals("dyed", WordMorphologyBuilder("dye").suffixEd()?.full)
        }

        @Test
        fun edForWordsEndingInConsonantY() {
            assertEquals("bullied", WordMorphologyBuilder("bully").suffixEd()?.full)
            assertEquals("cried", WordMorphologyBuilder("cry").suffixEd()?.full)
        }

        @Test
        fun edForWordsEndingInSingleVowelConsonantExceptY() {
            assertEquals("admitted", WordMorphologyBuilder("admit").suffixEd()?.full)
            assertEquals("begged", WordMorphologyBuilder("beg").suffixEd()?.full)
        }
    }
    @Nested
    inner class SuffixIng {
        @Test
        fun ingForBasicWords() {
            assertEquals("saying", WordMorphologyBuilder("say").suffixIng()?.full)
            assertEquals("staying", WordMorphologyBuilder("stay").suffixIng()?.full)
            assertEquals("buying", WordMorphologyBuilder("buy").suffixIng()?.full)
        }
        @Test
        fun ingForBasicWordsEndingE() {
            assertEquals("seeing", WordMorphologyBuilder("see").suffixIng()?.full)
            assertEquals("pulling", WordMorphologyBuilder("pull").suffixIng()?.full)
        }
        @Test
        fun ingForWordsEndingInSilentE() {
            assertEquals("riding", WordMorphologyBuilder("ride").suffixIng()?.full)
        }
        @Test
        fun ingForWordsWithSilentVowel() {
            assertEquals("shutting", WordMorphologyBuilder("shut").suffixIng()?.full)
            assertEquals("running", WordMorphologyBuilder("run").suffixIng()?.full)
        }
    }
    @Nested
    inner class SuffixLy {
        @Test
        fun lyForWordsEndingInConsonant() {
            assertEquals("slowly", WordMorphologyBuilder("slow").suffixLy()?.full)
            assertEquals("quickly", WordMorphologyBuilder("quick").suffixLy()?.full)
            assertEquals("friendly", WordMorphologyBuilder("friend").suffixLy()?.full)
        }
        @Test
        fun lyForWordsEndingInFul() {
            assertEquals("carefully", WordMorphologyBuilder("careful").suffixLy()?.full)
            assertEquals("successfully", WordMorphologyBuilder("successful").suffixLy()?.full)
        }
        @Test
        fun lyForWordsEndingInE() {
            assertEquals("lovely", WordMorphologyBuilder("love").suffixLy()?.full)
            assertEquals("immediately", WordMorphologyBuilder("immediate").suffixLy()?.full)
        }
        @Test
        fun lyForWordsEndingInESpecialCases() {
            assertEquals("truly", WordMorphologyBuilder("true").suffixLy()?.full)
            assertEquals("duly", WordMorphologyBuilder("due").suffixLy()?.full)
            assertEquals("wholly", WordMorphologyBuilder("whole").suffixLy()?.full)
        }
        @Test
        fun lyForWordsEndingInConsonantLE() {
            assertEquals("gently", WordMorphologyBuilder("gentle").suffixLy()?.full)
            assertEquals("simply", WordMorphologyBuilder("simple").suffixLy()?.full)
            assertEquals("terribly", WordMorphologyBuilder("terrible").suffixLy()?.full)
            assertEquals("wrinkly", WordMorphologyBuilder("wrinkle").suffixLy()?.full)
            assertEquals("incredibly", WordMorphologyBuilder("incredible").suffixLy()?.full)
        }
        @Test
        fun lyForWordsEndingInYFor2syllables() {
            assertEquals("easily", WordMorphologyBuilder("easy").suffixLy()?.full)
            assertEquals("necessarily", WordMorphologyBuilder("necessary").suffixLy()?.full)
        }
        @Test
        fun lyForWordsEndingInYFor1syllable() {
            assertEquals("shyly", WordMorphologyBuilder("shy").suffixLy()?.full)
            assertEquals("slyly", WordMorphologyBuilder("sly").suffixLy()?.full)
            assertEquals("coyly", WordMorphologyBuilder("coy").suffixLy()?.full)
        }
        @Test
        fun lyForWordsEndingInYSpecialCases() {
            assertEquals("daily", WordMorphologyBuilder("day").suffixLy()?.full)
            assertEquals("gaily", WordMorphologyBuilder("gay").suffixLy()?.full)
        }
    }
    @Nested
    inner class SuffixS {
        @Test
        fun sForGeneralWords() {
            assertEquals("scoops", WordMorphologyBuilder("scoop").suffixS()?.full)
            assertEquals("cones", WordMorphologyBuilder("cone").suffixS()?.full)
            assertEquals("measures", WordMorphologyBuilder("measure").suffixS()?.full)
        }
        @Test
        fun sForWordsEndingInIZSound() {
            assertEquals("dishes", WordMorphologyBuilder("dish").suffixS()?.full)
            assertEquals("dresses", WordMorphologyBuilder("dress").suffixS()?.full)
            assertEquals("boxes", WordMorphologyBuilder("box").suffixS()?.full)
            assertEquals("branches", WordMorphologyBuilder("branch").suffixS()?.full)
        }
        @Test
        fun sForWordsEndingInConsonantY() {
            assertEquals("cherries", WordMorphologyBuilder("cherry").suffixS()?.full)
            assertEquals("puppies", WordMorphologyBuilder("puppy").suffixS()?.full)
        }
        @Test
        fun sForWordsEndingInVowelY() {
            assertEquals("days", WordMorphologyBuilder("day").suffixS()?.full)
            assertEquals("monkeys", WordMorphologyBuilder("monkey").suffixS()?.full)
        }
        @Test
        fun sForWordsEndingInVowelO() {
            assertEquals("pistachios", WordMorphologyBuilder("pistachio").suffixS()?.full)
            assertEquals("stereos", WordMorphologyBuilder("stereo").suffixS()?.full)
        }
        @Test
        fun sForWordsEndingInConsonantO() {
            //assertEquals("heroes", WordMorphologyBuilder("hero").suffixS()?.full)
            assertEquals("pianos", WordMorphologyBuilder("piano").suffixS()?.full)
        }
        @Test
        fun sForWordsEndingInF() {
            assertEquals("wives", WordMorphologyBuilder("wife").suffixS()?.full)
            assertEquals("knives", WordMorphologyBuilder("knife").suffixS()?.full)
            assertEquals("loaves", WordMorphologyBuilder("loaf").suffixS()?.full)
        }
        @Test
        fun sForWordsEndingInFF() {
            assertEquals("cliffs", WordMorphologyBuilder("cliff").suffixS()?.full)
            assertEquals("puffs", WordMorphologyBuilder("puff").suffixS()?.full)
        }
        @Test
        fun sForIrregular() {
            assertEquals("children", WordMorphologyBuilder("child").suffixS()?.full)
            assertEquals("people", WordMorphologyBuilder("person").suffixS()?.full)
            assertEquals("men", WordMorphologyBuilder("man").suffixS()?.full)
            assertEquals("women", WordMorphologyBuilder("woman").suffixS()?.full)
            assertEquals("teeth", WordMorphologyBuilder("tooth").suffixS()?.full)
            assertEquals("feet", WordMorphologyBuilder("foot").suffixS()?.full)
            assertEquals("mice", WordMorphologyBuilder("mouse").suffixS()?.full)
            assertEquals("geese", WordMorphologyBuilder("goose").suffixS()?.full)
            assertEquals("oxen", WordMorphologyBuilder("ox").suffixS()?.full)
        }
        @Test
        fun sForSame() {
            assertEquals("deer", WordMorphologyBuilder("deer").suffixS()?.full)
            assertEquals("sheep", WordMorphologyBuilder("sheep").suffixS()?.full)
            assertEquals("means", WordMorphologyBuilder("means").suffixS()?.full)
            assertEquals("species", WordMorphologyBuilder("species").suffixS()?.full)
            assertEquals("series", WordMorphologyBuilder("series").suffixS()?.full)
            assertEquals("ice", WordMorphologyBuilder("ice").suffixS()?.full)
        }

    }
}