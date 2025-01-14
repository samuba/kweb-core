package kweb.html.events

import kweb.WebBrowser
import java.util.*

interface EventGenerator<T> {
    val browser: WebBrowser
    fun addImmediateEventCode(eventName: String, jsCode: String)
    fun addEventListener(eventName: String, returnEventFields: Set<String> = Collections.emptySet(), retrieveJs: String?, callback: (Any) -> Unit): T
}