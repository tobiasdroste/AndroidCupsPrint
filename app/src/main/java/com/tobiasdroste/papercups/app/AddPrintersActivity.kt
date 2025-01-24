package com.tobiasdroste.papercups.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tobiasdroste.papercups.app.printers.models.InputPrinter
import com.tobiasdroste.papercups.app.printers.models.InputPrinter.Name
import com.tobiasdroste.papercups.app.printers.models.InputPrinter.PrinterMappingResult.InputField
import com.tobiasdroste.papercups.app.printers.models.Printer
import com.tobiasdroste.papercups.databinding.AddPrintersBinding
import com.tobiasdroste.papercups.printservice.CupsPrinterDiscoverySession
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

/**
 * Called when the system needs to manually add a printer
 */
@AndroidEntryPoint
class AddPrintersActivity : AppCompatActivity() {
    private val viewModel: AddPrintersViewModel by viewModels()
    private lateinit var binding: AddPrintersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddPrintersBinding.inflate(layoutInflater)
        binding.addPrinter.setOnClickListener { addPrinter() }
        binding.searchPrinters.setOnClickListener { searchPrinters() }
        setContentView(binding.root)
    }

    /**
     * Called when the button will be clicked
     */
    private fun addPrinter() {
        val url = binding.addUrl.text.toString()
        val name = binding.addName.text.toString()

        val inputPrinter = InputPrinter(Name(name), url)

        when (val mappingResult = inputPrinter.toPrinter()) {
            is InputPrinter.PrinterMappingResult.Error -> {
                when (mappingResult.field) {
                    InputField.URL -> binding.addUrl.error = mappingResult.message
                    InputField.NAME -> binding.addName.error = mappingResult.message
                }
            }
            is InputPrinter.PrinterMappingResult.Success -> viewModel.addPrinter(mappingResult.printer)
        }

        // Allow the system to process the new printer addition before we get back to the list of printers
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 200)
    }

    private fun searchPrinters() {
        lifecycleScope.launch {
            val foundPrinters = withContext(Dispatchers.IO) {
                val httpPrinter = async { searchPrinters("http") }
                val httpsPrinter = async { searchPrinters("https") }
                httpPrinter.await() + httpsPrinter.await()
            }

            if (foundPrinters.isEmpty()) {
                MaterialAlertDialogBuilder(this@AddPrintersActivity).setTitle("Nothing found")
                    .setMessage("Couldn't find any printers using this IP / hostname.")
                    .setPositiveButton("OK") { _, _ -> }
            } else {
                MaterialAlertDialogBuilder(this@AddPrintersActivity).setTitle("Found ${foundPrinters.size} printers")
                    .setMessage("Do you want to add them?")
                    .setCancelable(true)
                    .setPositiveButton("Add") { _, _ ->
                        viewModel.addPrinters(foundPrinters)
                        lifecycleScope.launch {
                            CupsPrinterDiscoverySession.currentSession?.addManualPrinters()
                        }
                        finish()
                    }.setNegativeButton("Cancel") { _, _ ->
                        finish()
                    }
                    .show()
            }
        }
    }

    /**
     * Will search for printers at the scheme://xxx/printers/ URL
     *
     * @param scheme The target scheme, http or https
     * @return found printers
     */
    private fun searchPrinters(scheme: String): List<Printer> {
        var urlConnection: HttpURLConnection? = null
        val sb = StringBuilder()
        var server = binding.addServerIp.text.toString()
        if (!server.contains(":")) {
            server += ":631"
        }
        val baseHost = "$scheme://$server"
        val baseUrl = "$baseHost/printers/"
        try {
            urlConnection = URL(baseUrl).openConnection() as HttpURLConnection
            val isw = InputStreamReader(urlConnection.inputStream)
            var data = isw.read()
            while (data != -1) {
                val current = data.toChar()
                sb.append(current)
                data = isw.read()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        } finally {
            urlConnection?.disconnect()
        }

        /*
         * 1: URL
         * 2: Name
         * 3: Description
         * 4: Location
         * 5: Make and model
         * 6: Current state
         * pattern matching fields:                       1          2                  3               4                5              6
         */
        val p =
            Pattern.compile("<TR><TD><A HREF=\"([^\"]+)\">([^<]*)</A></TD><TD>([^<]*)</TD><TD>([^<]*)</TD><TD>([^<]*)</TD><TD>([^<]*)</TD></TR>\n")
        val matcher = p.matcher(sb)
        var url: String
        var name: String

        val foundPrinters = mutableListOf<Printer>()
        while (matcher.find()) {
            val path = matcher.group(1)
            if (path != null) {
                url = if (path.startsWith("/")) {
                    baseHost + path
                } else {
                    baseUrl + path
                }
                name = matcher.group(3) ?: "Unnamed"
                when (val mappingResult = InputPrinter(Name(name), url).toPrinter()) {
                    is InputPrinter.PrinterMappingResult.Error -> {
                        Timber.e("Error while saving printer from search on $url: ${mappingResult.message}")
                    }

                    is InputPrinter.PrinterMappingResult.Success -> foundPrinters.add(mappingResult.printer)
                }
            }
        }
        return foundPrinters
    }
}
