package kweb.html.fileUpload


import kotlinx.serialization.json.Json
import kweb.Element
import kweb.util.random
import mu.KotlinLogging
import kotlin.math.abs

private val logger = KotlinLogging.logger {}

class FileFormInput {

    private lateinit var _inputElement: Element

    val inputElement: Element
        get() = _inputElement


    fun setInputElement(e: Element) {
        this._inputElement = e
    }

    fun setAccept(acceptedTypes: String): Unit = inputElement.callJsFunction(
            """document.getElementById({}).accept = {};""", inputElement.id, acceptedTypes)
    fun isMultiSelect(isMultiple: Boolean): Unit = inputElement.callJsFunction(
            "document.getElementById({}).multiple = {}", inputElement.id, isMultiple )
    fun onFileSelect(onFileSelectCallback: () -> Unit) {
        inputElement.on.change { evt ->
            logger.info(evt.retrieved)
            onFileSelectCallback()
        }
    }

    fun retrieveFile(onFileRetrieveCallback: (FileUpload) -> Unit) {
        val callbackId = abs(random.nextInt())

        val js = """
                let fd = document.getElementById({}).files[0]
                let fr = new FileReader()
                fr.readAsDataURL(fd)
                fr.onload = function(){
                    callbackWs({}, {base64Content: fr.result, fileSize: fd.size, fileName: fd.name});
                }
            """.trimIndent()

        inputElement.browser.callJsFunctionWithCallback(js, callbackId, callback = { result ->
            logger.info("Result is $result")
            onFileRetrieveCallback(Json.decodeFromString(FileUpload.serializer(), result.toString()))
        }, inputElement.id, callbackId)
        inputElement.creator?.onCleanup(true) {
            inputElement.browser.removeCallback(callbackId)
        }
    }
}
