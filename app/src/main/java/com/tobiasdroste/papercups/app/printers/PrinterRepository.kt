package com.tobiasdroste.papercups.app.printers

import androidx.lifecycle.LiveData

interface PrinterRepository {
    fun getPrinters(): LiveData<List<Printer>>
    fun savePrinter(printer: Printer)
    fun deletePrinter(id: String)
}
