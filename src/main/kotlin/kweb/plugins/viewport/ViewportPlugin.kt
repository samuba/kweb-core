package kweb.plugins.viewport

import kweb.plugins.KwebPlugin
import kweb.plugins.viewport.ViewportWidth.deviceWidth
import org.jsoup.nodes.Document

class ViewportPlugin(val width : ViewportWidth = deviceWidth, val initialScale : Double = 1.0) : KwebPlugin() {
    override fun decorate(doc : Document) {
        // Note: we don't use [MetaElement] because this is a JSoup doc, not Kweb
        doc.head().appendElement("meta")
                .attr("name", "viewport")
                .attr("content", "width=${width.text}, initial-scale=$initialScale")
    }
}

enum class ViewportWidth(val text : String) {
    deviceWidth("device-width")
}