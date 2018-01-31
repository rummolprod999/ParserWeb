package parsers

import dateAddHours
import getDate
import downloadFromUrl
import logger
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.jsoup.nodes.Element
import java.util.*

class ParserZakupMos : Iparser {
    val urlPageAll: String = "http://zakupkikomos.ru/e-order2/index.php?SHOWALL_1=1"
    override fun parser() {
        val sPageAll = downloadFromUrl(urlPageAll)
        if (sPageAll == "") {
            logger("Gets empty string urlPageAll", urlPageAll)
            System.exit(0)
        }
        val html = Jsoup.parse(sPageAll)
        val tenders: Elements = html.select("table.new-table > tbody > tr")
        if (tenders.count() == 0) {
            logger("Gets o tenders", urlPageAll)
            return
        }
        else{
            tenders.reverse()
        }
        tenders.forEach { el: Element ->
            try {
                val typeT = el.select("td:eq(0)")?.text()?.trim() ?: ""
                val urlT = el.select("td:eq(1) > p > a[href]")?.attr("href")?.trim() ?: ""
                val url = "http://zakupkikomos.ru/e-order2/$urlT"
                val numb = el.select("td:eq(1) > p > b > span")?.text()?.trim()?.trim { it == '№' } ?: ""
                val purObj = el.select("td:eq(1) > p > a[href]")?.text()?.trim() ?: ""
                val contactP = el.select("td:eq(2) > span")?.text()?.trim() ?: ""
                val dateS = el.select("td:eq(4)")?.text()?.trim() ?: ""
                val dateE = el.select("td:eq(5)")?.text()?.trim() ?: ""
                var dateStart = getDate(dateS)
                if (dateStart != Date(0L)) {
                    dateStart = dateAddHours(dateStart, -1)
                }
                var dateEnd = getDate(dateE)
                if (dateEnd != Date(0L)) {
                    dateEnd = dateAddHours(dateEnd, -1)
                }
                val zm = ZakupMos(url, contactP, numb, purObj, typeT, dateStart, dateEnd)
                zm.parsing()
            } catch (e: Exception) {
                logger("error in ZakupMos.parsing()", e.stackTrace, e)
            }
        }
    }
}