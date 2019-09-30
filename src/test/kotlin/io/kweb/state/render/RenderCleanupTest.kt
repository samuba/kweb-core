package io.kweb.state.render

import io.github.bonigarcia.seljup.*
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.creation.tags.InputType.text
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new
import io.kweb.plugins.fomanticUI.fomantic
import io.kweb.plugins.fomanticUI.fomanticUIPlugin
import io.kweb.state.*
import org.apache.tools.ant.taskdefs.Parallel
import org.junit.Ignore
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions


@ExtendWith(SeleniumExtension::class)
class RenderCleanupTest {
    companion object {
        private lateinit var renderCleanupTestApp: RenderCleanupTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            renderCleanupTestApp = RenderCleanupTestApp()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            renderCleanupTestApp.server.close()
        }

        //selenium-jupiter will automatically fall back if the first browser it tries doesn't work
        //https://bonigarcia.github.io/selenium-jupiter/#generic-driver
        @Options
        var chromeOptions = ChromeOptions().apply {
            setHeadless(true)
        }

        @Options
        var firefoxOptions = FirefoxOptions().apply {
            setHeadless(true)
        }
    }

    @Ignore @Test
    fun initialRender(driver : WebDriver) {
        driver.get("http://localhost:7659/")
        val h1 = driver.findElement<WebElement>(By.tagName("H1"))
        h1.shouldNotBeNull()
    }
}

fun main() {
    RenderCleanupTestApp()
}

data class TaskList(val tasks: List<String>)

class RenderCleanupTestApp {
    val taskList = KVar(emptyList<String>())

    val server: Kweb = Kweb(port = 7659, plugins = listOf(fomanticUIPlugin)) {

        val editing = KVar(false)

        doc.body.new {
            render(editing) { _editing ->
                if (_editing) {
                    div(fomantic.ui.form).new {
                        div(fomantic.field).new {
                            label().text("What tasks would you like to prioritize?  (one per line)")
                            val ta = textarea()
                            ta.setValue(taskList.value.joinToString(separator = "\n"))
                            div(fomantic.ui.buttons).new {
                                button(fomantic.ui.button, type = ButtonType.submit).text("Save").on("${ta.jsExpression}.value").click { event ->
                                    taskList.value = event.retrieved!!.split('\n').map { it.trim() }.toList()
                                    editing.value = false
                                }
                                button(fomantic.ui.button, type = ButtonType.submit).text("Cancel").on("${ta.jsExpression}.value").click { event ->
                                    editing.value = false
                                }
                            }
                        }
                    }
                } else {
                    render(taskList.map { it.size }) { listSize ->
                        div(fomantic.ui.bulleted.list).new {
                            for (ix in 0 until listSize) {
                                div(fomantic.item).text(taskList[ix])
                            }
                        }
                        button(fomantic.ui.button).text("Edit").on.click {
                            editing.value = true
                        }
                        Unit
                    }
                }
            }
        }
    }

}

private val stringBool = object : ReversableFunction<Boolean, String>(label = "bool -> string") {
    override fun invoke(from: Boolean) = if (from) "true" else "false"
    override fun reverse(original: Boolean, change: String) = change == "true"
}