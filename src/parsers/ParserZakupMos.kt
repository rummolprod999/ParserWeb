package parsers

import UrlConnect
import downloadFromUrl
import java.util.*

class ParserZakupMos : Iparser {
    override fun parser() {
        println(UrlConnect)
        println(Date().toString())
        for (i in (0..100)){
            var d = downloadFromUrl("http://zakupkikomos.ru/e-order2/view_proposal.php?lot=2506612")
            println(d)
        }
        println(Date().toString())
    }
}