package com.tobiasdroste.papercups.app.printers

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.tobiasdroste.papercups.app.printers.models.LocalPrinter

@Dao
interface PrinterDao {

    @Query("SELECT * FROM printers")
    suspend fun getPrinters(): List<LocalPrinter>

    @Query("SELECT * FROM printers")
    fun observePrinters(): LiveData<List<LocalPrinter>>

    @Upsert
    suspend fun insertPrinter(localPrinter: LocalPrinter)

    @Query("DELETE FROM printers WHERE id = :id")
    suspend fun deletePrinter(id: Int)
}
