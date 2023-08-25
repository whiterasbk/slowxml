package io.whiterasbk.kotlin.slowxml

data class XmlNode(
    val name: String,
    val attributes: MutableMap<String, String> = mutableMapOf(),
    val content: String? = null,
    val children: MutableList<XmlNode> = mutableListOf()
)

class XmlParseException(message: String) : Exception(message)

abstract class XmlEventListener {
    abstract fun onOpenTag(tagName: String)
    abstract fun onCloseTag(tagName: String?)
    abstract fun onEndDefineAttributes()
    abstract fun onAttributeName(attribute: String)
    abstract fun onAttributeValue(value: String)
    abstract fun onComment(content: String)
    abstract fun onTextContent(content: String)
    abstract fun raiseExceptionCallback(block: (String) -> Nothing)
}

class XmlEventListenerBuilder {
    private var meetOpenTag: (String) -> Unit = {}
    private var meetCloseTag: (String?) -> Unit = {}
    private var meetEndDefineAttributes: () -> Unit = {}
    private var meetAttributeName: (String) -> Unit = {}
    private var meetAttributeValue: (String) -> Unit = {}
    private var meetComment: (String) -> Unit = {}
    private var meetTextContent: (String) -> Unit = {}
    private var _raiseException: (String) -> Nothing = { error(it) }

    fun onOpenTag(block: (String) -> Unit) { meetOpenTag = block }
    fun onCloseTag(block: (String?) -> Unit) { meetCloseTag = block }
    fun onEndDefineAttributes(block: () -> Unit) { meetEndDefineAttributes = block }
    fun onAttributeName(block: (String) -> Unit) { meetAttributeName = block }
    fun onAttributeValue(block: (String) -> Unit) { meetAttributeValue = block }
    fun onComment(block: (String) -> Unit) { meetComment = block }
    fun onTextContent(block: (String) -> Unit) { meetTextContent = block }
    fun raiseException(message: String = "xml segment fatal"): Nothing = _raiseException(message)

    fun build(): XmlEventListener = object : XmlEventListener() {
        override fun onOpenTag(tagName: String) = meetOpenTag(tagName)
        override fun onCloseTag(tagName: String?) = meetCloseTag(tagName)
        override fun onEndDefineAttributes() { meetEndDefineAttributes() }
        override fun onAttributeName(attribute: String) = meetAttributeName(attribute)
        override fun onAttributeValue(value: String) = meetAttributeValue(value)
        override fun onComment(content: String) = meetComment(content)
        override fun onTextContent(content: String) = meetTextContent(content)
        override fun raiseExceptionCallback(block: (String) -> Nothing) { _raiseException = block }
    }
}

internal class Stack<E> {

    private val _list = mutableListOf<E>()

    val size: Int get() = _list.size

    fun pop(): E = _list.removeLast()

    fun push(e: E) { _list += e }

    fun peek(): E = _list.last()

    fun empty(): Boolean = _list.isEmpty()

    operator fun plusAssign(e: E) {
        _list += e
    }

    fun joinToString(s: String) = _list.joinToString(s)

    fun clear() = _list.clear()
}

internal data class Open(
    var name: String,
    val attributes: MutableMap<String, String> = mutableMapOf()
)