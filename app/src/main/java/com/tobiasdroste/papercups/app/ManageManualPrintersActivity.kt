package com.tobiasdroste.papercups.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.tobiasdroste.papercups.R
import com.tobiasdroste.papercups.app.printers.models.Printer
import com.tobiasdroste.papercups.databinding.ActivityManageManualPrintersBinding
import com.tobiasdroste.papercups.databinding.ManagePrintersListItemBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageManualPrintersActivity : AppCompatActivity() {

    private val viewModel: ManageManualPrintersViewModel by viewModels()

    private lateinit var binding: ActivityManageManualPrintersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageManualPrintersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ManualPrintersAdapter(this, R.layout.manage_printers_list_item, mutableListOf())

        viewModel.printers.observe(this) {
            adapter.clear()
            adapter.addAll(it)

            adjustFabBasedOnPrinterCount(it.size)
        }

        // Setup adapter with click to remove
        binding.managePrintersList.adapter = adapter
        binding.managePrintersList.setOnItemClickListener { _, _, position, _ ->
            adapter.getItem(position)?.let { printer ->
                showDeleteConfirmationDialog(printer.name, printer.id)
            }
        }

        binding.floatingActionButton.setOnClickListener {
            val startAddPrintersActivityIntent = Intent(
                this,
                AddPrintersActivity::class.java
            )
            startActivity(startAddPrintersActivityIntent)
        }

        val defaultMargin = 16.toPixels()

        ViewCompat.setOnApplyWindowInsetsListener(binding.floatingActionButton) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. This solution sets
            // only the bottom, left, and right dimensions, but you can apply whichever
            // insets are appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left + defaultMargin
                bottomMargin = insets.bottom + defaultMargin
                rightMargin = insets.right + defaultMargin
            }

            // Return CONSUMED if you don't want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.managePrintersHelp) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left + defaultMargin,
                top = bars.top + defaultMargin,
                right = bars.right + defaultMargin,
                bottom = bars.bottom + defaultMargin,
            )
            WindowInsetsCompat.CONSUMED
        }

    }

    fun Int.toPixels() = (this * resources.displayMetrics.density).toInt()

    private fun adjustFabBasedOnPrinterCount(numPrinters: Int) {
        if (numPrinters == 0) {
            binding.floatingActionButton.extend()
        } else {
            binding.floatingActionButton.shrink()
        }
    }

    private fun showDeleteConfirmationDialog(printerName: String, printerId: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_printer_title)
            .setMessage(getString(R.string.delete_printer_message, printerName))
            .setPositiveButton(R.string.delete_button) { _, _ ->
                viewModel.removePrinter(printerId)
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    private class ManualPrinterInfoViews(var name: TextView, var url: TextView)

    private class ManualPrintersAdapter(
        context: Context,
        @LayoutRes resource: Int,
        objects: List<Printer>
    ) : ArrayAdapter<Printer>(context, resource, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = when (convertView) {
                null -> {
                    val binding = ManagePrintersListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    binding.root.tag =
                        ManualPrinterInfoViews(binding.manualPrinterName, binding.manualPrinterUrl)
                    binding.root
                }

                else -> convertView
            }

            val views = view.tag as ManualPrinterInfoViews

            val info = getItem(position)
            if (info != null) {
                views.name.text = info.name
                views.url.text = info.url
            } else {
                throw IllegalStateException("Manual printers list can't have invalid items")
            }

            return view
        }
    }
}
