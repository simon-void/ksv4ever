package net.simonvoid.ksv4ever

fun String.toCsvSourceConfig(
        commaChar: Char = ',',
        quoteChar: Char = '"',
        fixLine: StringModifier = ::removeBomChars,
        normalizeColumnName: StringModifier = ::toLowerCaseAndRemoveSpace
) = CsvSourceConfig(
    stream = this.byteInputStream(),
    charset = Charsets.UTF_8,
    commaChar = commaChar,
    quoteChar = quoteChar,
    fixLine = fixLine,
    normalizeColumnName = normalizeColumnName
)

fun <T: Any> Sequence<ProtoRow<T>>.filterSuccessToList() = this.mapNotNull {
    if(it is ProtoRow.Success<T>) {
        it.item
    } else {
        null
    }
}.toList()