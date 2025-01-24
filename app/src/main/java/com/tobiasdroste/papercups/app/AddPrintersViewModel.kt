package com.tobiasdroste.papercups.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobiasdroste.papercups.app.printers.models.Printer
import com.tobiasdroste.papercups.app.printers.PrinterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPrintersViewModel  @Inject constructor(
    private val printerRepository: PrinterRepository): ViewModel() {

    fun addPrinter(printer: Printer) = viewModelScope.launch {
        printerRepository.savePrinter(printer)
    }

    fun addPrinters(printers: List<Printer>) = viewModelScope.launch {
        printerRepository.savePrinters(printers)
    }

}