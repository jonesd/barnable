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

package info.dgjones.barnable.concept

interface Fields {
    val fieldName: String
    fun fieldName(): String {
        return fieldName
    }
}

enum class CoreFields(override val fieldName: String): Fields {
    // Related episodic concept identity
    Instance("instance"),
    Event("event"),
    Kind("kind"),
    Name("name"),
    Is("is"),
    State("state"),
    Scale("scale"),
    // FIXME not sure where these should be stored
    Location("location")
}

enum class ScaleConcepts {
    GreaterThanNormal,
    Normal,
    LessThanNormal
}

enum class StateConcepts {
    Positive,
    Neutral,
    Negative
}
