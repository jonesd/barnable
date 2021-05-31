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

/***
 * This file uses material from the Wikipedia article <a href="https://en.wikipedia.org/w/index.php?title=Snip-Snap-Snorum&oldid=989191986">"Snip-Snap-Snorum"</a>, which is released under the <a href="https://creativecommons.org/licenses/by-sa/3.0/">Creative Commons Attribution-Share-Alike License 3.0</a>.
 */
package info.dgjones.barnable.domain.cardgames.game

import info.dgjones.barnable.domain.cardgames.CardGame
import info.dgjones.barnable.util.CopyrightLicence
import info.dgjones.barnable.util.SourceMaterial

class GameSnipSnapSnorum : CardGame(snipSnapSnorumRules) {
}

private val snipSnapSnorumRules = SourceMaterial(
    content = """There are several methods of playing the game, but in the most common a full Whist pack is used and any number of players may take part. The pack is dealt, one card at a time, and the eldest hand places upon the table any card of his choosing. Each player in his turn then tries to match the card played just before his; playing it while saying one of the prescribed words: "Snip!", "Snap!" or "Snorem!" in sequence. Thus, if a King is played, the next player lays down another King (if one is in-hand) calling out "Snip!". The next player may lay down the third King if available, saying "Snap!", and the next the fourth King with the word "Snorem!". A player not being able to pair the card played may not discard, and the holder of snorem has the privilege of beginning the next round. The player who gets rid of all cards in-hand first wins a counter from the other players for each card still held by them.""",
    contentUrl = "https://en.wikipedia.org/w/index.php?title=Snip-Snap-Snorum&oldid=989191986",
    licence = CopyrightLicence.CC_BY_SA_30
)