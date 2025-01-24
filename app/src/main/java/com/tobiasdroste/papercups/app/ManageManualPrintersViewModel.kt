package com.tobiasdroste.papercups.app

import androidx.lifecycle.ViewModel
import com.tobiasdroste.papercups.app.printers.PrinterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageManualPrintersViewModel  @Inject constructor(
    private val printerRepository: PrinterRepository): ViewModel() {

}