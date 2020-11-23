package net.simonvoid.ksv4ever

import net.simonvoid.ksv4ever.Util.createLineSplitter
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestUtil {

    @Test(dataProvider = "createLineSplitterDataProvider")
    fun `test createLineSplitter`(commaChar: Char, quoteChar: Char, line: String, expectedSplits: List<String>) {
        val lineSplitter = createLineSplitter(commaChar, quoteChar)
        val actualSplits = lineSplitter(line)

        assertEquals(actualSplits, expectedSplits, "line being split: $line")
    }

    @DataProvider
    fun createLineSplitterDataProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            ',', '"',
            """  House of the Rising Sun  , " test , shouldn't split " """,
            listOf("House of the Rising Sun", "test , shouldn't split")
        ),
        arrayOf(
            ';', '\'',
            """  House of the Rising Sun  ; ' test ; shouldn't split ' """,
            listOf("House of the Rising Sun", "test ; shouldn't split")
        )
    )
}