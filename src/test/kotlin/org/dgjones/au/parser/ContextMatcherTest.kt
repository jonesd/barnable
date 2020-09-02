package org.dgjones.au.parser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MatchConceptByHeadTest {
    @Test
    fun `Can match by head name`() {
        assertTrue(matchConceptByHead("testHead")(Concept("testHead")))
    }
    @Test
    fun `Match by head needs full match`() {
        assertFalse(matchConceptByHead("testHead")(Concept("otherHead")))
    }
    @Test
    fun `Only matches on head`() {
        assertFalse(matchConceptByHead("testHead")(Concept("otherHead").value("testHead", Concept("testHead"))))
    }
    @Test
    fun `Fails for missing head`() {
        assertFalse(matchConceptByHead("testHead")(null))
    }
}

class MatchConceptByHeadCollectionTest {
    @Test
    fun `Can match by head name`() {
        assertTrue(matchConceptByHead(listOf("test1Head", "test2Head"))(Concept("test2Head")))
    }
    @Test
    fun `Match by head needs full match`() {
        assertFalse(matchConceptByHead(listOf("test1Head", "test2Head"))(Concept("otherHead")))
    }
    @Test
    fun `Fails for missing head`() {
        assertFalse(matchConceptByHead(listOf("test1Head", "test2Head"))(null))
    }
    @Test
    fun `Fails for no specified heads`() {
        assertFalse(matchConceptByHead(listOf())(Concept("testHead")))
    }
}

class MatchConceptByKindTest {
    @Test
    fun `Can match by kind name`() {
        assertTrue(matchConceptByKind("testKind")(Concept("head").value("kind", Concept("testKind"))))
    }
    @Test
    fun `Match by kind needs full match`() {
        assertFalse(matchConceptByHead("testKind")(Concept("head").value("kind", Concept("otherKind"))))
    }
    @Test
    fun `Only matches on kind`() {
        assertFalse(matchConceptByKind("testKind")(Concept("head").value("other", Concept("testKind"))))
    }
    @Test
    fun `Fails for missing head`() {
        assertFalse(matchConceptByKind("testHead")(null))
    }
}

class MatchConceptByKindCollectionTest {
    @Test
    fun `Can match by kind name`() {
        assertTrue(matchConceptByKind(listOf("testKind"))(Concept("head").value("kind", Concept("testKind"))))
    }
    @Test
    fun `Match by kind needs full match`() {
        assertFalse(matchConceptByHead(listOf("testKind"))(Concept("head").value("kind", Concept("otherKind"))))
    }
    @Test
    fun `Only matches on kind`() {
        assertFalse(matchConceptByKind(listOf("testKind"))(Concept("head").value("other", Concept("testKind"))))
    }
    @Test
    fun `Fails for missing head`() {
        assertFalse(matchConceptByKind(listOf("testHead"))(null))
    }
}


class MatchAnyTest {
    @Test
    fun `Matches if all match`() {
        assertTrue(matchAny(listOf(matchAlways(), matchAlways()))(Concept("anything")))
    }
    @Test
    fun `Matches if at least one matches`() {
        assertTrue(matchAny(listOf(matchAlways(), matchNever()))(Concept("anything")))
    }
    @Test
    fun `Fails if none match`() {
        assertFalse(matchAny(listOf(matchNever(), matchNever()))(Concept("anything")))
    }
}

class MatchAllTest {
    @Test
    fun `Matches if all match`() {
        assertTrue(matchAll(listOf(matchAlways(), matchAlways()))(Concept("anything")))
    }
    @Test
    fun `Fails if at least one matches`() {
        assertFalse(matchAll(listOf(matchAlways(), matchNever()))(Concept("anything")))
    }
    @Test
    fun `Fails if none match`() {
        assertFalse(matchAll(listOf(matchNever(), matchNever()))(Concept("anything")))
    }
}

class MatchAlwaysTest {
    @Test
    fun `Matches`() {
        assertTrue(matchAlways()(Concept("anything")))
    }
    @Test
    fun `Matches even for null`() {
        assertTrue(matchAlways()(null))
    }
}

class MatchNeverTest {
    @Test
    fun `Never matches`() {
        assertFalse(matchNever()(Concept("anything")))
    }
    @Test
    fun `Not even for null`() {
        assertFalse(matchNever()(null))
    }
}