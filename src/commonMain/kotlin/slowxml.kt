package io.whiterasbk.kotlin.slowxml

@Throws(XmlParseException::class)
fun parseXml(xml: String): XmlNode {
    val stack = Stack<Any>()
    val attributeNameList = mutableListOf<String>()
    val attributeValueList = mutableListOf<String>()

    loopInXmlString(xml) {
        onOpenTag { stack.push(Open(it)) }

        onCloseTag { closeTagName ->
            closeTagName?.let {
                val children = mutableListOf<XmlNode>()
                var content: String? = null

                while (stack.peek() !is Open) {
                    when (stack.peek()) {
                        is XmlNode -> children += stack.pop() as XmlNode
                        is String -> content = stack.pop() as String
                        else -> raiseException()
                    }
                }
                children.reverse()

                val open = stack.pop()
                if (open !is Open) raiseException("xml segment fatal, ${stack.peek()} is not open tag")
                if (open.name != it) raiseException("xml segment fatal, close label is not match expect: <${open.name}>, actual: <$it>")

                stack += XmlNode(
                    name = open.name,
                    children = children,
                    attributes = open.attributes,
                    content = content
                )
            } ?: run {
                // self close tag
                val open = stack.pop()
                if (open !is Open) raiseException("xml segment fatal, ${stack.peek()} is not open tag")

                val node = XmlNode(
                    name = open.name,
                    attributes = open.attributes
                )
                stack.push(node)
            }
        }

        onTextContent { stack += it.trim() }

        onAttributeName { attributeNameList += it }

        onAttributeValue { attributeValueList += it }

        onEndDefineAttributes {
            val attributes = attributeNameList.zip(attributeValueList) { name, value -> name to value }
            when (val peek = stack.peek()) {
                is Open -> attributes.forEach { (name, value) ->
                    peek.attributes[name] = value
                }
                else -> raiseException("xml segment fatal, ${stack.peek()} is not open tag")
            }
            attributeNameList.clear()
            attributeValueList.clear()
        }
    }

    if (stack.empty() or (stack.size > 1))
        throw XmlParseException("parse failed, internal exception")
    else {
        val root = stack.pop()
        if (root !is XmlNode) throw XmlParseException("parse failed, internal exception")
        return root
    }
}



