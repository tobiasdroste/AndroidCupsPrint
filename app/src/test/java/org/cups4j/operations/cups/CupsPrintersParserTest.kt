package org.cups4j.operations.cups

import ch.ethz.vppserver.ippclient.IppResult
import ch.ethz.vppserver.schema.ippclient.Attribute
import ch.ethz.vppserver.schema.ippclient.AttributeGroup
import ch.ethz.vppserver.schema.ippclient.AttributeValue
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.net.URL

class CupsPrintersParserTest {

    private fun attr(name: String, value: String?): Attribute {
        val a = Attribute()
        a.name = name
        if (value != null) {
            val av = AttributeValue()
            av.value = value
            a.attributeValue.add(av)
        }
        return a
    }

    @Test
    fun parsePrinters_parsesValidGroup() {
        val group = AttributeGroup().apply {
            tagName = "printer-attributes-tag"
            attribute.add(attr("printer-uri-supported", "ipps://printer.local:631/printers/HP"))
            attribute.add(attr("printer-name", "HP LaserJet"))
            attribute.add(attr("printer-location", "Office"))
            attribute.add(attr("printer-info", "Fast printer"))
        }
        val result = IppResult().apply { attributeGroupList = listOf(group) }

        val printers = CupsPrintersParser.parsePrinters(result, "http", "Unknown")

        assertThat(printers).hasSize(1)
        val p = printers[0]
        assertThat(p.name).isEqualTo("HP LaserJet")
        assertThat(p.location).isEqualTo("Office")
        assertThat(p.description).isEqualTo("Fast printer")
        assertThat(p.printerURL).isEqualTo(URL("http://printer.local:631/printers/HP"))
    }

    @Test
    fun parsePrinters_handlesMissingOptionalAttributes() {
        val group = AttributeGroup().apply {
            tagName = "printer-attributes-tag"
            attribute.add(attr("printer-uri-supported", "ipp://host/printers/Queue"))
            attribute.add(attr("printer-name", null))
        }
        val result = IppResult().apply { attributeGroupList = listOf(group) }

        val printers = CupsPrintersParser.parsePrinters(result, "http", "Unknown")

        assertThat(printers).hasSize(1)
        val p = printers[0]
        assertThat(p.name).isEqualTo("Unknown")
        assertThat(p.location).isNull()
        assertThat(p.description).isNull()
        assertThat(p.printerURL).isEqualTo(URL("http://host/printers/Queue"))
    }

    @Test
    fun parsePrinters_malformedUriThrows() {
        val group = AttributeGroup().apply {
            tagName = "printer-attributes-tag"
            attribute.add(attr("printer-uri-supported", "ht!tp://bad^uri"))
        }
        val result = IppResult().apply { attributeGroupList = listOf(group) }

        assertThrows(Exception::class.java) {
            CupsPrintersParser.parsePrinters(result, "http", "Unknown")
        }
    }
}
