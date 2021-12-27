# Barnable Reader Prototype

Deep text reader library that generates a semantic model from a short input text. At the moment it is very primitive and is in no way a "deep" reader yet. 

The project is focused on learning more about the "BORIS" and "Understanding Editorial Text" systems, which were developed during the 1980s. For example see: https://apps.dtic.mil/dtic/tr/fulltext/u2/a098965.pdf

The library is written in the Kotlin language, and the intention is to generate native and Java libraries, in time.

# Running

Currently, the prototype has very limited capabilities. There is minimal CLI with support for question and answer, however, the primary way that the author has been working with the system is through automated tests to process an input text and generate a resulting model.

## Command Line

There is a very simple CLI that supports entering a short text, which will then be processed, with the resulting data structures listed. Simple Who questions can then be asked of the generated model.

At that point questions can be asked, however this is extremely limited at this stage.

Here is an example of using the the CLI:

``` 
Welcome to Barnable!
====================

This is a primitive english text reader.

Enter text: john had lunch with george

Ask a who question:
Enter text: who had lunch with george

Answer: John
```

You will quickly find that this quite brittle.

## Automated Tests - Library of input texts

The primary way to drive the system during this early development has been to write automated tests that provide an input tex to be processed. The resulting built memory is then asserted against to ensure that the correct model and elements have been captured.

For example:

``` 
fun `Exercise 1 John gave Mary a book`() {
    val textProcessor = runTextProcess("John gave Mary a book", lexicon)

    assertEquals(1, textProcessor.workingMemory.concepts.size)

    val atrans = textProcessor.workingMemory.concepts[0]
    assertEquals(Acts.ATRANS.name, atrans.name)
    assertEquals("John", atrans.value(ActFields.Actor)?.valueName(HumanFields.FirstName))
    assertEquals("book", atrans.value(ActFields.Thing)?.valueName(CoreFields.Name))
    assertEquals("Mary", atrans.value(ActFields.To)?.valueName(HumanFields.FirstName))
    assertEquals("John", atrans.value(ActFields.From)?.valueName(HumanFields.FirstName))
}
```

# Processing

The input text is split up into sentences, and then each sentence is processed.

The words in each sentence are processed from left to right, and are used to build, or lookup, data structures encoding the content. At the end of the sentence some of these data structures will be added to the episodic, or long lived, memory.

# Documentation to Add
- Models/Episodic
- Model for current sentence
- Searching for matching/related elements

# References

- BORIS -- An Experiment in In-Depth Understanding of Narratives
    Wendy Lehnert, Michael G. Dyer, Peter N. Johnson
    https://apps.dtic.mil/dtic/tr/fulltext/u2/a098965.pdf

- In-depth Understanding: A computer model of integrated processing for narrative comprehension
    Michael G. Dyer
    https://www.worldcat.org/title/in-depth-understanding-a-computer-model-of-integrated-processing-for-narrative-comprehension/oclc/827009769

- Understanding Editorial Text: A computer model of Argument Comprehension
    Sergio J. Alvarado
    https://www.worldcat.org/title/understanding-editorial-text-a-computer-model-of-argument-comprehension/oclc/21669425

# License

Copyright  2020 David G Jones

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
