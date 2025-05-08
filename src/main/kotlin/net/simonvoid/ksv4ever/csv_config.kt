package net.simonvoid.ksv4ever

import net.simonvoid.ksv4ever.Util.addTrimQuotesToNormalizeColumnNames
import net.simonvoid.ksv4ever.Util.createLineSplitter
import net.simonvoid.ksv4ever.Util.removeSpace
import java.io.InputStream
import java.nio.charset.Charset


data class CsvSourceConfig (
    val stream: InputStream,
    val charset: Charset = Charsets.UTF_8,
    private val commaChar: Char = ',',
    private val quoteChar: Char = '"',
    val fixLine: StringModifier = ::removeBomChars,
    val duplicateLineStrategy: HandleDuplicates = HandleDuplicates.ALLOW_DUPLICATES,
    private val normalizeColumnName: StringModifier = ::toLowerCaseAndRemoveSpace,
) {
    val splitByComma: LineSplitter = createLineSplitter(commaChar, quoteChar)
    val effectiveNormalizeColumnName: StringModifier = addTrimQuotesToNormalizeColumnNames(quoteChar, normalizeColumnName)
}

fun CsvSourceConfig.bufferedReader() = stream.bufferedReader(charset)

internal fun toLowerCaseAndRemoveSpace(s: String) = s.lowercase().removeSpace()
// removing the possible UTF-8 BOM character at the start of each line
internal fun removeBomChars(s: String) = s.trimStart('\uFEFF', '\u200B')

typealias LineSplitter = String.() -> List<String>
typealias StringModifier = (String) -> String