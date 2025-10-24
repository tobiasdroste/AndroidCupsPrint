package org.cups4j.operations.cups

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

/* 
 * Notice
 * This file has been modified. It is not the original.
 * Jon Freeman - 2013
 * Tobias Droste - 2025
 */

import android.content.Context
import org.cups4j.CupsPrinter
import org.cups4j.operations.IppOperation
import timber.log.Timber
import java.net.URL

open class CupsGetPrintersOperation(context: Context) : IppOperation(context) {
    init {
        operationID = 0x4002
        bufferSize = 8192
    }

    @Throws(Exception::class)
    fun getPrinters(
        url: URL,
        path: String,
        firstName: String? = null,
        limit: Int? = null
    ): List<CupsPrinter> {
        val parameters = HashMap<String, String>()
        parameters["requested-attributes"] =
            "copies-supported page-ranges-supported printer-name printer-info printer-location printer-make-and-model printer-uri-supported"

        if (firstName != null) {
            parameters["first-printer-name"] = firstName
        }
        if (limit != null && limit >= 1) {
            parameters["limit"] = limit.toString()
        }

        val result = request(URL(url.toString() + path), parameters)
            ?: run {
                Timber.e("Couldn't get printers from URL: $url with path: $path")
                return emptyList()
            }

        return CupsPrintersParser.parsePrinters(result, url.protocol, DEFAULT_PRINTER_NAME)
    }
}
