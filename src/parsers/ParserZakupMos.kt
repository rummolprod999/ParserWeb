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
        val tenders: Elements = html.select("#table_standart_view tr")
        if (tenders.count() == 0) {
            logger("Gets o tenders", urlPageAll)
            return
        }
        else{
            tenders.removeAt(0)
            tenders.reverse()
        }
        tenders.forEach { el: Element ->
            try {
                val typeT = el.select("td:eq(0) a")?.text()?.trim() ?: ""
                val urlT = el.select("td:eq(0) > p > a[href]")?.attr("href")?.trim() ?: ""
                if(urlT == ""){
                    logger("can not find urlT")
                    return
                }
                val url = "http://zakupkikomos.ru/e-order2/$urlT"
                val numb = el.select("td:eq(0) > p > b > span")?.text()?.trim()?.trim { it == '№' } ?: ""
                if(numb == ""){
                    logger("can not find purNum", url)
                    return
                }
                val purObj = el.select("td:eq(0) > p > a[href]")?.text()?.trim() ?: ""
                if(purObj == ""){
                    logger("can not find purObj", url)
                    return
                }
                val contactP = el.select("td:eq(1) > span")?.text()?.trim() ?: ""
                val dateS = el.select("td:eq(3)")?.text()?.trim() ?: ""
                val dateE = el.select("td:eq(4)")?.text()?.trim() ?: ""
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