package org.cups4j.operations.cups

import android.content.Context
import ch.ethz.vppserver.ippclient.IppResult
import ch.ethz.vppserver.schema.ippclient.Attribute
import ch.ethz.vppserver.schema.ippclient.AttributeGroup
import ch.ethz.vppserver.schema.ippclient.AttributeValue
import com.google.common.truth.Truth.assertThat
import org.cups4j.CupsPrinter
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.net.URL

@RunWith(MockitoJUnitRunner::class)
class CupsGetPrintersOperationTest {

    private class TestableOp(context: Context) : CupsGetPrintersOperation(context) {
        var lastUrl: URL? = null
        var lastParameters: Map<String, String>? = null
        var nextResult: IppResult? = null
        override fun request(url: URL, parameters: Map<String, String>): IppResult? {
            lastUrl = url
            lastParameters = parameters
            return nextResult
        }
    }

    @Test
    fun returnsEmptyList_whenRequestReturnsNull() {
        val context = Mockito.mock(Context::class.java)
        val op = TestableOp(context)
        op.nextResult = null

        val url = URL("http://example.com:631")
        val printers = op.getPrinters(url, "/printers")

        assertThat(printers).isEmpty()
        assertThat(op.lastUrl.toString()).isEqualTo("http://example.com:631/printers")
        // ensure requested-attributes are always set
        assertThat(op.lastParameters?.get("requested-attributes")).isNotNull()
    }

    @Test
    fun parsesPrinters_correctly_andNormalizesProtocol() {
        val context = Mockito.mock(Context::class.java)
        val op = TestableOp(context)
        op.nextResult = buildIppResult(
            uri = "ipps://print.example.org:631/printers/Office",
            name = "Office",
            location = "Floor 1",
            info = "Main printer"
        )

        val baseUrl = URL("http://print.example.org:631")
        val printers = op.getPrinters(baseUrl, "/printers")

        assertThat(printers).hasSize(1)
        val p: CupsPrinter = printers[0]
        assertThat(p.name).isEqualTo("Office")
        assertThat(p.location).isEqualTo("Floor 1")
        assertThat(p.description).isEqualTo("Main printer")
        // protocol normalized to base url protocol (http)
        assertThat(p.printerURL.protocol).isEqualTo("http")
        assertThat(p.printerURL.host).isEqualTo("print.example.org")
        assertThat(p.printerURL.path).isEqualTo("/printers/Office")
    }

    @Test
    fun paramsContain_firstName_and_validLimit_only() {
        val context = Mockito.mock(Context::class.java)
        val op = TestableOp(context)
        op.nextResult = buildIppResult(
            uri = "ipp://host/printers/p1",
            name = "p1",
            location = null,
            info = null
        )

        val baseUrl = URL("http://host")
        op.getPrinters(baseUrl, "/printers", firstName = "abc", limit = 5)

        val params = op.lastParameters
        assertThat(params?.get("first-printer-name")).isEqualTo("abc")
        assertThat(params?.get("limit")).isEqualTo("5")

        // invalid limit is ignored
        op.getPrinters(baseUrl, "/printers", firstName = null, limit = 0)
        val params2 = op.lastParameters
        assertThat(params2?.containsKey("limit")).isFalse()
    }

    @Test
    fun defaultsName_whenMissing() {
        val context = Mockito.mock(Context::class.java)
        val op = TestableOp(context)
        op.nextResult = buildIppResult(
            uri = "ipp://host/printers/unknown",
            name = null,
            location = null,
            info = null
        )

        val printers = op.getPrinters(URL("http://host"), "/printers")
        assertThat(printers).hasSize(1)
        assertThat(printers[0].name).isEqualTo(DEFAULT_PRINTER_NAME)
    }

    private fun buildIppResult(
        uri: String?,
        name: String?,
        location: String?,
        info: String?
    ): IppResult {
        fun attr(name: String, value: String?): Attribute {
            val a = Attribute()
            a.name = name
            if (value != null) {
                val av = AttributeValue()
                av.value = value
                a.attributeValue.add(av)
            }
            return a
        }

        val group = AttributeGroup().apply {
            tagName = "printer-attributes-tag"
            attribute.add(attr("printer-uri-supported", uri))
            attribute.add(attr("printer-name", name))
            attribute.add(attr("printer-location", location))
            attribute.add(attr("printer-info", info))
        }

        return IppResult().apply {
            attributeGroupList = listOf(group)
            httpStatusResponse = "200"
            ippStatusResponse = "successful-ok"
        }
    }
}
