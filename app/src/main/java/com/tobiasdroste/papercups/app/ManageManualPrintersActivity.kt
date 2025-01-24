package com.tobiasdroste.papercups.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
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
            adapter.getItem(position)?.let { viewModel.removePrinter(it.id) }
        }

        binding.floatingActionButton.setOnClickListener {
            val startAddPrintersActivityIntent = Intent(
                this,
                AddPrintersActivity::class.java
            )
            startActivity(startAddPrintersActivityIntent)
        }
    }

    private fun adjustFabBasedOnPrinterCount(numPrinters: Int) {
        if (numPrinters == 0) {
            binding.floatingActionButton.extend()
        } else {
            binding.floatingActionButton.shrink()
        }
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
