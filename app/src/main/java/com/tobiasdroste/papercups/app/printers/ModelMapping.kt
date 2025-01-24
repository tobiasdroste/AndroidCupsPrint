/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tobiasdroste.papercups.app.printers

import com.tobiasdroste.papercups.app.printers.models.LocalPrinter
import com.tobiasdroste.papercups.app.printers.models.Printer


/**
 * Data model mapping extension functions. There are three model types:
 *
 * - Printer: External model exposed to other layers in the architecture.
 * Obtained using `toExternal`.
 *
 * - NetworkPrinter: Internal model used to represent a printer from the network. Obtained using
 * `toNetwork`.
 *
 * - LocalPrinter: Internal model used to represent a printer stored locally in a database. Obtained
 * using `toLocal`.
 *
 */

// External to local
fun Printer.toLocal() = LocalPrinter(
    id = id,
    name = name,
    url = url,
)

fun List<Printer>.toLocal() = map(Printer::toLocal)

// Local to External
fun LocalPrinter.toExternal() = Printer(
    id = id,
    name = name,
    url = url,
)

// Note: JvmName is used to provide a unique name for each extension function with the same name.
// Without this, type erasure will cause compiler errors because these methods will have the same
// signature on the JVM.
@JvmName("localToExternal")
fun List<LocalPrinter>.toExternal() = map(LocalPrinter::toExternal)
