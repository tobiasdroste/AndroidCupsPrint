package com.tobiasdroste.papercups.app.printers

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PrinterDao {
    @Query("SELECT * FROM printers")
    fun getPrinters(): LiveData<List<Printer>>

    @Insert
    fun insertPrinter(printer: Printer)

    @Query("DELETE FROM printers WHERE id = :id")
    fun deletePrinter(id: String)
}
