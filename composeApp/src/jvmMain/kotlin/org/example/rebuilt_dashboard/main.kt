package org.example.rebuilt_dashboard

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Rebuilt Dashboard",
    ) {
        App()
    }
}