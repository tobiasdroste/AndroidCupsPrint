package com.tobiasdroste.papercups.app.printers

import androidx.lifecycle.LiveData
import com.tobiasdroste.papercups.app.printers.models.Printer

interface PrinterRepository {
    suspend fun getPrinters(): List<Printer>
    fun observePrinters(): LiveData<List<Printer>>
    suspend fun savePrinter(printer: Printer)
    suspend fun deletePrinter(id: Int)
}
