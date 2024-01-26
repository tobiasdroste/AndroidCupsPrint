package org.cups4j

/**
 * Copyright (C) 2009 Harald Weyhing
 *
 *
 * This file is part of Cups4J. Cups4J is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *
 * Cups4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License along with Cups4J. If
 * not, see <http:></http:>//www.gnu.org/licenses/>.
 */

import java.io.InputStream

/**
 * Print job class
 */
class PrintJob internal constructor(builder: Builder) {
    val document: InputStream
    val copies: Int
    val pageRanges: String?
    val userName: String?
    val jobName: String?
    var isDuplex = false
    var attributes: MutableMap<String, String>? = null

    init {
        this.document = builder.document
        this.jobName = builder.jobName
        this.copies = builder.copies
        this.pageRanges = builder.pageRanges
        this.userName = builder.userName
        this.isDuplex = builder.duplex
        this.attributes = builder.attributes
    }

    /**
     *
     *
     * Builds PrintJob objects like so:
     *
     *
     *
     * PrintJob printJob = new
     * PrintJob.Builder(document).jobName("jobXY").userName
     * ("harald").copies(2).build();
     *
     *
     *
     * documents are supplied as byte[] or as InputStream
     *
     */
    class Builder
    /**
     * Constructor
     *
     * @param document Printed document
     */(var document: InputStream) {
        var copies = 1
        var pageRanges: String? = null
        var userName: String? = null
        var jobName: String? = null
        var duplex = false
        var attributes: MutableMap<String, String>? = null

        /**
         * Additional attributes for the print operation and the print job
         *
         * @param attributes provide operation attributes and/or a String of job-attributes
         *
         * job attributes are separated by "#"
         *
         * example:
         * `
         * attributes.put("compression","none");
         * attributes.put("job-attributes",
         * "print-quality:enum:3#sheet-collate:keyword:collated#sides:keyword:two-sided-long-edge"
         * );
         * `
         * -> take a look config/ippclient/list-of-attributes.xml for more information
         *
         * @return Builder
         */
        fun attributes(attributes: MutableMap<String, String>): Builder {
            this.attributes = attributes
            return this
        }

        /**
         * Builds the PrintJob object.
         *
         * @return PrintJob
         */
        fun build(): PrintJob = PrintJob(this)
    }
}
