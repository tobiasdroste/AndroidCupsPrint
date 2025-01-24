package com.tobiasdroste.papercups.app.printers.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "printers")
data class LocalPrinter(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var name: String,
    var url: String) {
}