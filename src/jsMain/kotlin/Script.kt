/**
 * JS script for diktat-demo main page
 */

// todo fix and usuppress
@file:Suppress("PACKAGE_NAME_MISSING", "MISSING_KDOC_TOP_LEVEL", "MISSING_KDOC_ON_FUNCTION", "MISSING_KDOC_CLASS_ELEMENTS",
        "KDOC_WITHOUT_PARAM_TAG", "KDOC_WITHOUT_RETURN_TAG")

import kotlin.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement

@JsModule("ace-builds")
@JsNonModule
@JsName("ace")
external object Ace {
    @JsName("edit")
    fun edit(editorName: String): Editor
}

@JsModule("ace-code-editor")
@JsNonModule
external class Editor {
    /**
     * @param path path to ace theme
     */
    fun setTheme(path: String)

    fun getSession(): Session

    fun setReadOnly(readOnly: Boolean)

    class Session {
        fun setMode(mode: String)

        fun getValue(): String

        fun setValue(text: String)

        fun on(eventName: String, handler: () -> Unit)
    }
}

@JsName("$")
external fun jQuery(query: String): Jquery

external class Jquery {
    fun ready(handler: () -> Unit)
}

fun getFile() = (document.getElementById("upfile") as HTMLElement).click()

// todo unused?
// fun sub(obj: Any) {
// val file = obj.value
// val filePathParts = file.split("\\")
// document.getElementById("yourBtn").innerHTML = filePathParts.last()
// document.myForm.submit()
// }

fun main() {
    jQuery("document").ready {
        val editor = Ace.edit("editor")
        editor.setTheme("ace/theme/monokai")
        editor.getSession().setMode("ace/mode/kotlin")

        val result = Ace.edit("result")
        result.setTheme("ace/theme/monokai")
        result.getSession().setMode("ace/mode/kotlin")
        result.setReadOnly(true)

        val textarea = document.getElementById("source") as HTMLTextAreaElement
        editor.getSession().setValue(textarea.value)
        editor.getSession().on("change") {
            textarea.value = editor.getSession().getValue()
        }
    }
}
