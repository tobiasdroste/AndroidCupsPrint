package com.tobiasdroste.papercups.app.printers.models

import com.tobiasdroste.papercups.app.printers.models.InputPrinter.PrinterMappingResult.InputField.NAME
import com.tobiasdroste.papercups.app.printers.models.InputPrinter.PrinterMappingResult.InputField.URL
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException

/**
 * Class for a manually entered printer
 */
data class InputPrinter(
    var name: Name,
    var url: String) {

    data class Name(val value: String) {
        fun isValid() = value.isNotBlank()
    }

    companion object {

        private fun String.toPrinterUrl(): String {
            // Ensure a port is set, and set it to 631 if unset
            try {
                val uri = URI(this)
                val port = if (uri.port < 0) 631 else uri.port
                var url = uri.scheme + "://" + uri.host + ":" + port
                if (uri.path != null) {
                    url += uri.path
                }

                Timber.d("Set URL to $url")
                return url
            } catch (e: URISyntaxException) {
                throw IllegalArgumentException("Unable to parse manually-entered URI: $this")
            }
        }


    }

    fun toPrinter() = when {
        !name.isValid() -> PrinterMappingResult.Error(NAME, "Name cannot be blank")
        else -> try {
            PrinterMappingResult.Success(Printer(name = name, url = url.toPrinterUrl()))
        } catch (illegalArgumentException: IllegalArgumentException) {
            PrinterMappingResult.Error(URL, "Unable to parse manually-entered URI: $url")
        }
    }

    sealed class PrinterMappingResult {

        enum class InputField { NAME, URL }

        data class Success(val printer: Printer) : PrinterMappingResult()
        data class Error(val field: InputField, val message: String) : PrinterMappingResult()
    }
}
