package com.dinohud.dartx.resources

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslationsTest {

    private val resourcesDir = File("src/commonMain/composeResources")

    private fun keysIn(folder: String): Set<String> {
        val xml = File(resourcesDir, "$folder/strings.xml").readText()
        return Regex("""<(string|plurals|string-array) name="([^"]+)"""")
            .findAll(xml)
            .map { "${it.groupValues[1]}:${it.groupValues[2]}" }
            .toSet()
    }

    private fun placeholdersIn(folder: String): Map<String, Set<String>> {
        val xml = File(resourcesDir, "$folder/strings.xml").readText()
        return Regex("""<string name="([^"]+)">(.*?)</string>""")
            .findAll(xml)
            .associate { match ->
                match.groupValues[1] to Regex("""%\d+\$[sd]""").findAll(match.groupValues[2]).map { it.value }.toSet()
            }
    }

    @Test
    fun everyLocaleHasTheSameKeys() {
        val english = keysIn("values")
        assertTrue(english.isNotEmpty())
        resourcesDir.listFiles { file -> file.name.startsWith("values-") }!!.forEach { localized ->
            val translated = keysIn(localized.name)
            assertEquals(emptySet(), english - translated, "Missing in ${localized.name}")
            assertEquals(emptySet(), translated - english, "Only in ${localized.name}")
        }
    }

    @Test
    fun translationsKeepTheSamePlaceholders() {
        val english = placeholdersIn("values")
        resourcesDir.listFiles { file -> file.name.startsWith("values-") }!!.forEach { localized ->
            placeholdersIn(localized.name).forEach { (key, placeholders) ->
                assertEquals(english[key], placeholders, "Placeholders differ for $key in ${localized.name}")
            }
        }
    }
}
