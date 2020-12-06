package net.simonvoid.ksv4ever

// junit and testng have problems with importing top-level functions
object Util {
    fun createLineSplitter(
        commaChar: Char = ',',
        quoteChar: Char = '"',
    ): LineSplitter = {
        val line = this
        var numberOfDoubleQuotesEncountered = 0
        val separatorCommaIndexes = mutableListOf(0)
        line.forEachIndexed { index, char ->
            when (char) {
                quoteChar -> {
                    numberOfDoubleQuotesEncountered++
                }
                commaChar -> {
                    if ((numberOfDoubleQuotesEncountered % 2) == 0) {
                        separatorCommaIndexes.add(index)
                    }
                }
            }
        }
        separatorCommaIndexes.add(line.length)

        mutableListOf<String>().apply {
            if (separatorCommaIndexes.size == 2) {
                add(line)
            } else {
                val commaIndexIter = separatorCommaIndexes.iterator()
                var startIndex = commaIndexIter.next()
                while (commaIndexIter.hasNext()) {
                    val endIndex = commaIndexIter.next()
                    add(line.substring(startIndex, endIndex))
                    startIndex = endIndex + 1
                }
            }
        }.map { it.trimThenTrimQuotesThenTrim(quoteChar) }
    }

    private fun String.trimThenTrimQuotesThenTrim(quoteChar: Char): String = this.trim().let { trimmed ->
        if (trimmed.startsWith(quoteChar) && trimmed.endsWith(quoteChar) && trimmed.length != 1) {
            trimmed.substring(1, trimmed.lastIndex).trim()
        } else {
            trimmed
        }
    }

    fun addTrimQuotesToNormalizeColumnNames(
        quoteChar: Char,
        originalNormalizeColumnNames: StringModifier,
    ): StringModifier = {
        originalNormalizeColumnNames(it.trimThenTrimQuotesThenTrim(quoteChar))
    }

    fun String.removeSpace() = this.replace(" ", "")

    /**
     * Filters duplicate elements of a sequence in accordance to the [HandleDuplicates] parameter.
     */
    fun <T : Any> Sequence<T>.handleConsecutiveDuplicates(howToHandleDuplicates: HandleDuplicates): Sequence<T> =
        when (howToHandleDuplicates) {
            HandleDuplicates.ALLOW_DUPLICATES -> this
            HandleDuplicates.REMOVE_CONSECUTIVE_DUPLICATES -> {
                var priorEntry: T? = null
                this.mapNotNull { entry ->
                    if (entry == priorEntry) {
                        null
                    } else {
                        priorEntry = entry
                        entry
                    }
                }
            }
            HandleDuplicates.ONLY_DISTINCT_ENTRIES__POTENTIALLY_EXPENSIVE -> this.distinct()
        }
}

enum class HandleDuplicates {
    /** no attempt to remove duplicates lines in the csv file is undertaken. */
    ALLOW_DUPLICATES,
    /** consecutive line duplicates are removed. Since this only requires the latest line to be memorised, it can be done efficiently. */
    REMOVE_CONSECUTIVE_DUPLICATES,
    /** removes all duplicates (by invoking `sequence.distinct`) which means only the first occurrence of an element will remain.
     * You mustn't use this option, if you use `csv2sequence` in order to avoid having all lines in memory at the same time. */
    ONLY_DISTINCT_ENTRIES__POTENTIALLY_EXPENSIVE;
}