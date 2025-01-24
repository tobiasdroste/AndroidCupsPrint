package com.tobiasdroste.papercups.app.printers

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.tobiasdroste.papercups.app.printers.models.Printer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPrinterRepository  @Inject constructor(
    private val localDataSource: PrinterDao
) : PrinterRepository {

    override suspend fun getPrinters(): List<Printer> {
        return localDataSource.getPrinters().toExternal()
    }

    override fun observePrinters(): LiveData<List<Printer>> {
        return localDataSource.observePrinters().map { it.toExternal() }
    }

    override suspend fun savePrinter(printer: Printer) {
        localDataSource.insertPrinter(printer.toLocal())
    }

    override suspend fun deletePrinter(id: Int) {
        localDataSource.deletePrinter(id)
    }
}
