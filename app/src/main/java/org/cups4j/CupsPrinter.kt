package org.cups4j

/**
 * Copyright (C) 2009 Harald Weyhing
 *
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 * See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this program; if not, see
 * <http:></http:>//www.gnu.org/licenses/>.
 */

import android.content.Context
import org.cups4j.operations.ipp.IppPrintJobOperation
import java.net.URL

/**
 * Represents a printer on a CUPS (Common Unix Printing System) server.
 * 
 * This class encapsulates the properties and capabilities of a printer accessible
 * via the Internet Printing Protocol (IPP). It provides methods to print documents
 * and manage print jobs for the specific printer.
 *
 * @property printerURL The URL for accessing this printer via IPP
 * @property name The name of this printer as defined on the CUPS server
 * @property isDefault Whether this printer is set as the default printer on the CUPS server
 */
class CupsPrinter(
    /**
     * The URL for accessing this printer via IPP.
     * 
     * This URL typically follows the format: http://server:631/printers/printer-name
     * where 631 is the standard port for IPP.
     */
    val printerURL: URL,

    /**
     * The name of this printer as defined on the CUPS server.
     * 
     * For a printer with URL http://localhost:631/printers/printerName,
     * the name would be 'printerName'.
     */
    val name: String,

    /**
     * Indicates whether this printer is set as the default printer on the CUPS server.
     * 
     * When true, this printer will be used for print jobs that don't specify a printer.
     */
    var isDefault: Boolean
) {
    /**
     * The description of this printer as provided by the CUPS server.
     * 
     * This typically contains information about the printer model, capabilities,
     * or other identifying information.
     */
    var description: String? = null

    /**
     * The physical location of this printer as configured on the CUPS server.
     * 
     * This might indicate the room, building, or department where the printer
     * is located (e.g., "Building A, Room 123").
     */
    var location: String? = null

    /**
     * Sends a print job to this printer.
     *
     * This method processes the print job, applies any specified attributes (like copies,
     * page ranges, duplex settings), and submits it to the CUPS server. It handles the
     * IPP communication and returns the result of the print request.
     *
     * @param printJob The print job to be processed, containing the document and print settings
     * @param context Android context used for network operations
     * @return A [PrintRequestResult] containing the status and details of the print request
     * @throws Exception If there's an error during print job submission or communication with the CUPS server
     */
    @Throws(Exception::class)
    fun print(printJob: PrintJob, context: Context): PrintRequestResult {
        var ippJobID = -1
        val document = printJob.document
        var userName = printJob.userName
        val jobName = printJob.jobName ?: "Unknown"
        val copies = printJob.copies
        val pageRanges = printJob.pageRanges

        var attributes: MutableMap<String, String>? = printJob.attributes

        if (userName == null) {
            userName = CupsClient.DEFAULT_USER
        }
        if (attributes == null) {
            attributes = HashMap()
        }

        attributes["requesting-user-name"] = userName
        attributes["job-name"] = jobName

        val copiesString: String
        val rangesString = StringBuilder()
        if (copies > 0) {// other values are considered bad value by CUPS
            copiesString = "copies:integer:$copies"
            addJobAttribute(attributes, copiesString)
        }
        if (pageRanges != null && "" != pageRanges) {
            val ranges =
                pageRanges.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            var delimiter = ""

            rangesString.append("page-ranges:setOfRangeOfInteger:")
            for (range in ranges) {
                var actualRange = range.trim { it <= ' ' }
                val values =
                    range.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (values.size == 1) {
                    actualRange = "$range-$range"
                }

                rangesString.append(delimiter).append(actualRange)
                // following ranges need to be separated with ","
                delimiter = ","
            }
            addJobAttribute(attributes, rangesString.toString())
        }

        if (printJob.isDuplex) {
            addJobAttribute(attributes, "sides:keyword:two-sided-long-edge")
        }
        val command = IppPrintJobOperation(context)
        val ippResult = command.request(printerURL, attributes, document)

        val result = PrintRequestResult(ippResult)


        for (group in ippResult!!.attributeGroupList!!) {
            if (group.tagName == "job-attributes-tag") {
                for (attr in group.attribute) {
                    if (attr.name == "job-id") {
                        ippJobID = attr.attributeValue[0].value?.toInt()!!
                    }
                }
            }
        }
        result.jobId = ippJobID
        return result
    }

    /**
     * Adds a job attribute to the attributes map for a print job.
     *
     * This method handles the proper formatting of job attributes, including
     * concatenating multiple attributes with the appropriate delimiter.
     *
     * @param map The map of job attributes to modify
     * @param value The attribute value to add, in the format "name:type:value"
     */
    private fun addJobAttribute(map: MutableMap<String, String>, value: String?) {
        val name = "job-attributes"
        if (value != null) {
            var attribute: String? = map[name]
            if (attribute == null) {
                attribute = value
            } else {
                attribute += "#$value"
            }
            map[name] = attribute
        }
    }

    /**
     * Returns a string representation of this printer.
     *
     * The string includes the printer's URI, default status, and name, providing
     * a concise summary of the printer's key properties for debugging and logging.
     *
     * @return A string representation of the printer in the format "printer uri=[URL] default=[Boolean] name=[name]"
     */
    override fun toString(): String =
        "printer uri=$printerURL default=$isDefault name=$name"
}
