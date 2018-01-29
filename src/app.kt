
import parsers.ParserZakupMos

fun main(args: Array<String>) {
    Init()
    parserZakupMos()
}

fun parserZakupMos() {
    Logger("Начало парсинга")
    val p = ParserZakupMos()
    try {
        p.parser()
    } catch (e: Exception) {
        Logger("Error in ParserZakupMos function", e.stackTrace, e)
        e.printStackTrace()
    }

    Logger("Добавили тендеров $AddTender")
    Logger("Конец парсинга")

}