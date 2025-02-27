package com.tobiasdroste.papercups.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobiasdroste.papercups.app.printers.PrinterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageManualPrintersViewModel  @Inject constructor(
    private val printerRepository: PrinterRepository): ViewModel() {

    fun removePrinter(id: Int) = viewModelScope.launch {
        printerRepository.deletePrinter(id)
    }

    val printers = printerRepository.observePrinters()

}