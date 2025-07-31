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

/*Notice
 * This file has been modified. It is not the original.
 * Jon Freeman - 2013
 */


import android.content.Context
import org.cups4j.operations.cups.CupsGetDefaultOperation
import org.cups4j.operations.cups.CupsGetPrintersOperation
import org.cups4j.operations.ipp.IppCancelJobOperation
import org.cups4j.operations.ipp.IppGetJobAttributesOperation
import timber.log.Timber
import java.net.URL
import java.security.cert.X509Certificate

/**
 * Main client for accessing CUPS (Common Unix Printing System) features.
 * 
 * This class provides methods to interact with CUPS servers, including:
 * - Discovering available printers
 * - Getting printer details
 * - Printing documents
 * - Managing print jobs (getting attributes, canceling)
 * 
 * The client handles connection details, authentication, and certificate validation
 * when communicating with CUPS servers.
 *
 * @param context Android context used for network operations and resource access
 * @param url The URL of the CUPS server, defaults to http://localhost:631
 * @param userName The username for authentication with the CUPS server, defaults to "anonymous"
 */
class CupsClient @JvmOverloads constructor(
    val context: Context,
    private val url: URL = URL(DEFAULT_URL),
    private val userName: String = DEFAULT_USER
) {
    /**
     * Stores server certificates if they're not trusted by the system.
     * Used for custom certificate validation and user prompting.
     */
    var serverCerts: Array<X509Certificate>? = null
        private set 

    /**
     * The HTTP response code from the last CUPS server request.
     * Useful for debugging connection issues.
     */
    var lastResponseCode: Int = 0
        private set

    /**
     * Path to list of printers, like xxx://ip:port/printers/printer_name => will contain '/printers/'
     * seen in issue: https://github.com/BenoitDuffez/AndroidCupsPrint/issues/40
     */
    private var path = "/printers/"

    /**
     * Retrieves a list of available printers from the CUPS server.
     * 
     * This method queries the CUPS server for available printers and marks the default printer
     * if one is configured. The method can optionally filter results by name and limit the
     * number of results returned.
     *
     * @param firstName Optional filter to return only printers whose names start with this string
     * @param limit Optional maximum number of printers to return
     * @return A list of [CupsPrinter] objects representing available printers
     * @throws Exception If there's an error communicating with the CUPS server
     */
    @Throws(Exception::class)
    fun getPrinters(firstName: String? = null, limit: Int? = null): List<CupsPrinter> {
        val cupsGetPrintersOperation = CupsGetPrintersOperation(context)
        val printers: List<CupsPrinter>
        try {
            printers = cupsGetPrintersOperation.getPrinters(url, path, firstName, limit)
        } finally {
            serverCerts = cupsGetPrintersOperation.serverCerts
            lastResponseCode = cupsGetPrintersOperation.lastResponseCode
        }
        val defaultPrinter = defaultPrinter

        for (p in printers) {
            if (defaultPrinter != null && p.printerURL.toString() == defaultPrinter.printerURL.toString()) {
                p.isDefault = true
            }
        }

        return printers
    }

    /**
     * Gets the default printer configured on the CUPS server.
     * 
     * @return The default [CupsPrinter] or null if no default is configured
     * @throws Exception If there's an error communicating with the CUPS server
     */
    private val defaultPrinter: CupsPrinter?
        @Throws(Exception::class)
        get() = CupsGetDefaultOperation(context).getDefaultPrinter(url, path)

    /**
     * Gets the hostname of the CUPS server this client is connected to.
     * 
     * @return The hostname of the CUPS server
     */
    val host: String
        get() = url.host

    /**
     * Gets a specific printer by its URL.
     * 
     * This method attempts to find a printer that matches the given URL by querying
     * the CUPS server and filtering the results.
     *
     * @param printerURL The URL of the printer to retrieve
     * @return The [CupsPrinter] object if found, or null if no matching printer exists
     * @throws Exception If there's an error communicating with the CUPS server
     */
    @Throws(Exception::class)
    fun getPrinter(printerURL: URL): CupsPrinter? {
        // Extract the printer name
        var name = printerURL.path
        val pos = name.indexOf('/', 1)
        if (pos > 0) {
            name = name.substring(pos + 1)
        }

        val printers = getPrinters(name, 1)

        Timber.d("getPrinter: Found ${printers.size} possible CupsPrinters")
        for (p in printers) {
            if (p.printerURL.path == printerURL.path)
                return p
        }
        return null
    }

    /**
     * Gets the attributes of a specific print job.
     *
     * @param jobID The ID of the print job to query
     * @return A [PrintJobAttributes] object containing the job's attributes
     * @throws Exception If there's an error communicating with the CUPS server
     */
    @Throws(Exception::class)
    fun getJobAttributes(jobID: Int): PrintJobAttributes = getJobAttributes(url, userName, jobID)

    /**
     * Internal method to get job attributes from a specific CUPS server.
     *
     * @param url The URL of the CUPS server
     * @param userName The username for authentication
     * @param jobID The ID of the print job to query
     * @return A [PrintJobAttributes] object containing the job's attributes
     * @throws Exception If there's an error communicating with the CUPS server
     */
    @Throws(Exception::class)
    private fun getJobAttributes(url: URL, userName: String, jobID: Int): PrintJobAttributes =
        IppGetJobAttributesOperation(context).getPrintJobAttributes(url, userName, jobID)

    /**
     * Cancels a specific print job.
     *
     * @param jobID The ID of the print job to cancel
     * @return true if the job was successfully canceled, false otherwise
     * @throws Exception If there's an error communicating with the CUPS server
     */
    @Throws(Exception::class)
    fun cancelJob(jobID: Int): Boolean =
        IppCancelJobOperation(context).cancelJob(url, userName, jobID)

    /**
     * Sets the path to the printers on the CUPS server.
     *
     * This method ensures the path starts and ends with a slash for proper URL formatting.
     * The path is typically "/printers/" for standard CUPS installations, but some servers
     * may use different paths.
     *
     * @param path Path to printers on the CUPS server (e.g., "/printers/")
     * @return This CupsClient instance for method chaining
     */
    fun setPath(path: String): CupsClient {
        this.path = path
        if (!path.startsWith("/")) {
            this.path = "/$path"
        }
        if (!path.endsWith("/")) {
            this.path += "/"
        }
        return this
    }

    companion object {
        const val DEFAULT_USER = "anonymous"
        private const val DEFAULT_URL = "http://localhost:631"
    }
}
