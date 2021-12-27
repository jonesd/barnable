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

package info.dgjones.barnable.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import info.dgjones.barnable.parser.buildTextModel
import info.dgjones.barnable.parser.runTextProcess
import info.dgjones.barnable.qa.QuestionProcessor

class BarnableCLI : CliktCommand() {
    val text: String? by argument(help = "Input text to process").default("")

    override fun run() {
        showWelcomeMessage()

        processInput(textToProcess())
    }

    private fun showWelcomeMessage() {
        echo("Welcome to Barnable!")
        echo("====================")
        echo("")
        echo("This is a primitive english text reader.")
        echo("")
    }

    fun processInput(input: String) {
        val textProcessor = runTextProcess(input)
        val qa = QuestionProcessor(textProcessor)
        while (true) {
            echo("\n\n\n")
            echo("Question: $input")
            echo("Ask a who question:")
            val result = qa.question(buildTextModel(textToProcess()))
            echo("\n\n")
            echo("Question: $input")
            echo("Answer: ${result.answer}")
        }
    }

    private fun textToProcess(): String {
        val textArgument = text
        return if (textArgument != null && textArgument.isNotBlank()) {
            textArgument
        } else {
            promptForInput()
        }
    }

    private fun promptForInput(): String {
        var input: String? = null
        do {
            input = TermUi.prompt("Enter text")
        } while (input == null || input.isBlank())
        return input
    }
}

fun main(args: Array<String>) = BarnableCLI().main(args)