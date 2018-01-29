
import parsers.ParserZakupMos

fun main(args: Array<String>) {
    Init()
    parserZakupMos()
}

fun parserZakupMos() {
    logger("Начало парсинга")
    val p = ParserZakupMos()
    try {
        p.parser()
    } catch (e: Exception) {
        logger("Error in ParserZakupMos function", e.stackTrace, e)
        e.printStackTrace()
    }

    logger("Добавили тендеров $AddTender")
    logger("Конец парсинга")

}