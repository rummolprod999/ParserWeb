package parsers

import PassDb
import Prefix
import UrlConnect
import UserDb
import downloadFromUrl
import logger
import org.jsoup.Jsoup
import java.sql.DriverManager
import java.util.*
import java.sql.Connection
import java.sql.Timestamp


data class ZakupMos(val Url: String, val ContactPerson: String, val NumberT: String, val PurchaseObj: String, val TypeT: String, val DateSt: Date, val DateEn: Date, val typeFz: Int = 11) {
    fun parsing() {

        /*Class.forName("com.mysql.jdbc.Driver").newInstance()*/
        DriverManager.getConnection(UrlConnect, UserDb, PassDb).use(fun(con: Connection) {
            val stmt0 = con.prepareStatement("SELECT id_tender FROM ${Prefix}tender WHERE purchase_number = ? AND date_version = ? AND type_fz = ?")
            stmt0.setString(1, NumberT)
            stmt0.setTimestamp(2, Timestamp(DateSt.time))
            stmt0.setInt(3, typeFz)
            val r = stmt0.executeQuery()
            if (r.next()) {
                r.close()
                stmt0.close()
                return
            }
            r.close()
            stmt0.close()
            val stPage = downloadFromUrl(Url)
            if (stPage == "") {
                logger("Gets empty string urlPageAll", Url)
                return
            }
            val html = Jsoup.parse(stPage)
            var cancelstatus = 0
            val stmt = con.prepareStatement("SELECT id_tender, date_version FROM ${Prefix}tender WHERE purchase_number = ? AND cancel=0 AND type_fz = ?")
            stmt.setString(1, NumberT)
            stmt.setInt(2, typeFz)
            val rs = stmt.executeQuery()
            while (rs.next()) {
                val idT = rs.getInt(1)
                val dateB = rs.getTimestamp(2)
                if (DateSt.after(dateB) || dateB == Timestamp(DateSt.time)) {
                    val preparedStatement = con.prepareStatement("UPDATE ${Prefix}tender SET cancel=1 WHERE id_tender = ?")
                    preparedStatement.setInt(1, idT)
                    preparedStatement.execute()
                    preparedStatement.close()
                } else {
                    cancelstatus = 1
                }

            }
            rs.close()
            stmt.close()
            val NoticeVersion = ""
            var IdOrganizer = 0
            val fullnameOrg = html.selectFirst("p:has(b:containsOwn(Предприятие-инициатор:))")?.ownText() ?: ""
            println(fullnameOrg)
        })
    }

}

