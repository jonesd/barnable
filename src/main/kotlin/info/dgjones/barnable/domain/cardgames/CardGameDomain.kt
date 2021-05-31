/*
 * Copyright  2021 David G Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package info.dgjones.barnable.domain.cardgames

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.domain.shippingforecast.Domain
import info.dgjones.barnable.grammar.Preposition
import info.dgjones.barnable.grammar.expectPrep
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.*
import info.dgjones.barnable.util.SourceMaterial
import info.dgjones.barnable.util.transformCamelCaseToLowerCaseList

/**
 * Domain to support interpreting Card Games as a game model
 */
class CardGameDomain : Domain {
    override val name: String = "CardGame"
    override fun buildDomainLexicon(lexicon: Lexicon) {
        buildInDepthUnderstandingLexicon(lexicon)
        //FIXME buildGeneralDomainLexicon(lexicon)
        addToLexicon(lexicon)
    }
    override fun addToLexicon(lexicon: Lexicon) {
        CardObjects.values().forEach { lexicon.addMapping(PhysicalObjectWord(it)) }
        CardPlayer.values().forEach { lexicon.addMapping(PlayerHandler(it)) }
    }
}

enum class CardObjects(override val title: String, override val kind: PhysicalObjectKind = PhysicalObjectKind.Liquid):
    PhysicalObjectDefinitions {
    Deck("deck"),
    Pack("pack"),
    Card("card"),
    King("king"),
    Queen("queen"),
    Jack("jack"),
    Ace("ace"),
    Table("table"),
}

enum class CardGameConcept {
    Player;

    fun word() = this.name.toLowerCase()
}

open class CardGame(val source: SourceMaterial)

enum class CardPlayer {
    EldestHand,
    Player;

    val words = transformCamelCaseToLowerCaseList(name)
}

class PlayerHandler(private val cardPlayer: CardPlayer) : WordHandler(EntryWord(cardPlayer.words[0], cardPlayer.words)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, CardGameConcept.Player.name) {
            slot(CoreFields.Name, cardPlayer.name)
        }.demons
}



