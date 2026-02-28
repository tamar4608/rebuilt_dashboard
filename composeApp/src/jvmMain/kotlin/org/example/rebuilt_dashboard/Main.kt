package org.example.rebuilt_dashboard

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import edu.wpi.first.networktables.NetworkTableInstance

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Rebuilt Dashboard",
    ) {
        App()
    }
}