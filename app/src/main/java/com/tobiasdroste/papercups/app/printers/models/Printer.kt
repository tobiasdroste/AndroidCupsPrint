package com.tobiasdroste.papercups.app.printers.models

data class Printer(
    val id: Int = 0,
    val name: String,
    val url: String
) {
    constructor(name: InputPrinter.Name, url: String) : this(0, name.value, url)
}
