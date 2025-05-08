## KSV4EVER - robust mapping of comma-separated values (csv-files) to user-defined data classes for Kotlin on the JVM

The robustness stems from csv columns being identified by (normalized) column name instead of the column index,
which makes this solution work seamlessly in situations in which columns have been swapped or new columns have been inserted.
The names of the column might even have changed slightly. (The default name normalization removes lower/uppercase differences as well as spaces.)
This property is invaluable if the source of the csv-file(s) is not within your organization, and you can't enforce a certain format 
(, e.g. if you regularly import csv-files from government sites). 

You only have to annotate a data class with `@CsvRow` and it’s properties with either 
`@CsvValue` (for Strings, Ints, Doubles and Booleans), `CsvTimestamp` (for LocalDate and LocalDateTime) or `@CsvGeneric` (for user-defined mappings).
Because this library is written in Kotlin, you can define the **nullability** of properties. A blank value in the csv results in a null value of the property
 (assuming the property doesn't have a default value).

```kotlin
@CsvRow data class DataRow(
    @CsvValue(name = "RQIA") val id: String,
    @CsvValue(name = "Number of beds") val bedCount: Int?, // types can be nullable
    val addressLine1: String,                              // without annotation, it's assumed the column name is the property name
    val city: String = "London",                           // without a value in the csv file, the Kotlin default value is used
    @CsvTimestamp(name = "latest check", format = "yyyy/MM/dd|dd/MM/yyyy")  
    val latestCheckDate: LocalDate?,                       // multiple formats can be provided separated by '|'
    @CsvGeneric(name = "offers Cola, Sprite or Fanta", converterName = "beverageBoolean")
    val refreshments: Boolean?                             // a user-defined converter can be used
)

// register a user-defined converter
registerGenericConverter("beverageBoolean") {
    it.toLowerCase()=="indeed"
}

val csvStream: InputStream = """
  city, addressLine1, Number of beds, latest check, RQIA, "offers Cola, Sprite or Fanta"
  if a line doesn't fit the pattern, it will be discarded <- like this line, the next line is fine because city and Number of beds are nullable
      , "2 Marylebone Rd",          ,2020/03/11,   WERS234, nope
  Berlin, "Berkaer Str 41", 1       ,28/08/2012, "NONE123", indeed
  Paris,"Rue Gauge, Maison 1", 4    ,          , "FR92834",
  Atlantis,,25000,,,
  """.trimIndent().byteInputStream()

val dataRows: List<DataRow> = csv2List(
  CsvSourceConfig(
    stream = csvStream 
  )    
)
```
This code is actually executed in the testclass [TestExample](https://github.com/whichdigital/ksv/blob/master/src/test/kotlin/uk/co/whichdigital/ksv/test_example.kt).

## How to Import this Lib into your (Kotlin) Gradle project

via a source dependency!

First, add this git-repository to your projects **settings.gradle.kts** file:
```kotlin
sourceControl {
    gitRepository(java.net.URI.create("https://github.com/simon-void/ksv4ever")) {
        producesModule("net.simonvoid.ksv4ever:ksv4ever")
    }
}
```
then add this dependency (in its latest git-tagged version) to your **build.gradle.kts** file:
```kotlin
implementation("net.simonvoid.ksv4ever:ksv4ever:2.0.0")
``` 
Done.

## Annotations

### class annotation(s)

#### @CsvRow
Is a marker annotation on a data class marking it as 
Boolean conversion


### property annotation(s)

All values are trimmed and stripped of surrounding quotes (default quote is double quote).

#### @CsvValue
for mapping values to **String**, **Int**, **Double** or **Boolean**.
 
Booleans are mapped from a String value by comparing the lowercase version to
"true", "yes", "y" and "1", which are mapped to true, otherwise false.

annotation parameter:
* name (optional): the name of the column this property is instantiated from. If no name is provided, the name of the annotated property is used.

#### @CsvTimestamp
for **LocalDate** and **LocalDateTime**.

annotation parameter:
* name (optional): the name of the column this property is instantiated from. If no name is provided, the name of the annotated property is used.
* format: a format is either a single timestamp pattern (e.g. "yyyy/MM/dd") or multiple patterns separated by '|' (e.g. "yyyy/MM/dd|dd-MM-yyyy")

#### @CsvGeneric
For user-defined mappings to any type. It just has to be assured that the user-defined converter
is registered before the annotation is used. This is done by invoking the global `registerConverter` function.
```text
fun <T: Any> registerGenericConverter(
  converterName: String,
  converter: (String) -> T
)
```
where `T` is the type of the property.

annotation parameter:
* name (optional): the name of the column this property is instantiated from. If no name is provided, the name of the annotated property is used.
* converterName: has to match the name of a registered converter. The return type of the converter has to match the type of the annotated property.

## Code

#### csv2List

Is the global function that converts a csv source (an InputStream plus optional more configuration parameters)
 to a list of the user-defined row type.
```kotlin
val dataRows: List<DataRow> = csv2List(
  CsvSourceConfig(
    stream = csvStream 
  )    
)
```
Invoking `csv2List` will close `csvStream`.

`csv2List` has actually a bunch of optional parameters, apart from the main one that takes in a `CsvSourceConfig` -
that provide statistics about how many rows where discarded/parsed. (As the naming suggests the main idea here is to allow for logging.)
* modifyItem: `(item: T) -> T`: while `csvSourceConfig.fixLine` works on a `String` level, this function modifies items on the resulting class level.
* keepItem: `(item: T) -> Boolean`: a predicate to remove unneeded items from the resulting list. Make sure to check if the option `csvSourceConfig.removeConsecutiveDuplicates` as well. 
* logInvalidLine: `(line: String, msg: String)->Unit`: the line and reason of why a certain row/line was dropped from the csv (, mostly because the number of commas didn't fit).
* logRejectedRecord: `(record: String)->Unit`: the String representation of a CsvRecord (slightly process row/line) that was rejected by the `keepCsvRecord`-Predicate.
* logConversionError: `(record: String, msg: String)->Unit`: a record and why its conversion to the expected (row)type failed (, e.g. because of unfulfilled nullability constraints).
* logSummary: `(invalidLineCount: Int, rejectedRecordCount: Int, conversionErrorCount: Int, itemsCreated: Int)->Unit`: after all lines have been considered, here a summary of the complete process can be logged. 

e.g.
```kotlin
val csvFilePath: String = "data/someFile.csv"
val dataRows: List<DataRow> = csv2List(
  CsvSourceConfig(
    stream = classLoader.getResourceAsStream(csvFilePath),
    logSummary = {invalidLineCount: Int, conversionErrorCount: Int, rejectedItemCount: Int, itemsCreated: Int ->
      logger.info("""
        Finished importing file $csvFilePath
          items imported: $itemsCreated
          rejected items: $rejectedItemCount (optional filter provided)
          csvRecord which couldn't be converted to item: $conversionErrorCount
          lines with invalid format: $invalidLineCount (probably wrong amount of commas)
        """.trimIndent())
    }
  )    
)
```

#### csv2Sequence

Is the global function that converts a csv source (an InputStream plus optional more configuration parameters)
to a sequence of the user-defined row type.

This method is more low level and should be used if `csv2list` isn't a perfect fit,
or if you don't want to have all elements of the csv file in memory at the same time.

```kotlin
val protoRows: Sequence<ProtoRow<DataRow>> = csv2Sequence(
  CsvSourceConfig(
    stream = csvStream 
  )    
)

val dataRows: Sequence<DataRow> = protoRows.filterIsInstance<Success<DataRow>>().map {
    it.item
}
```
with `ProtoRow` defined like this
```kotlin
sealed class ProtoRow<out T:Any> {
  // if the line in the csv file had the wrong number of commas (outside of quotes and compared to the headerline)
  class InvalidLineError(val line: String, val msg: String): ProtoRow<Nothing>()
  // if not all parts of the line could be converted into the expected type (as defined in your @Row annotated data class)
  class ConversionError(val record: String, val msg: String): ProtoRow<Nothing>()
  // contains an instance of the expected @Row annotated data class, given that the data conformed to the expected format
  class Success<out T:Any>(val item: T): ProtoRow<T>()
}
```
Invoking `csv2Sequence` will close `csvStream`.

If you use this method to not have all lines in memory at the same time,
make sure not to use the option `HandleDuplicates.ONLY_DISTINCT_ENTRIES__POTENTIALLY_EXPENSIVE`.

#### CsvSourceConfig
Assuming the InputStream uses UTF8 the instantiation of a `CsvSourceConfig` only needs said InputStream.
But there are more configuration options:
* stream: `InputStream`: the source of the csv.
* charset: `Charset`: the default is UTF8.
* commaChar: `Char`: the default is a normal comma (',') but csv files are known to sometimes use other characters (e.g. a semicolon) as a delimiter.
* quoteChar: `Char`: the default is a double quote, but char (e.g. single quote) can be used.
* fixLine: `(String)->String`: this function is used on every line of the csv file. The idea is to remove e.g. illegal characters. The default removes invisible BOM characters (`\uFEFF` and `\u200B`) from the start of the line.
* duplicateLineStrategy: `HandleDuplicates`: How to handle duplicate lines in the csv file. Possible options:
  - `ALLOW_DUPLICATES`: duplicates are allowed. This is the default.
  - `REMOVE_CONSECUTIVE_DUPLICATES`: consecutive duplicates are removed (which is a computationally inexpensive memory wise).
  - `ONLY_DISTINCT_ENTRIES__POTENTIALLY_EXPENSIVE`: removes all duplicates. Only the first occurrence of a line remains.
    This requires all distinct lines/rows of the csv file to be in memory at the same time, so you might not want to use
    this option if your csv file is more than a gigabyte in size.
* normalizeColumnName: `(String)->String`: if we don't control the source of the csv data (e.g. because the files come from an external source),
 it often happens the column names change slightly between different versions. the `normalizeColumnName`-parameter is supposed to make
 a configuration more robust against such changes. The default version removes all spaces from the column names and maps them to their lower case version.
 If you have different requirements (or the default version leads to collisions of normalized column names), provide your own function.

Here an example of how to define a predicate for the optional `keepCsvRecord`-parameter: (it allows only lines where the number of beds is bigger than 2)
```kotlin
val dataRows: List<DataRow> = csv2List(
  CsvSourceConfig(
    stream = csvStream,
    // or as lambda {row-> row.bedCount?.let {it > 2 } ?: false}
    keepItem = ::onlyRowsWithAtLeastTwoBeds
  )    
)

private fun onlyRowsWithAtLeastTwoBeds(row: DataRow): Boolean = row.bedCount?.let {
    it > 2
} ?: false
```
Of course this filter operation could also be implemented on  `dataRows` after the complete csv source has been parsed:
```kotlin
val dataRows: List<DataRow> = csv2List(CsvSourceConfig(csvStream)).filter {row: DataRow ->
  row.bedCount?.let{nrOfBeds: Int -> nrOfBeds>=2} ?: false
}
```
But doing it after the `List<DataRow>` is created meanst that all the items have already been in memory at the same time.
This can be prohibitive in the case of massive csv files.
Another reason for using the provided `csv2list keepItem` parameter would be better/inbuild logging, since
`csv2list` is counting the number of rejected items, which it then
provides as argument `rejectedItemCount` to `CsvSourceConfig.logSummary`.

#### Origin

This library came into being as a fork of the [ksv library](https://github.com/whichdigital/ksv) 
which I created during my time working for [Which?](https://www.which.co.uk/).

I decided to fork it so that I could continue to take care of it.