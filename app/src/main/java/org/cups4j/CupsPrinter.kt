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
 * Represents a printer on your IPP server
 */

/**
 * Constructor
 *
 * @param printerURL  Printer URL
 * @param name Printer name
 * @param isDefault   true if this is the default printer on this IPP server
 */
class CupsPrinter(
    /**
     * The URL for this printer
     */
    val printerURL: URL,

    /**
     * Name of this printer.
     * For a printer http://localhost:631/printers/printerName 'printerName' will
     * be returned.
     */
    val name: String,

    /**
     * Is this the default printer
     */
    var isDefault: Boolean
) {
    /**
     * Description attribute for this printer
     */
    var description: String? = null

    /**
     * Location attribute for this printer
     */
    var location: String? = null

    /**
     * Print method
     *
     * @param printJob Print job
     * @return PrintRequestResult
     * @throws Exception
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
     * @param map   Attributes map
     * @param value Attribute value
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
     * Get a String representation of this printer consisting of the printer URL
     * and the name
     *
     * @return String
     */
    override fun toString(): String =
        "printer uri=$printerURL default=$isDefault name=$name"
}
