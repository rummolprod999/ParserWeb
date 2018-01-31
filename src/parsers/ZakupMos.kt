package parsers

import AddTenderKomos
import addVNum
import PassDb
import Prefix
import tenderKwords
import UrlConnect
import UserDb
import downloadFromUrl
import logger
import org.jsoup.Jsoup
import java.sql.*
import java.util.Date
import extractNum
import org.jsoup.select.Elements

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
            var NoticeVersion = html.selectFirst("p:has(b:containsOwn(Примечание:))")?.ownText()?.trim() ?: ""
            var IdOrganizer = 0
            val fullnameOrg = html.selectFirst("p:has(b:containsOwn(Предприятие-инициатор:))")?.ownText()?.trim() ?: ""
            if (fullnameOrg != "") {
                val stmto = con.prepareStatement("SELECT id_organizer FROM ${Prefix}organizer WHERE full_name = ?")
                stmto.setString(1, fullnameOrg)
                val rso = stmto.executeQuery()
                if (rso.next()) {
                    IdOrganizer = rso.getInt(1)
                    rso.close()
                    stmto.close()
                } else {
                    rso.close()
                    stmto.close()
                    val stmtins = con.prepareStatement("INSERT INTO ${Prefix}organizer SET full_name = ?", Statement.RETURN_GENERATED_KEYS)
                    stmtins.setString(1, fullnameOrg)
                    stmtins.executeUpdate()
                    val rsoi = stmtins.generatedKeys
                    if (rsoi.next()) {
                        IdOrganizer = rsoi.getInt(1)
                    }
                    rsoi.close()
                    stmtins.close()
                }
            }
            val IdPlacingWay = 0
            var IdEtp = 0
            val etpName = "КОМОС ГРУПП"
            val etpUrl = "http://zakupkikomos.ru"
            try {
                val stmto = con.prepareStatement("SELECT id_etp FROM ${Prefix}etp WHERE name = ? AND url = ? LIMIT 1")
                stmto.setString(1, etpName)
                stmto.setString(2, etpUrl)
                val rso = stmto.executeQuery()
                if (rso.next()) {
                    IdEtp = rso.getInt(1)
                    rso.close()
                    stmto.close()
                } else {
                    rso.close()
                    stmto.close()
                    val stmtins = con.prepareStatement("INSERT INTO ${Prefix}etp SET name = ?, url = ?, conf=0", Statement.RETURN_GENERATED_KEYS)
                    stmtins.setString(1, etpName)
                    stmtins.setString(2, etpUrl)
                    stmtins.executeUpdate()
                    val rsoi = stmtins.generatedKeys
                    if (rsoi.next()) {
                        IdEtp = rsoi.getInt(1)
                    }
                    rsoi.close()
                    stmtins.close()
                }
            } catch (ignored: Exception) {

            }
            var idTender = 0
            val insertTender = con.prepareStatement("INSERT INTO ${Prefix}tender SET id_region = 0, id_xml = ?, purchase_number = ?, doc_publish_date = ?, href = ?, purchase_object_info = ?, type_fz = ?, id_organizer = ?, id_placing_way = ?, id_etp = ?, end_date = ?, cancel = ?, date_version = ?, num_version = ?, notice_version = ?, xml = ?, print_form = ?", Statement.RETURN_GENERATED_KEYS)
            insertTender.setString(1, NumberT)
            insertTender.setString(2, NumberT)
            insertTender.setTimestamp(3, Timestamp(DateSt.time))
            insertTender.setString(4, Url)
            insertTender.setString(5, PurchaseObj)
            insertTender.setInt(6, typeFz)
            insertTender.setInt(7, IdOrganizer)
            insertTender.setInt(8, IdPlacingWay)
            insertTender.setInt(9, IdEtp)
            insertTender.setTimestamp(10, Timestamp(DateEn.time))
            insertTender.setInt(11, cancelstatus)
            insertTender.setTimestamp(12, Timestamp(DateSt.time))
            insertTender.setInt(13, 1)
            insertTender.setString(14, NoticeVersion)
            insertTender.setString(15, Url)
            insertTender.setString(16, Url)
            insertTender.executeUpdate()
            val rt = insertTender.generatedKeys
            if (rt.next()) {
                idTender = rt.getInt(1)
            }
            rt.close()
            insertTender.close()
            AddTenderKomos++
            var idLot = 0
            val LotNumber = 1
            val currency = html.selectFirst("p:has(b:containsOwn(Валюта:))")?.ownText()?.trim() ?: ""
            val mPr = html.selectFirst("b:containsOwn(Сумма лота:)")?.text()?.trim() ?: ""
            val maxPrice = extractNum(mPr)
            val insertLot = con.prepareStatement("INSERT INTO ${Prefix}lot SET id_tender = ?, lot_number = ?, currency = ?, max_price = ?", Statement.RETURN_GENERATED_KEYS)
            insertLot.setInt(1, idTender)
            insertLot.setInt(2, LotNumber)
            insertLot.setString(3, currency)
            insertLot.setString(4, maxPrice)
            insertLot.executeUpdate()
            val rl = insertLot.generatedKeys
            if (rl.next()) {
                idLot = rl.getInt(1)
            }
            rl.close()
            insertLot.close()
            var idCustomer = 0
            if (fullnameOrg != "") {
                val stmto = con.prepareStatement("SELECT id_customer FROM ${Prefix}customer WHERE full_name = ? LIMIT 1")
                stmto.setString(1, fullnameOrg)
                val rso = stmto.executeQuery()
                if (rso.next()) {
                    idCustomer = rso.getInt(1)
                    rso.close()
                    stmto.close()
                } else {
                    rso.close()
                    stmto.close()
                    val stmtins = con.prepareStatement("INSERT INTO ${Prefix}customer SET full_name = ?, is223=1, reg_num = ?", Statement.RETURN_GENERATED_KEYS)
                    stmtins.setString(1, fullnameOrg)
                    stmtins.setString(2, java.util.UUID.randomUUID().toString())
                    stmtins.executeUpdate()
                    val rsoi = stmtins.generatedKeys
                    if (rsoi.next()) {
                        idCustomer = rsoi.getInt(1)
                    }
                    rsoi.close()
                    stmtins.close()
                }
            }
            val purObj: Elements = html.select("table.new-table > tbody > tr")
            purObj.forEach { po ->
                var name = po.select("td:eq(1)")?.text()?.trim() ?: ""
                val addChar = po.select("td:eq(2) > div")?.text()?.trim() ?: ""
                if (addChar != "") {
                    name = "$name ($addChar)"
                }
                val okei = po.select("td:eq(3)")?.text()?.trim() ?: ""
                val quantity_value = po.select("td:eq(4)")?.text()?.trim() ?: ""
                val price = po.select("td:eq(5)")?.text()?.trim() ?: ""
                val insertPurObj = con.prepareStatement("INSERT INTO ${Prefix}purchase_object SET id_lot = ?, id_customer = ?, name = ?, quantity_value = ?, price = ?, okei = ?, customer_quantity_value = ?")
                insertPurObj.setInt(1, idLot)
                insertPurObj.setInt(2, idCustomer)
                insertPurObj.setString(3, name)
                insertPurObj.setString(4, quantity_value)
                insertPurObj.setString(5, price)
                insertPurObj.setString(6, okei)
                insertPurObj.setString(7, quantity_value)
                insertPurObj.executeUpdate()
                insertPurObj.close()
            }
            val delivDate = html.selectFirst("p:has(b:containsOwn(Срок поставки:))")?.ownText()?.trim { it <= ' ' } ?: ""
            val delivPay = html.selectFirst("p:has(b:containsOwn(Условия оплаты:))")?.ownText()?.trim { it <= ' ' } ?: ""
            val delivPlace = html.selectFirst("p:has(b:containsOwn(Условия поставки:))")?.ownText()?.trim { it <= ' ' } ?: ""
            var dTerm = ""
            if (delivDate != "") {
                dTerm = "Срок поставки: ${delivDate}\n"
            }
            if (delivPay != "") {
                dTerm = "${dTerm}Условия оплаты: ${delivPay}\n"
            }
            if (delivPlace != "") {
                dTerm = "${dTerm}Условия поставки: $delivPlace"
            }
            dTerm = dTerm.trim { it <= ' ' }
            if (dTerm != "") {
                val insertCusRec = con.prepareStatement("INSERT INTO ${Prefix}customer_requirement SET id_lot = ?, id_customer = ?, delivery_term = ?")
                insertCusRec.setInt(1, idLot)
                insertCusRec.setInt(2, idCustomer)
                insertCusRec.setString(3, dTerm)
                insertCusRec.executeUpdate()
                insertCusRec.close()
            }
            try {
                tenderKwords(idTender, con)
            } catch (e: Exception) {
                logger("Ошибка добавления ключевых слов", e.stackTrace, e)
            }


            try {
                addVNum(con, NumberT, typeFz)
            } catch (e: Exception) {
                logger("Ошибка добавления версий", e.stackTrace, e)
            }

        })
    }

}

