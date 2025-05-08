package net.simonvoid.ksv4ever

import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeTest
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.LocalDate
import java.time.LocalDateTime


private const val DEFAULT_TOWN = "Tokio (default)"
private const val DEFAULT_NR = 96

@CsvRow
data class Row1(
    @CsvValue(name = "town") val a: String,
    @CsvValue var nr: Int?
)

@CsvRow
data class Row2(
    @CsvValue(name = "town") val a: String = DEFAULT_TOWN,
    @CsvValue var nr: Int? = DEFAULT_NR
)

@CsvRow
data class Row3(
    @CsvTimestamp(format = "[d][dd]/[M][MM]/yyyy") val date: LocalDate,
    @CsvTimestamp(name = "date_time", format = "yyyy-[M][MM]-[d][dd] [H][HH]:mm:ss") val dateTime: LocalDateTime?
)

@CsvRow
data class Row4(
    @CsvTimestamp(format = "dd/MM/yyyy|yyyy-MM-dd") val date: LocalDate
)

@CsvRow
data class Row5(
    @CsvGeneric(converterName = "fuzzyBooleanConverter") val truthiness1: FuzzyBoolean = FuzzyBoolean.UNKNOWN,
    @CsvGeneric(converterName = "fuzzyBooleanConverter") val truthiness2: FuzzyBoolean?
)

enum class FuzzyBoolean {
    YES, NO, MAYBE, UNKNOWN;
}

class TestParseCsv {

    @BeforeTest
    fun setup() {
        // for test case `test csv generic parsing`
        registerGenericConverter("fuzzyBooleanConverter") { token: String ->
            try {
                FuzzyBoolean.valueOf(token.uppercase())
            } catch (e: IllegalArgumentException) {
                FuzzyBoolean.UNKNOWN
            }
        }
    }

    @Test(dataProvider = "csvParsingTestDataProvider")
    fun `test basic csv parsing`(msg: String, csv: String, expectedRows: List<Row1>) {
        val actualRows1: Sequence<ProtoRow<Row1>> = csv2Sequence(csv.toCsvSourceConfig())
        assertEquals(actualRows1.filterSuccessToList(), expectedRows, msg)

        val actualRows2: List<Row1> = csv2List(csv.toCsvSourceConfig())
        assertEquals(actualRows2, expectedRows, msg)
    }

    @DataProvider
    fun csvParsingTestDataProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            "basic functionality, expected: get two instances",
            """
            |town,nr
            |Copenhagen,53
            |Malmo, 64
            """.trimMargin(),
            listOf(
                Row1("Copenhagen", 53),
                Row1("Malmo", 64)
            )
        ),
        arrayOf(
            "missing nullable value, expected: initialize value with null",
            """
            |town,nr
            |Malmo,
            """.trimMargin(),
            listOf(
                Row1("Malmo", null)
            )
        )
    )

    @Test(dataProvider = "csvParsingWithDefaultParamsTestDataProvider")
    fun `test csv parsing with parameter default values`(msg: String, csv: String, expectedRows: List<Row2>) {
        val actualRows1: Sequence<ProtoRow<Row2>> = csv2Sequence(csv.toCsvSourceConfig())
        assertEquals(actualRows1.filterSuccessToList(), expectedRows, msg)

        val actualRows2: List<Row2> = csv2List(csv.toCsvSourceConfig())
        assertEquals(actualRows2, expectedRows, msg)
    }

    @DataProvider
    fun csvParsingWithDefaultParamsTestDataProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            "missing non-nullable value with default value, expected: initialize value with default value",
            """
            |town,nr
            |, 64
            """.trimMargin(),
            listOf(
                Row2(
                    DEFAULT_TOWN,
                    64
                )
            )
        ),
        arrayOf(
            "missing nullable value with default value, expected: initialize value with default value",
            """
            |town,nr
            |Cairo,
            """.trimMargin(),
            listOf(
                Row2(
                    "Cairo",
                    DEFAULT_NR
                )
            )
        ),
    )

    @Test(dataProvider = "csvTimestampParsingTestDataProvider")
    fun `test csv timestamp parsing`(msg: String, csv: String, expectedRows: List<Row3>) {
        val actualRows1: Sequence<ProtoRow<Row3>> = csv2Sequence(csv.toCsvSourceConfig())
        assertEquals(actualRows1.filterSuccessToList(), expectedRows, msg)

        val actualRows2: List<Row3> = csv2List(csv.toCsvSourceConfig())
        assertEquals(actualRows2, expectedRows, msg)
    }

    @DataProvider
    fun csvTimestampParsingTestDataProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            "converting timestamp values, expected: initialize value with timestamp value",
            """
            |date,date_time
            |26/04/2018, 2019-03-27 10:15:30
            |2/4/2018, 2019-3-7 8:15:30
            """.trimMargin(),
            listOf(
                Row3(
                    LocalDate.of(2018, 4, 26),
                    LocalDateTime.of(2019, 3, 27, 10, 15, 30)
                ),
                Row3(
                    LocalDate.of(2018, 4, 2),
                    LocalDateTime.of(2019, 3, 7, 8, 15, 30)
                )
            )
        ),
        arrayOf(
            "converting nullable timestamp values, expected: missing value becomes null",
            """
            |date,date_time
            |11/12/2015,
            """.trimMargin(),
            listOf(
                Row3(
                    LocalDate.of(2015, 12, 11),
                    null
                )
            )
        )
    )

    @Test(dataProvider = "csvTimestampParsingInMultipleFormatsTestDataProvider")
    fun `test csv timestamp parsing with multiple formats`(msg: String, csv: String, expectedRows: List<Row4>) {
        val actualRows1: Sequence<ProtoRow<Row4>> = csv2Sequence(csv.toCsvSourceConfig())
        assertEquals(actualRows1.filterSuccessToList(), expectedRows, msg)

        val actualRows2: List<Row4> = csv2List(csv.toCsvSourceConfig())
        assertEquals(actualRows2, expectedRows, msg)
    }

    @DataProvider
    fun csvTimestampParsingInMultipleFormatsTestDataProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            "converting timestamp values, expected: initialize value with timestamp value",
            """
            |date
            |26/04/2018
            |2013-10-12
            """.trimMargin(),
            listOf(
                Row4(LocalDate.of(2018, 4, 26)),
                Row4(LocalDate.of(2013, 10, 12))
            )
        )
    )

    @Test(dataProvider = "csvGenericTestDataProvider")
    fun `test csv generic parsing`(msg: String, csv: String, expectedRows: List<Row5>) {
        val actualRows1: Sequence<ProtoRow<Row5>> = csv2Sequence(csv.toCsvSourceConfig())
        assertEquals(actualRows1.filterSuccessToList(), expectedRows, msg)

        val actualRows2: List<Row5> = csv2List(csv.toCsvSourceConfig())
        assertEquals(actualRows2, expectedRows, msg)
    }

    @DataProvider
    fun csvGenericTestDataProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            "converting generic values, expected: initialize value custom enum, use default FuzyBoolean.UNKNOWN if invalid or null token",
            """
            |truthiness1,truthiness2
            |YES, yes
            |NO, no
            |MAYBE, maybe
            |UNKNOWN, unknown
            |truly new, even more different
            |,
            """.trimMargin(),
            listOf(
                Row5(
                    FuzzyBoolean.YES,
                    FuzzyBoolean.YES
                ),
                Row5(
                    FuzzyBoolean.NO,
                    FuzzyBoolean.NO
                ),
                Row5(
                    FuzzyBoolean.MAYBE,
                    FuzzyBoolean.MAYBE
                ),
                Row5(
                    FuzzyBoolean.UNKNOWN,
                    FuzzyBoolean.UNKNOWN
                ),
                Row5(
                    FuzzyBoolean.UNKNOWN,
                    FuzzyBoolean.UNKNOWN
                ),
                Row5(
                    FuzzyBoolean.UNKNOWN,
                    null
                )
            )
        )
    )

    @Test(dataProvider = "csvFilteringTestProvider")
    fun `test reject items`(
        csv: String,
        keepItem: (Row1) -> Boolean,
        expectedRows: List<Row1>
    ) {
        val actualRows: List<Row1> = csv2List(
            sourceConfig = csv.toCsvSourceConfig(),
            keepItem = { keepItem(it) },
        )
        assertEquals(actualRows, expectedRows)
    }

    @DataProvider
    fun csvFilteringTestProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            """
            |town,nr
            |Copenhagen,1
            |Copenhagen, 5
            """.trimMargin(),
            { row: Row1 -> row.nr?.let { it > 3 } ?: false },
            listOf(
                Row1("Copenhagen", 5)
            )
        ),
        arrayOf(
            """
            |town,nr
            |Copenhagen,1
            |Malmo, 1
            """.trimMargin(),
            { row: Row1 -> row.a.startsWith("M") },
            listOf(
                Row1("Malmo", 1)
            )
        ),
    )

    @Test(dataProvider = "modifyItemTestProvider")
    fun `test modify items`(
        csv: String,
        modifyItem: (Row1) -> Row1,
        expectedRows: List<Row1>
    ) {
        val actualRows: List<Row1> = csv2List(
            sourceConfig = csv.toCsvSourceConfig(),
            modifyItem = { modifyItem(it) },
        )
        assertEquals(actualRows, expectedRows)
    }

    @DataProvider
    fun modifyItemTestProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            """
            |town,nr
            |open from 9AM?15PM,1
            |open from 9AM?15PM?, 5
            """.trimMargin(),
            { row: Row1 ->
                if (row.a.contains("M?1")) {
                    row.copy(a = row.a.replace("M?1", "M-1"))
                } else {
                    row
                }
            },
            listOf(
                Row1("open from 9AM-15PM", 1),
                Row1("open from 9AM-15PM?", 5),
            )
        ),
    )

    @Test(dataProvider = "removeConsecutiveDuplicatesTestProvider")
    fun `test removeConsecutiveDuplicates`(
        csv: String,
        duplicateLineStrategy: HandleDuplicates,
        expectedRows: List<Row1>
    ) {
        val actualRows: List<Row1> = csv2List(
            sourceConfig = CsvSourceConfig(
                stream = csv.byteInputStream(),
                duplicateLineStrategy = duplicateLineStrategy
            ),
        )
        assertEquals(actualRows, expectedRows)
    }

    @DataProvider
    fun removeConsecutiveDuplicatesTestProvider(): Array<Array<Any>> = arrayOf(
        arrayOf(
            """
            |town,nr
            |Copenhagen,1
            |Copenhagen,1
            |Malmo, 1
            |Copenhagen,1
            """.trimMargin(),
            HandleDuplicates.ONLY_DISTINCT_ENTRIES__POTENTIALLY_EXPENSIVE,
            listOf(
                Row1("Copenhagen", 1),
                Row1("Malmo", 1),
            )
        ),
        arrayOf(
            """
            |town,nr
            |Copenhagen,1
            |Copenhagen,1
            |Malmo, 1
            |Copenhagen,1
            """.trimMargin(),
            HandleDuplicates.REMOVE_CONSECUTIVE_DUPLICATES,
            listOf(
                Row1("Copenhagen", 1),
                Row1("Malmo", 1),
                Row1("Copenhagen", 1),
            )
        ),
        arrayOf(
            """
            |town,nr
            |Copenhagen,1
            |Copenhagen,1
            |Malmo, 1
            |Copenhagen,1
            """.trimMargin(),
            HandleDuplicates.ALLOW_DUPLICATES,
            listOf(
                Row1("Copenhagen", 1),
                Row1("Copenhagen", 1),
                Row1("Malmo", 1),
                Row1("Copenhagen", 1),
            )
        ),
    )
}
