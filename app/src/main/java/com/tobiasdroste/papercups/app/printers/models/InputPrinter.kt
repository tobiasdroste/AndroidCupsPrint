package com.tobiasdroste.papercups.app.printers.models

import com.tobiasdroste.papercups.app.printers.models.InputPrinter.PrinterMappingResult.InputField.NAME
import com.tobiasdroste.papercups.app.printers.models.InputPrinter.PrinterMappingResult.InputField.URL
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException

/**
 * A printer that has been manually entered by the user.
 *
 * @property name The name of the printer.
 * @property url The URL of the printer.
 */
data class InputPrinter(
    var name: Name,
    var url: String) {

    data class Name(val value: String) {
        fun isValid() = value.isNotBlank()
    }

    companion object {

        private const val DEFAULT_IPP_PORT = 631

        /**
         * Converts a string to a printer URL, ensuring a port is set.
         * If no port is specified, the default IPP port (631) is used.
         */
        private fun String.toPrinterUrl(): String {
            // Ensure a port is set, and set it to DEFAULT_IPP_PORT if unset
            try {
                val uri = URI(this)
                if (uri.host == null) {
                    throw URISyntaxException(this, "Invalid URI: host is null")
                }
                val port = if (uri.port < 0) DEFAULT_IPP_PORT else uri.port
                var url = uri.scheme + "://" + uri.host + ":" + port
                if (uri.path != null) {
                    url += uri.path
                }

                Timber.d("Set URL to $url")
                return url
            } catch (_: URISyntaxException) {
                throw IllegalArgumentException("Unable to parse manually-entered URI: $this")
            }
        }
    }

    fun toPrinter() = when {
        !name.isValid() -> PrinterMappingResult.Error(NAME, "Name cannot be blank")
        else -> try {
            PrinterMappingResult.Success(Printer(name = name.value, url = url.toPrinterUrl()))
        } catch (_: IllegalArgumentException) {
            PrinterMappingResult.Error(URL, "Unable to parse manually-entered URI: $url")
        }
    }

    sealed class PrinterMappingResult {

        enum class InputField { NAME, URL }

        data class Success(val printer: Printer) : PrinterMappingResult()
        data class Error(val field: InputField, val message: String) : PrinterMappingResult()
    }
}
