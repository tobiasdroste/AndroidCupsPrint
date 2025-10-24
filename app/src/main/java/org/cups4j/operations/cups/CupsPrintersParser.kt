package org.cups4j.operations.cups

import ch.ethz.vppserver.ippclient.IppResult
import ch.ethz.vppserver.schema.ippclient.Attribute
import org.cups4j.CupsPrinter
import java.net.URL

/**
 * Small helper to parse printers from an IPP response result.
 */
internal object CupsPrintersParser {
    fun parsePrinters(
        result: IppResult,
        baseProtocol: String,
        defaultPrinterName: String
    ): List<CupsPrinter> {
        val printers = ArrayList<CupsPrinter>()
        val groups = result.attributeGroupList ?: return printers
        for (group in groups) {
            if (group.tagName == "printer-attributes-tag") {
                var printerURI: String? = null
                var printerName: String? = null
                var printerLocation: String? = null
                var printerDescription: String? = null
                for (attr in group.attribute) {
                    when (attr.name) {
                        "printer-uri-supported" -> printerURI =
                            attr.valueOrNull()?.replace(Regex("ipps?://"), "$baseProtocol://")

                        "printer-name" -> printerName = attr.valueOrNull()
                        "printer-location" -> printerLocation = attr.valueOrNull()
                        "printer-info" -> printerDescription = attr.valueOrNull()
                    }
                }
                val printerUrl: URL = try {
                    URL(printerURI)
                } catch (t: Throwable) {
                    t.printStackTrace()
                    System.err.println(
                        "Error encountered building URL from printer uri of printer " + printerName
                                + ", uri returned was [" + printerURI + "].  Attribute group tag/description: [" + group.tagName
                                + "/" + group.description
                    )
                    throw Exception(t)
                }
                val printer = CupsPrinter(printerUrl, printerName ?: defaultPrinterName, false)
                printer.location = printerLocation
                printer.description = printerDescription
                printers.add(printer)
            }
        }
        return printers
    }
}

private fun Attribute.valueOrNull(): String? =
    if (this.attributeValue.isNotEmpty()) this.attributeValue[0].value else null
