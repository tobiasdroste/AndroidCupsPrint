package com.tobiasdroste.papercups.app.printers

import androidx.lifecycle.LiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPrinterRepository  @Inject constructor(
    private val localDataSource: PrinterDao
) : PrinterRepository {

    override fun getPrinters(): LiveData<List<Printer>> {
        return localDataSource.getPrinters()
    }

    override fun savePrinter(printer: Printer) {
        localDataSource.insertPrinter(printer)
    }

    override fun deletePrinter(id: String) {
        localDataSource.deletePrinter(id)
    }
}
