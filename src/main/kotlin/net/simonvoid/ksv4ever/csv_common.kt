package net.simonvoid.ksv4ever

import net.simonvoid.ksv4ever.Util.handleConsecutiveDuplicates
import kotlin.reflect.KClass


inline fun <reified T : Any> csv2List(
    sourceConfig: CsvSourceConfig,
    crossinline modifyItem: (item: T) -> T = { it },
    crossinline keepItem: (item: T) -> Boolean = { true },
    crossinline logInvalidLine: (line: String, msg: String) -> Unit = { _, _ -> },
    crossinline logConversionError: (record: String, msg: String) -> Unit = { _, _ -> },
    crossinline logSummary: (
        invalidLineCount: Int,
        conversionErrorCount: Int,
        rejectedItemCount: Int,
        itemsCreated: Int,
    ) -> Unit = { _, _, _, _ -> },
): List<T> {
    var invalidLinesCount = 0
    var conversionExceptionCount = 0
    var rejectedItemCount = 0

    return csv2Sequence<T>(sourceConfig).mapNotNull { parsedCsvLine ->
        when (parsedCsvLine) {
            is ProtoRow.InvalidLineError -> {
                invalidLinesCount++
                logInvalidLine(parsedCsvLine.line, parsedCsvLine.msg)
                null
            }
            is ProtoRow.ConversionError -> {
                conversionExceptionCount++
                logConversionError(parsedCsvLine.record, parsedCsvLine.msg)
                null
            }
            is ProtoRow.Success -> {
                parsedCsvLine.item.let {
                    if(keepItem(it)) {
                        modifyItem(it)
                    } else {
                        rejectedItemCount++
                        null
                    }
                }
            }
        }
    }.toList().also { itemList ->
        logSummary(
            invalidLinesCount,
            conversionExceptionCount,
            rejectedItemCount,
            itemList.size,
        )
    }
}

inline fun <reified T : Any> csv2Sequence(
    sourceConfig: CsvSourceConfig,
): Sequence<ProtoRow<T>> = csv2Sequence(
    sourceConfig,
    T::class
)

fun <T: Any> csv2Sequence(
    sourceConfig: CsvSourceConfig,
    tClass: KClass<T>,
): Sequence<ProtoRow<T>> {
    val itemFactory = ReflectiveItemFactory(tClass, sourceConfig.effectiveNormalizeColumnName)
    val csvTable = csv2CsvTable(sourceConfig)
    val header = csvTable.header
    return csvTable.csvRecords.map { protoRecord ->
        when(protoRecord) {
            is ProtoCsvRecord.Failure -> ProtoRow.InvalidLineError(protoRecord.line, protoRecord.msg)
            is ProtoCsvRecord.Success -> {
                val record = protoRecord.record
                when (val record2dataResult = record2Item(header, record, itemFactory)) {
                    is Record2ItemResult.ConversionException -> ProtoRow.ConversionError(
                        record.toString(),
                        record2dataResult.e.toString()
                    )
                    is Record2ItemResult.Success -> ProtoRow.Success(record2dataResult.item)
                }
            }
        }
    }
}

sealed class ProtoRow<out T:Any> {
    class InvalidLineError(val line: String, val msg: String): ProtoRow<Nothing>()
    class ConversionError(val record: String, val msg: String): ProtoRow<Nothing>()
    class Success<out T:Any>(val item: T): ProtoRow<T>()
}

private fun csv2CsvTable(
    sourceConfig: CsvSourceConfig,
): CsvTable = sourceConfig.bufferedReader().let { reader ->
    val lineIterator = reader.lineSequence().handleConsecutiveDuplicates(sourceConfig.duplicateLineStrategy).iterator()
    val headerLine = sourceConfig.fixLine(lineIterator.next())
    val header = CsvHeader(headerLine, sourceConfig.effectiveNormalizeColumnName, sourceConfig.splitByComma)
    CsvTable(
        header,
        sequence {
            for (line in lineIterator) {
                val fixedLine = sourceConfig.fixLine(line)
                if (fixedLine.isBlank()) continue
                yield(
                    CsvRecord.constructFrom(
                        fixedLine,
                        header.numberOrColumns,
                        sourceConfig.splitByComma
                    )
                )
            }
            reader.close()
        }
    )
}
