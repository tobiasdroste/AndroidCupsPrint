package com.tobiasdroste.papercups.app.printers.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for [InputPrinter].
 */
class InputPrinterTest {

    /**
     * Tests that [InputPrinter.toPrinter] returns [InputPrinter.PrinterMappingResult.Success]
     * when valid inputs are provided.
     */
    @Test
    fun `toPrinter with valid input returns success`() {
        val inputPrinter = InputPrinter(InputPrinter.Name("Test Printer"), "ipp://192.168.1.100")
        val result = inputPrinter.toPrinter()
        assertThat(result).isInstanceOf(InputPrinter.PrinterMappingResult.Success::class.java)
        val printer = (result as InputPrinter.PrinterMappingResult.Success).printer
        assertThat(printer.name).isEqualTo("Test Printer")
        assertThat(printer.url).isEqualTo("ipp://192.168.1.100:631")
    }

    /**
     * Tests that [InputPrinter.toPrinter] returns [InputPrinter.PrinterMappingResult.Error]
     * when the printer name is blank.
     */
    @Test
    fun `toPrinter with blank name returns error`() {
        val inputPrinter = InputPrinter(InputPrinter.Name(""), "ipp://192.168.1.100")
        val result = inputPrinter.toPrinter()
        assertThat(result).isInstanceOf(InputPrinter.PrinterMappingResult.Error::class.java)
        val error = result as InputPrinter.PrinterMappingResult.Error
        assertThat(error.field).isEqualTo(InputPrinter.PrinterMappingResult.InputField.NAME)
        assertThat(error.message).isEqualTo("Name cannot be blank")
    }

    /**
     * Tests that [InputPrinter.toPrinter] returns [InputPrinter.PrinterMappingResult.Error]
     * when the printer URL is invalid.
     */
    @Test
    fun `toPrinter with invalid url returns error`() {
        val inputPrinter = InputPrinter(InputPrinter.Name("Test Printer"), "invalid-url")
        val result = inputPrinter.toPrinter()
        assertThat(result).isInstanceOf(InputPrinter.PrinterMappingResult.Error::class.java)
        val error = result as InputPrinter.PrinterMappingResult.Error
        assertThat(error.field).isEqualTo(InputPrinter.PrinterMappingResult.InputField.URL)
        assertThat(error.message).isEqualTo("Unable to parse manually-entered URI: invalid-url")
    }

    /**
     * Tests that [InputPrinter.toPrinter] adds the default IPP port to the URL if no port is specified.
     */
    @Test
    fun `toPrinter with url without port adds default port`() {
        val inputPrinter = InputPrinter(InputPrinter.Name("Test Printer"), "ipp://192.168.1.100")
        val result = inputPrinter.toPrinter()
        assertThat(result).isInstanceOf(InputPrinter.PrinterMappingResult.Success::class.java)
        val printer = (result as InputPrinter.PrinterMappingResult.Success).printer
        assertThat(printer.url).isEqualTo("ipp://192.168.1.100:631")
    }

    /**
     * Tests that [InputPrinter.toPrinter] preserves the path in the URL.
     */
    @Test
    fun `toPrinter with url with path preserves path`() {
        val inputPrinter = InputPrinter(
            InputPrinter.Name("Test Printer"),
            "ipp://192.168.1.100/printers/my_printer"
        )
        val result = inputPrinter.toPrinter()
        assertThat(result).isInstanceOf(InputPrinter.PrinterMappingResult.Success::class.java)
        val printer = (result as InputPrinter.PrinterMappingResult.Success).printer
        assertThat(printer.url).isEqualTo("ipp://192.168.1.100:631/printers/my_printer")
    }
}
