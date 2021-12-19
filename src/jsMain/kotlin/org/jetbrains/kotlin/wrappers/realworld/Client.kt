package org.jetbrains.kotlin.wrappers.realworld

import kotlinx.browser.document
import kotlinx.browser.window

fun main() {
    window.onload = {
        println(document.getElementById("root"))
    }
}
