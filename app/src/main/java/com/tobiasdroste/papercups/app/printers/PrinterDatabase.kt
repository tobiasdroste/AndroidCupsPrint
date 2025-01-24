package com.tobiasdroste.papercups.app.printers

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tobiasdroste.papercups.app.printers.Printer

@Database(entities = [Printer::class], version = 1, exportSchema = true)
abstract class PrinterDatabase : RoomDatabase() {
    abstract fun printerDao(): PrinterDao
}
