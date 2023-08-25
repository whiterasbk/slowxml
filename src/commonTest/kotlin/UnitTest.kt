package io.whiterasbk.kotlin.slowxml.test

import io.whiterasbk.kotlin.slowxml.XmlNode
import io.whiterasbk.kotlin.slowxml.loopInXmlString
import io.whiterasbk.kotlin.slowxml.parseXml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class UnitTest {
    @Test
    fun `test parsing android xml`() {
        val xml = Thread.currentThread().contextClassLoader.getResource("android.xml")?.readText()
        val sample = Thread.currentThread().contextClassLoader.getResource("sample.min.txt")?.readText()
        xml ?: error("get test resource xml failure")
        sample ?: error("get test resource sample failure")
        val node = parseXml(xml)
        assertEquals(node.toString(), sample)
    }

    @Test
    fun `test doctype`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <svg/>
        """
        assertEquals(XmlNode("svg"), parseXml(xml))
    }

    @Test
    fun `test comment`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <svg>
                <!-- <path/> -->
                <g />
                <!-- this is a comment in xml --> 
                <!-- <path/> -->
            </svg>
        """
        assertEquals(XmlNode(
            "svg", children = mutableListOf(XmlNode("g"))
        ), parseXml(xml))
        var get = ""
        loopInXmlString("<r> <!-- comment --> </a>") {
            onComment { get = it }
        }
        assertEquals(get, " comment ")
    }

    @Test
    fun `test tag name`() {
        val xml = parseXml("""
           <root>
               <tag1 />
               <tag2></tag2>
           </root>
        """)
        assertEquals(xml, XmlNode("root",
            children = mutableListOf(XmlNode("tag1") , XmlNode("tag2"))
        ))

        assertFails {
            parseXml("""
                <标签 / >
            """)
        }
    }

    @Test
    fun `test attributes`() {
        val xml = parseXml("""
           <root a = "1" b="2"/>
        """)
        assertEquals(xml, XmlNode("root", mutableMapOf("a" to "1", "b" to "2")))

        assertFails { parseXml("""<a 属性="" />""") }
        assertFails { parseXml("""<a attr=" qo\" qo" />""") }
        assertFails { parseXml("""<a attr== />""") }
        assertFails { parseXml("""<a attr=" />""") }
        assertFails { parseXml("""<attr="" />""") }
        assertFails { parseXml("""<attr=" />""") }
        assertFails { parseXml("""<attr= />""") }
        assertFails { parseXml("""<a="" />""") }
        assertFails { parseXml("""<a=" />""") }
        assertFails { parseXml("""<a= />""") }
        assertFails { parseXml("""<a = />""") }
    }

    @Test
    fun `test text content`() {
        val xml = parseXml("""
           <root a = "1" b="2">  text </root>
        """)
        assertEquals(xml, XmlNode("root", mutableMapOf("a" to "1", "b" to "2"), "text"))
    }

    @Test
    fun `test close tag`() {
        assertFails {
            parseXml("""
                <root></r00t>
            """)
        }

        assertFails {
            parseXml("""
                <root>< /root>
            """)
        }

        assertFails {
            parseXml("""
                <root> < !-- c --> </root>
            """)
        }

        assertFails {
            parseXml("""
                <root> <!-- c -- > </root>
            """)
        }

        assertFails {
            parseXml("""
                <root / >
            """)
        }

        assertFails {
            parseXml("""
                <root> 
                    <a>
                        <b>
                            <c/>
                            <d>
                        </b>
                        <c/>
                        <c></c>
                    </a>
                </root>
            """)
        }
    }


}