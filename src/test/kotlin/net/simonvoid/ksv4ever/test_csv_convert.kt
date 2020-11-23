package net.simonvoid.ksv4ever

import io.mockk.every
import io.mockk.mockk
import org.testng.Assert.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KType


class TestConvertToken {
    class TestNoAnnotation {
        @Test(dataProvider = "no csv annotation to basic type TestDataProvider")
        fun `test no csv annotation to basic type conversion`(
            rowParam: CsvRowParam.ByNoAnnotation,
            token: String?,
            expectedValue: Any?
        ) {
            val actualValue: Any? = convert(
                token,
                rowParam,
                "name of the csv column the toke was found in",
            )

            assertEquals(actualValue, expectedValue, "for token \"$token\"")
        }

        @DataProvider
        fun `no csv annotation to basic type TestDataProvider`(): Array<Array<Any?>> {
            fun mockNoCsvAnnoForBasicTypes(clazz: KClass<*>) = mockk<CsvRowParam.ByNoAnnotation>().also {
                every { it.paramType } returns mockk<KType>().also { every { it.classifier } returns clazz }
            }

            return arrayOf(
                arrayOf(mockNoCsvAnnoForBasicTypes(String::class), " some value ", "some value"),
                arrayOf(mockNoCsvAnnoForBasicTypes(Int::class), "123", 123),
                arrayOf(mockNoCsvAnnoForBasicTypes(Double::class), " 34.5 ", 34.5),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "true", true),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "yes", true),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "y", true),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "TRUE", true),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "YES", true),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "Y", true),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), " 1 ", true),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "false", false),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "no", false),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "n", false),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "0", false),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "MAYBE", false),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), "not yes", false),
                arrayOf(mockNoCsvAnnoForBasicTypes(String::class), "  ", null),
                arrayOf(mockNoCsvAnnoForBasicTypes(Int::class), "     ", null),
                arrayOf(mockNoCsvAnnoForBasicTypes(Double::class), "  ", null),
                arrayOf(mockNoCsvAnnoForBasicTypes(Boolean::class), " ", null),
            )
        }
    }

    class TestCsvValue {
        @Test(dataProvider = "csvValue to basic type TestDataProvider")
        fun `test csvValue to basic type conversion`(
            rowParam: CsvRowParam.ByCsvValue,
            token: String?,
            expectedValue: Any?
        ) {
            val actualValue: Any? = convert(
                token,
                rowParam,
                "name of the csv column the toke was found in",
            )

            assertEquals(actualValue, expectedValue, "for token \"$token\"")
        }

        @DataProvider
        fun `csvValue to basic type TestDataProvider`(): Array<Array<Any?>> {
            fun mockCsvValueForBasicTypes(clazz: KClass<*>) = mockk<CsvRowParam.ByCsvValue>().also {
                every { it.paramType } returns mockk<KType>().also { every { it.classifier } returns clazz }
            }
            return arrayOf(
                arrayOf(mockCsvValueForBasicTypes(String::class), " some value ", "some value"),
                arrayOf(mockCsvValueForBasicTypes(Int::class), "123", 123),
                arrayOf(mockCsvValueForBasicTypes(Double::class), " 34.5 ", 34.5),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "true", true),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "yes", true),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "y", true),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "TRUE", true),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "YES", true),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "Y", true),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), " 1 ", true),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "false", false),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "no", false),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "n", false),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "0", false),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "MAYBE", false),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), "not yes", false),
                arrayOf(mockCsvValueForBasicTypes(String::class), "  ", null),
                arrayOf(mockCsvValueForBasicTypes(Int::class), "     ", null),
                arrayOf(mockCsvValueForBasicTypes(Double::class), "  ", null),
                arrayOf(mockCsvValueForBasicTypes(Boolean::class), " ", null)
            )
        }
    }

    class TestCsvTimestampConversion {
        @Test(dataProvider = "csvTimestamp to LocalDateTime TestDataProvider")
        fun `test CsvTimestamp to LocalDateTime conversion`(
                rowParam: CsvRowParam.ByCsvTimestamp,
                token: String?,
                expectedDateTime: LocalDateTime?
        ) {
            val actualDateTime: Any? = convert(
                    token,
                    rowParam,
                    "name of the csv column the toke was found in",
            ) as LocalDateTime?

            assertEquals(actualDateTime, expectedDateTime, "token: \"$token\", expected format: \"${rowParam.format}\"")
        }

        @Test(dataProvider = "csvTimestamp to LocalDate TestDataProvider")
        fun `test CsvTimestamp to LocalDate conversion`(
                rowParam: CsvRowParam.ByCsvTimestamp,
                token: String?,
                expectedDate: LocalDate?
        ) {
            val actualDate = convert(token, rowParam, "name of the csv column the toke was found in") as LocalDate?

            assertEquals(actualDate, expectedDate, "token: \"$token\", expected format: \"${rowParam.format}\"")
        }

        @DataProvider
        fun `csvTimestamp to LocalDateTime TestDataProvider`(): Array<Array<Any?>> {
            fun mockCsvTimestampForLocalDateTime(format: String) = mockk<CsvRowParam.ByCsvTimestamp>().also {
                every { it.paramType } returns mockk<KType>().also { every { it.classifier } returns LocalDateTime::class }
                every { it.format } returns format
            }
            return arrayOf(
                arrayOf(
                    mockCsvTimestampForLocalDateTime("yyyy/MM/dd - HH:mm|dd/MM/yyyy - HH:mm"),
                    "2018/09/21 - 00:00",
                    LocalDateTime.of(2018, 9, 21, 0, 0)
                ),
                arrayOf(
                    mockCsvTimestampForLocalDateTime("yyyy/MM/dd - HH:mm"),
                    "2018/09/21 - 20:35",
                    LocalDateTime.of(2018, 9, 21, 20, 35)
                ),
                arrayOf(
                    mockCsvTimestampForLocalDateTime("dd/MM/yyyy - HH:mm"),
                    "21/09/2018 - 11:35",
                    LocalDateTime.of(2018, 9, 21, 11, 35)
                ),
                arrayOf(
                    mockCsvTimestampForLocalDateTime("yyyy/MM/dd - HH:mm"),
                    "",
                    null
                )
            )
        }

        @DataProvider
        fun `csvTimestamp to LocalDate TestDataProvider`(): Array<Array<Any?>> {
            fun mockCsvTimestampForLocalDate(format: String) = mockk<CsvRowParam.ByCsvTimestamp>().also {
                every { it.paramType } returns mockk<KType>().also { every { it.classifier } returns LocalDate::class }
                every { it.format } returns format
            }
            return arrayOf(
                arrayOf(
                    mockCsvTimestampForLocalDate("yyyy/MM/dd"),
                    "2018/09/21",
                    LocalDate.of(2018, 9, 21)
                ),
                arrayOf(
                    mockCsvTimestampForLocalDate("yyyy/MM/dd"),
                    "",
                    null
                ),
                arrayOf(
                    mockCsvTimestampForLocalDate("dd/MM/yyyy - HH:mm"),
                    "21/09/2018 - 00:00",
                    LocalDate.of(2018, 9, 21)
                )
            )
        }
    }

    class TestCsvGeneric {
        private fun mockCsvGeneric(
            converterName: String,
            clazz: KClass<*>
        ) = mockk<CsvRowParam.ByCsvGeneric>().also {
            every { it.paramType } returns mockk<KType>().also { every { it.classifier } returns clazz }
            every { it.converterName } returns converterName
        }

        @Test
        fun `test csvGeneric to basic type conversion`() {
            registerGenericConverter("gooToTrue") {
                it == "goo"
            }
            val gooToTrueCsvRowParam = mockCsvGeneric("gooToTrue", Boolean::class)
            val columnName = "name of the csv column the toke was found in"

            assertTrue(convert("goo", gooToTrueCsvRowParam, columnName) as Boolean)
            assertFalse(convert("GOO", gooToTrueCsvRowParam, columnName) as Boolean)
        }
    }
}
