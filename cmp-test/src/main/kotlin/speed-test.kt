
import io.whiterasbk.kotlin.slowxml.parseXml
import java.io.File
import org.dom4j.io.SAXReader
import org.slf4j.LoggerFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

val logger = LoggerFactory.getLogger("cmp - testing")

fun main() {    
    val xmlFile = File("../src/commonTest/resources/android.xml") 

    val xml = xmlFile.readText()
    val times = 400_000

    logger.info("init dom4j")

    val reader = SAXReader()

    logger.info("testing dom4j")

    val dom4jTime = averageTime(times) {
        val document = reader.read(xml.byteInputStream())
    }

    logger.info("testing slowxml")

    val slowTime = averageTime(times) {
        val slowxmlNode = parseXml(xml)
    }

    logger.info("test compelete")

    
    logger.info("dom4j:  ${dom4jTime}ms")
    logger.info("slowxml: ${slowTime}ms")
    runBlocking {
    
        for (c in "It's ") {
            print(c)
            delay(500)
        }

        for (c in "reeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeaaaaaly ") {
            print(c)
            delay(50)
        }

        
        for (c in "slow") {
            print(c)
            delay(200)
        }

        println()

        delay(2_000)

        println("right?")
        
        delay(1_000)
    }
}

inline fun averageTime(testTimes: Int = 500, block: () -> Unit): Double {
    val start = System.currentTimeMillis()    
    repeat(testTimes) { block() }
    val end = System.currentTimeMillis()
    return (end - start).toDouble() / testTimes
}