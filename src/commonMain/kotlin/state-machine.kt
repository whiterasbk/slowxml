package io.whiterasbk.kotlin.slowxml

@Throws(XmlParseException::class)
fun loopInXmlString(xmlInput: String, block: XmlEventListenerBuilder.() -> Unit) {
    var index = 0
    var state = 1
    var previousState = -1

    val charStack = Stack<Char>()
    val attrStack = Stack<Char>()
    val commentStack = Stack<Char>()
    val textStack = Stack<Char>()

    val builder = XmlEventListenerBuilder()
    builder.block()
    val hook = builder.build()

    // todo: use state machine to parse, or just simply remove it
    val xml = xmlInput.replace("<\\?.*xml.*\\?>".toRegex(), "")

    fun exception(message: String = "xml segment fatal"): Nothing {
        val (line, colum) = calculateLineAndColumn(xml, index)
        throw XmlParseException("message: $message; line: $line, colum: $colum")
    }

    hook.raiseExceptionCallback(::exception)

    for (current in xml) {
        // state machine
        when (state) {
            1 -> state = when (current) {
                in blank -> 1
                '<' -> 2
                else -> exception("xml segment fatal, expect `<`")
            }
            2 -> state = when (current) {
                in blank -> 2
                in validTagNameChar -> 3
                '/' -> {
                    if (index - 1 <= 0) exception()
                    if (xml[index - 1] != '<')
                        exception("xml segment fatal, `/` must be followed by `<`")
                    13
                }
                '!' -> {
                    if (index - 1 <= 0) exception()
                    if (xml[index - 1] != '<')
                        exception("xml segment fatal, `!` must be followed by `<`")
                    17 // comment
                }
                else -> exception("xml segment fatal, expect valid tag name")
            }

            3 -> state = when (current) {
                in validTagNameChar -> 3
                in blank -> 4
                '/' -> 11
                '>' -> 12
                else -> exception("xml segment fatal, expect valid tag name")
            }
            4 -> state = when (current) {
                in blank -> 4
                in validAttrNameChar -> 5
                '/' -> 11
                '>' -> 12
                else -> exception("xml segment fatal, expect valid attr name")
            }

            5 -> state = when (current) {
                in validAttrNameChar -> 5
                in blank -> 6
                '=' -> 7
                else -> exception("xml segment fatal, expect valid attr")
            }

            6 -> state = when (current) {
                in blank -> 6
                '=' -> 7
                else -> exception()
            }
            7 -> state = when (current) {
                in blank -> 7
                '"' -> 8
                else -> exception()
            }
            8 -> state = when (current) {
                '"' -> 4
                else -> 9
            }
            9 -> state = when (current) {
                '"' -> 4
                else -> 9
            }
            10 -> state = when (current) {
                '>' -> 1
                in blank -> 10
                else -> exception()
            }
            11 -> state = when (current) {
                '>' -> 1
                else -> exception()
            }
            12 -> state = when (current) {
                '<' -> 2
                in blank -> 12
                '>' -> exception()
                else -> 15
            }

            13 -> state = when (current) {
                in blank -> 13
                in validTagNameChar -> 14
                else -> exception()
            }

            14 -> state = when (current) {
                '>' -> 1
                in blank -> 10
                in validTagNameChar -> 14
                else -> exception()
            }

            15 -> state = when (current) {
                '<' -> 2
                '>' -> exception()
                else -> 15
            }

            // comments implementation
            17 -> state = when (current) {
                '-' -> 18
                else -> exception()
            }
            18 -> state = when (current) {
                '-' -> 19
                else -> exception()
            }
            19 -> state = when (current) {
                '-' -> exception("xml segment fatal, can not put `--` in comment")
                else -> 20
            }
            20 -> state = when (current) {
                '-' -> 21
                else -> 20
            }
            21 -> state = when (current) {
                '-' -> 22
                else -> 20
            }
            22 -> state = when (current) {
                '>' -> 1
                else -> exception("xml segment fatal, expect `>` to close comment")
            }
        }

        // process
        when {
            state == 3 || state == 14 -> charStack += current
            state == 5 || state == 9 -> attrStack += current
            // meet text content
            //       f
            // <text>apple</text>
            //       ^
            state == 15 -> textStack += current

            // meet open tag
            //    3c         34          34               34                34           3b
            // <svg>      <svg >      <svg a="v">      <svg a="v"/>      <svg />      <svg/>
            //     ^          ^           ^                ^                 ^            ^
            previousState == 3 && (state == 4 || state == 11 || state == 12) -> {
                val tagName = charStack.joinToString("")
                if (!(tagNameRegex matches tagName)) exception("xml segment fatal, tagName `$tagName` is invalid")
                charStack.clear()
                // ====== former implementation ======
                // stack += Open(tagName)
                // ====== former implementation ======
                hook.onOpenTag(tagName)
            }

            // meet close tag
            state == 1 -> {
                when (previousState) {
                    // self close
                    //     b1          4b1               4b1               44b1
                    // <svg/>      <svg />      <svg a="v"/>      <svg a="v" />
                    //      ^            ^                 ^                  ^
                    11 -> {
                        // ====== former implementation ======
                        // val open = stack.pop()
                        // if (open !is Open) exception("xml segment fatal, ${stack.peek()} is not open tag")
                        // val node = XmlNode(
                        //     name = open.name,
                        //     attributes = open.attributes
                        // )
                        // stack.push(node)
                        // ====== former implementation ======
                        hook.onCloseTag(null)
                    }

                    // maybe has children
                    //     d1           d1          da1           da1
                    // </svg>      </ svg>      </svg >      </ svg >
                    //      ^            ^            ^             ^
                    14, 10 -> {
                        val closeName = charStack.joinToString("")
                        charStack.clear()
                        hook.onCloseTag(closeName)
                        // ====== former implementation ======
                        // val children = mutableListOf<XmlNode>()
                        // var content: String? = null

                        // while (stack.peek() !is Open) {
                        //     when (stack.peek()) {
                        //         is XmlNode -> children += stack.pop() as XmlNode
                        //         is String -> content = stack.pop() as String //content.append(stack.pop())
                        //         else -> exception()
                        //     }
                        // }
                        // children.reverse()

                        // val open = stack.pop()
                        // if (open !is Open) exception("xml segment fatal, ${stack.peek()} is not open tag")
                        // if (open.name != closeName) exception("xml segment fatal, close label is not match expect: <${open.name}>, actual: <$closeName>")

                        // val node = XmlNode(
                        //     name = open.name,
                        //     children = children,
                        //     attributes = open.attributes,
                        //     content = content
                        // )
                        // stack.push(node)
                        // ====== former implementation ======
                    }
                }
            }

            // meet end of text content
            //           f2                      ff2
            // <text>apple</text>      <text>apple </text>
            //            ^
            previousState == 15 && state == 2 -> {
                val content = textStack.joinToString("")
                //  ====== former implementation ======
                // textStack.clear()
                // if (content.isNotBlank()) stack.push(content.trim())
                //  ====== former implementation ======
                hook.onTextContent(content)
            }

            // meet open tag ends, or self close tag
            //         94c               44c              94b1              944b1             84c             844c             84b1             844b1
            // <svg a="v">      <svg a="v" >      <svg a="v"/>      <svg a="v" />      <svg a="">      <svg a="" >      <svg a=""/>      <svg a="" />
            //           ^                 ^                ^                  ^                ^                ^               ^                 ^
            previousState == 4 && (state == 11 || state == 12) -> {
                // ====== former implementation ======
                // val attributes = attributeNameList.zip(attributeValueList) { name, value -> name to value }
                // when (val peek = stack.peek()) {
                //     is Open -> attributes.forEach { (name, value) ->
                //         peek.attributes[name] = value
                //     }
                //     else -> exception("xml segment fatal, ${stack.peek()} is not open tag")
                // }

                // attributeNameList.clear()
                // attributeValueList.clear()
                //  ====== former implementation ======
                hook.onEndDefineAttributes()
            }

            // meet equal
            //       7                7
            // <svg a="v">      <svg a="v"/>
            //       ^                ^
            state == 7 && current == '=' -> {
                val attributeName = attrStack.joinToString("")
                if (!(attributeNameRegex matches attributeName)) exception("attributeName `$attributeName` is invalid")
                attrStack.clear()
                hook.onAttributeName(attributeName)
                // ====== former implementation ======
                // attributeNameList += attributeName
                //  ====== former implementation ======
            }

            // meet quote mark at the end side, like attr="val" empty=""
            //         94c              944c              94b1              944b1             84c             844c             84b1             844b1
            // <svg a="v">      <svg a="v" >      <svg a="v"/>      <svg a="v" />      <svg a="">      <svg a="" >      <svg a=""/>      <svg a="" />
            //          ^                ^                 ^                 ^                 ^               ^                ^                ^
            (state == 4 && (previousState == 8 || previousState == 9)) -> {
                val attributeValue = attrStack.joinToString("")
                hook.onAttributeValue(attributeValue)
                attrStack.clear()
                //  ====== former implementation ======
                // attributeValueList += attributeValue
                //  ====== former implementation ======
            }

            // meet comment start
            state == 19 -> commentStack.clear()

            // meet comment value
            state == 20 -> commentStack += current

            // meet comment end
            state == 22 -> {
                val comment = commentStack.joinToString("")
                hook.onComment(comment)
            }
        }
        /*
                if (current != '\r')
                    print(
                        if (state in arrayOf(1, 2, 11, 12, 13, 15) && current !in " \n")
                            "`$current:$state`"
                        else current
                    )
                if (state == 3 || current in blank) print(current) // print tag name
                if (state in arrayOf(5, 7,  9) || current in blank) print(current) // print attr
                if (state in arrayOf(5) || current in blank) print(current) // print attr name
                if (state in arrayOf(7) || current in blank) print(current) // print =
                if (state in arrayOf(8) || current in blank) print(current) // print "
                if (state in arrayOf(9) || current in blank) print(current) // print attr value
                if (state in arrayOf(15) || current in blank) print(current) // print content text
         */
        index ++
        previousState = state
    }

    // ====== former implementation ======
    // if (stack.empty() or (stack.size > 1))
    //     exception("parse failed, internal exception")
    // else {
    //     val root = stack.pop()
    //     if (root !is XmlNode) exception("parse failed, internal exception")
    //     return root
    // }
    //  ====== former implementation ======
}
