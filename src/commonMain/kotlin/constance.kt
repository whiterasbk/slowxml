package io.whiterasbk.kotlin.slowxml

internal const val validTagNameChar = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM:-._0123456789"
internal const val validAttrNameChar = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM:-._0123456789"
internal val attributeNameRegex ="[A-Za-z][A-Za-z0-9-:._]*".toRegex()
internal val tagNameRegex = "[A-Za-z][A-Za-z0-9-:._]*".toRegex()
internal val blank = arrayOf(' ', '\n', '\r', '\t')