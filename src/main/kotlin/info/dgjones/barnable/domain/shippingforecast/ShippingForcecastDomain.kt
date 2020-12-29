/*
 * Copyright  2020 David G Jones
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

package info.dgjones.barnable.domain.shippingforecast

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.domain.general.GeneralConcepts
import info.dgjones.barnable.domain.general.PhysicalObjects
import info.dgjones.barnable.grammar.ModifierWord
import info.dgjones.barnable.narrative.MopFields
import info.dgjones.barnable.narrative.MopRestaurant
import info.dgjones.barnable.parser.*
import info.dgjones.barnable.util.transformCamelCaseToSpaceSeparatedWords

interface Domain {
    val name: String
    fun buildGeneralDomainLexicon(lexicon: Lexicon)
}

enum class ShippingForecastConcepts {
    ShippingForecast_Region
}

/**
 * Domain to support the MetOffice UK Shipping Forecasts
 *
 * See: https://www.metoffice.gov.uk/weather/specialist-forecasts/coast-and-sea/shipping-forecast
 * See: https://web.archive.org/web/20190401161444/https://www.metoffice.gov.uk/binaries/content/assets/mohippo/pdf/3/3/fact_sheet_no._8.pdf
 */
class ShippingForecastDomain : Domain {
    override val name: String = "ShippingForecast"
    override fun buildGeneralDomainLexicon(lexicon: Lexicon) {
        buildRegions(lexicon)
    }

    private fun buildRegions(lexicon: Lexicon) {
        ShippingForecastEnum.values().forEach {
            lexicon.addMapping(ShippingForecastRegionWord(it))
        }
    }
}

enum class ShippingForecastEnum() {
    Viking,
    NorthUtsire,
    SouthUtsire,
    Forties,
    Cromarty,
    Forth,
    Tyne,
    Dogger,
    Fisher,
    GermanBight,
    Humber,
    Thames,
    Dover,
    Wight,
    Portland,
    Plymouth,
    Biscay,
    Trafalgar,
    FitzRoy,
    Sole,
    Lundy,
    Fastnet,
    IrishSea,
    Shannon,
    Rockall,
    Malin,
    Hebrides,
    Bailey,
    FairIsle,
    Faeroes,
    SoutheastIceland;

    fun title(): String = transformCamelCaseToSpaceSeparatedWords(this.name)
}

enum class SeaState(val minHeight: Double, val maxHeight: Double) {
    Smooth(0.0, 0.5),
    Slight(0.5, 1.25),
    Moderate(1.25, 2.5),
    Rough(2.5, 4.0),
    VeryRough(4.0, 6.0),
    High(6.0, 9.0),
    VeryHigh(9.0, 14.0),
    Phenomenal(14.0, Double.MAX_VALUE)
}

private class ShippingForecastRegionWord(val region: ShippingForecastEnum): WordHandler(EntryWord(region.title().split(" ").first(), region.title().split(" "))) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, GeneralConcepts.PhysicalObject.name) {
            slot(CoreFields.Name, region.name)
            slot(CoreFields.Kind, ShippingForecastConcepts.ShippingForecast_Region.name)
        }.demons
}