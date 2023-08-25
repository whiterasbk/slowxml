package io.whiterasbk.kotlin.slowxml.test

import io.whiterasbk.kotlin.slowxml.XmlNode
import io.whiterasbk.kotlin.slowxml.loopInXmlString
import io.whiterasbk.kotlin.slowxml.parseXml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class UnitTest {
    @Test
    fun test_parsing_android_xml() {

        val xml = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="locate in ur hear">
    <text>content</text>
    <self-close/>
    <self-close />
    <self-close attr="value1"/>
    <self-close attr="value2" />
    <tag></tag>
    <tag> </tag>
    <tag ></tag>
    <tag></ tag>
    <tag ></ tag>
    <!--    comment-->
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.ComposeTest">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.ComposeTest">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
        """
        val sample = "XmlNode(name=manifest, attributes={xmlns:android=http://schemas.android.com/apk/res/android, package=locate in ur hear}, content=null, children=[XmlNode(name=text, attributes={}, content=content, children=[]), XmlNode(name=self-close, attributes={}, content=null, children=[]), XmlNode(name=self-close, attributes={}, content=null, children=[]), XmlNode(name=self-close, attributes={attr=value1}, content=null, children=[]), XmlNode(name=self-close, attributes={attr=value2}, content=null, children=[]), XmlNode(name=tag, attributes={}, content=null, children=[]), XmlNode(name=tag, attributes={}, content=null, children=[]), XmlNode(name=tag, attributes={}, content=null, children=[]), XmlNode(name=tag, attributes={}, content=null, children=[]), XmlNode(name=tag, attributes={}, content=null, children=[]), XmlNode(name=application, attributes={android:allowBackup=true, android:icon=@mipmap/ic_launcher, android:label=@string/app_name, android:roundIcon=@mipmap/ic_launcher_round, android:supportsRtl=true, android:theme=@style/Theme.ComposeTest}, content=null, children=[XmlNode(name=activity, attributes={android:name=.MainActivity, android:exported=true, android:label=@string/app_name, android:theme=@style/Theme.ComposeTest}, content=null, children=[XmlNode(name=intent-filter, attributes={}, content=null, children=[XmlNode(name=action, attributes={android:name=android.intent.action.MAIN}, content=null, children=[]), XmlNode(name=category, attributes={android:name=android.intent.category.LAUNCHER}, content=null, children=[])])])])])"
        val node = parseXml(xml)
        assertEquals(node.toString(), sample)
    }

    @Test
    fun test_doctype() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <svg/>
        """
        assertEquals(XmlNode("svg"), parseXml(xml))
    }

    @Test
    fun test_comment() {
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
    fun test_tag_name() {
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
    fun test_attributes() {
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
    fun test_text_content() {
        val xml = parseXml("""
           <root a = "1" b="2">  text </root>
        """)
        assertEquals(xml, XmlNode("root", mutableMapOf("a" to "1", "b" to "2"), "text"))
    }

    @Test
    fun test_close_tag() {
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