package org.example.rebuilt_dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import rebuilt_dashboard.composeapp.generated.resources.Res
import rebuilt_dashboard.composeapp.generated.resources.simulation_board_2026_ampty

data class FieldLocationButton(
    val id: String,
    val initialX: Float,
    val initialY: Float,
    val activeColor: Color
)

data class RowButtons(
    val id: String,
    val initialX: Float,
    val initialY: Float,
    val active: Boolean
)


@Composable
fun App() {
    MaterialTheme {
        var selectedButtonId by remember { mutableStateOf<String?>(null) }

        val rowButtons = remember {
            mutableStateListOf(
                RowButtons("ClimbAlinment", 0.15f, 1.05f, true),
                RowButtons("StaticShooting", 0.25f, 1.05f, false),
                RowButtons("ShootOnMove", 0.35f, 1.05f, false),
                RowButtons("AutoIntake", 0.45f, 1.05f, false),
                RowButtons("S5", 0.55f, 1.05f, false),
                RowButtons("S6", 0.65f, 1.05f, false),
            )
        }

        val fieldButtons = remember {
            listOf(
                FieldLocationButton("L1", 0.15f, 0.10f, Color.Black),
                FieldLocationButton("L2", 0.15f, 0.44f, Color.Black),
                FieldLocationButton("L3", 0.15f, 0.73f, Color.Black),
                FieldLocationButton("R1", 0.30f, 0.10f, Color.Black),
                FieldLocationButton("R2", 0.30f, 0.44f, Color.Black),
                FieldLocationButton("R3", 0.30f, 0.73f, Color.Black)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(0.66f).background(Color.Black)
            ) {
                val boardWidthPx = constraints.maxWidth
                val boardHeightPx = constraints.maxHeight
                val dynamicFieldButtonSize = (maxWidth * 0.08f)
                val dynamicRowButtonSize = (maxWidth * 0.05f)

                Image(
                    painter = painterResource(Res.drawable.simulation_board_2026_ampty),
                    contentDescription = "dashboard background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                rowButtons.forEachIndexed { index, button ->
                    val buttonColor = if (button.active) Color.Cyan else Color.Black

                    Button(
                        modifier = Modifier
                            .size(dynamicRowButtonSize)
                            .offset {
                                IntOffset(
                                    x = (button.initialX * boardWidthPx).toInt(),
                                    y = (button.initialY * boardHeightPx).toInt()
                                )
                            },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            rowButtons[index] = button.copy(active = !button.active)
                        }
                    ) {
                    }
                }

                fieldButtons.forEach { button ->
                    val isSelected = selectedButtonId == button.id
                    val buttonColor = if (isSelected) button.activeColor else Color.Black.copy(alpha = 0.4f)

                    Button(
                        modifier = Modifier
                            .size(dynamicFieldButtonSize)
                            .offset {
                                IntOffset(
                                    x = (button.initialX * boardWidthPx).toInt(),
                                    y = (button.initialY * boardHeightPx).toInt()
                                )
                            },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            selectedButtonId = if (isSelected) null else button.id
                        }
                    ) {}
                }
            }
        }
    }
}