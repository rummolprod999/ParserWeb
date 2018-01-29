import java.io.File
import java.lang.reflect.Array.getLength
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.DocumentBuilder
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.text.SimpleDateFormat
import java.util.*


val executePath: String = System.getProperty("user.dir")
var Database: String? = null
var tempDirTenders: String? = null
var logDirTenders: String? = null
var Prefix: String? = null
var UserDb: String? = null
var PassDb: String? = null
var Server: String? = null
var Port: Int = 0
var logPath: String? = null
val DateNow = Date()
var AddTender: Int = 0

fun GetSettings() = try {
    val filePathSetting = executePath + File.separator + "setting_tenders.xml"
    val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val document = documentBuilder.parse(filePathSetting)
    val root = document.documentElement
    val settings = root.childNodes
    for (i in 0 until settings.length) {
        val setng = settings.item(i)
        if (setng.nodeType !== Node.TEXT_NODE) {
            when (setng.nodeName) {
                "database" -> Database = setng.childNodes.item(0).textContent
                "tempdir_tenders_zakupkikomos" -> tempDirTenders = executePath + File.separator + setng.childNodes.item(0).textContent
                "logdir_tenders_zakupkikomos" -> logDirTenders = executePath + File.separator + setng.childNodes.item(0).textContent
                "prefix" -> try {
                    Prefix = setng.childNodes.item(0).textContent
                } catch (e: Exception) {
                    Prefix = ""
                }

                "userdb" -> UserDb = setng.childNodes.item(0).textContent
                "passdb" -> PassDb = setng.childNodes.item(0).textContent
                "server" -> Server = setng.childNodes.item(0).textContent
                "port" -> Port = Integer.valueOf(setng.childNodes.item(0).textContent)
            }
        }

    }
} catch (e: Exception) {
    e.printStackTrace()
    System.exit(1)
}

fun Init() {
    GetSettings()
    if (tempDirTenders == null || tempDirTenders == "") {
        println("Не задана папка для временных файлов, выходим из программы")
        System.exit(0)
    }
    if (logDirTenders == null || logDirTenders == "") {
        println("Не задана папка для логов, выходим из программы")
        System.exit(0)
    }
    val tmp = File(tempDirTenders)
    if (tmp.exists()) {
        tmp.delete()
        tmp.mkdir()
    } else {
        tmp.mkdir()
    }
    val log = File(logDirTenders)
    if (!log.exists()) {
        log.mkdir()
    }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    logPath = "$logDirTenders${File.separator}log_parsing_${dateFormat.format(DateNow)}.log"
}