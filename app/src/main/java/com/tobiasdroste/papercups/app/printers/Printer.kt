package com.tobiasdroste.papercups.app.printers

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "printers")
data class Printer(
    @PrimaryKey val id: String,
    val title: String,
    val description: String
)
