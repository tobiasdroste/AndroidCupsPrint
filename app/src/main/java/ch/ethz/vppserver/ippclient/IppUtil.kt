package ch.ethz.vppserver.ippclient

import java.io.UnsupportedEncodingException
import kotlin.experimental.and

/**
 * Copyright (C) 2008 ITS of ETH Zurich, Switzerland, Sarah Windler Burri
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this program; if not, see
 * <http:></http:>//www.gnu.org/licenses/>.
 */
object IppUtil {
    private const val DEFAULT_CHARSET = "UTF-8"

    /**
     *
     * @param a
     * high byte
     * @param b
     * low byte
     * @return short value
     */
    private fun toShort(a: Byte, b: Byte): Short =
            ((a and (0x00ff shl 8).toByte()) + (b and 0x00ff.toByte())).toShort()

    /**
     *
     * @param b
     * byte
     * @return String
     */
    fun toHex(b: Int): String {
        val st = Integer.toHexString(b)
        return if (st.length == 1) "0$st" else st
    }

    /**
     *
     * @param b
     * byte
     * @return String with Marker '0x' ahead
     */
    fun toHexWithMarker(b: Int): String {
        val sb = StringBuilder()
        sb.append("0x").append(toHex(b))
        return sb.toString()
    }

    /**
     *
     * @param dst
     * array of byte
     * @return String representation
     */
    internal fun toString(dst: ByteArray): String {
        val l = dst.size
        val sb = StringBuilder(l)
        for (i in 0 until l) {
            val b = dst[i].toInt()
            val iVal = b and 0xff
            val c = iVal.toChar()
            sb.append(c)
        }
        return sb.toString()
    }

    /**
     *
     * @param str
     * String to encode
     * @param encoding
     * @return byte array
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun toBytes(str: String, encoding: String? = null): ByteArray =
            str.toByteArray(charset(encoding ?: DEFAULT_CHARSET))

    /**
     * see RFC 2579 for DateAndTime byte length and explanation of byte fields IPP datetime must have
     * a length of eleven bytes
     *
     * @param dst
     * byte array
     * @return String representation of dateTime
     */
    internal fun toDateTime(dst: ByteArray): String {
        val sb = StringBuffer()
        val year = toShort(dst[0], dst[1])
        sb.append(year.toInt()).append("-")
        val month = dst[2]
        sb.append(month.toInt()).append("-")
        val day = dst[3]
        sb.append(day.toInt()).append(",")
        var hours = dst[4]
        sb.append(hours.toInt()).append(":")
        var min = dst[5]
        sb.append(min.toInt()).append(":")
        val sec = dst[6]
        sb.append(sec.toInt()).append(".")
        val decSec = dst[7]
        sb.append(decSec.toInt()).append(",")

        val b = dst[8].toInt()
        val iVal = b and 0xff
        val c = iVal.toChar()
        sb.append(c)

        hours = dst[9]
        sb.append(hours.toInt()).append(":")
        min = dst[10]
        sb.append(min.toInt())
        return sb.toString()
    }

    /**
     * See RFC2910, http://www.ietf.org/rfc/rfc2910 IPP boolean is defined as SIGNED-BYTE where 0x00
     * is 'false' and 0x01 is 'true'
     *
     * @param b
     * byte
     * @return String representation of boolean: i.e. true, false
     */
    fun toBoolean(b: Byte): String = if (b.toInt() == 0) "false" else "true"

}
