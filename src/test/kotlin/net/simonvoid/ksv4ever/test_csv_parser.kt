package net.simonvoid.ksv4ever

import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeTest
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestHeader {
    private lateinit var defaultFixLine: StringModifier
    private lateinit var defaultColumnNormalizer: StringModifier
    private lateinit var defaultLineSplitter: LineSplitter

    @BeforeTest
    fun setup() {
        val config = CsvSourceConfig(
            stream = "unused".byteInputStream()
        )
        defaultColumnNormalizer = config.effectiveNormalizeColumnName
        defaultLineSplitter = config.splitByComma
        defaultFixLine = config.fixLine
    }

    @Test(dataProvider = "initHeaderDataProvider")
    fun `test initialization of header`(line: String, expectedColumnNames: List<String>) {
        val header = CsvHeader(defaultFixLine(line), defaultColumnNormalizer, defaultLineSplitter)
        val actualColumnNames = header.normalizedColumnNames

        assertEquals(expectedColumnNames, actualColumnNames, "line being split: $line")
    }

    @Test
    fun `test header removes BOM character`() {
        // there is an invisible UTF-8 character ('\uFEFF') call BOM at the start of this line
        val line = """﻿"Org ID","Org  Name","Org Type" """
        val header = CsvHeader(defaultFixLine(line), defaultColumnNormalizer, defaultLineSplitter)

        assertEquals(
            listOf("orgid", "orgname", "orgtype"),
            header.normalizedColumnNames
        )
    }

    @DataProvider
    fun initHeaderDataProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            "",
            listOf("")
        ),
        arrayOf(
            "12,hi",
            listOf("12", "hi")
        ),
        arrayOf(
            "12,  , hi  ",
            listOf("12", "", "hi")
        ),
        arrayOf(
            """12,,hi,"hi there"""",
            listOf("12", "", "hi", "hithere")
        ),
        arrayOf(
            """"12","","hi","hi, there's a dog"""",
            listOf("12", "", "hi", "hi,there'sadog")
        ),
        arrayOf(
            """"12","",,hi,"hi, there's a dog"""",
            listOf("12", "", "", "hi", "hi,there'sadog")
        ),
        arrayOf(
            """"Org ID","Org Name"""",
            listOf("orgid", "orgname")
        )
    )
}