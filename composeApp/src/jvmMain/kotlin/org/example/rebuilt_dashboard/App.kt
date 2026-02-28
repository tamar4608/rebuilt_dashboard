package org.example.rebuilt_dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.wpi.first.math.geometry.Pose2d
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import rebuilt_dashboard.composeapp.generated.resources.Res
import rebuilt_dashboard.composeapp.generated.resources.simulation_board_2026_ampty

// --- DATA CLASSES ---

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

// Updated exact 2026 Field Dimensions
const val FIELD_LENGTH_METERS = 16.54f
const val FIELD_WIDTH_METERS = 8.07f

@Composable
fun App() {
    startClient(ConnectionType.LOCAL)
    MaterialTheme {
        var selectedButtonId by remember { mutableStateOf<String?>(null) }

        var robotState by remember {mutableStateOf(Pose2d())}

        // Start Offset Settings (in meters)
        val startOffsetX = 2.5f
        val startOffsetY = 0.8f

        val poseSubscriber = remember { robotPose }

        // Polling loop (50Hz)
        LaunchedEffect(Unit) {
            while (true) {
                robotState = poseSubscriber.get()
                delay(20)
            }
        }

        val rowButtons = remember {
            mutableStateListOf(
                RowButtons("S1", 0.15f, 1.05f, false),
                RowButtons("S2", 0.25f, 1.05f, false),
                RowButtons("S3", 0.35f, 1.05f, false),
                RowButtons("S4", 0.45f, 1.05f, false),
                RowButtons("S5", 0.55f, 1.05f, false),
                RowButtons("S6", 0.65f, 1.05f, false),
            )
        }

        val fieldButtons = remember {
            listOf(
                FieldLocationButton("L1", 0.15f, 0.10f, Color.White),
                FieldLocationButton("L2", 0.15f, 0.44f, Color.Red),
                FieldLocationButton("L3", 0.15f, 0.73f, Color.Gray),
                FieldLocationButton("R1", 0.30f, 0.10f, Color.Cyan),
                FieldLocationButton("R2", 0.30f, 0.44f, Color.LightGray),
                FieldLocationButton("R3", 0.30f, 0.73f, Color.Yellow)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            val boardPainter = painterResource(Res.drawable.simulation_board_2026_ampty)
            val imgWidth = boardPainter.intrinsicSize.width
            val imgHeight = boardPainter.intrinsicSize.height
            val autoRatio = if (imgWidth > 0 && imgHeight > 0 && !imgWidth.isNaN()) {
                imgWidth / imgHeight
            } else 1.8f

            Box(
                modifier = Modifier
                    .fillMaxSize(0.9f)
                    .aspectRatio(autoRatio)
            ) {

                // 1. Field Background
                Image(
                    painter = boardPainter,
                    contentDescription = "dashboard background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                // 2. THE ROBOT
                // "Other Origin" Math (Assuming Red Alliance Origin at Top-Right)
                // Inverts X so it draws right-to-left. Y goes top-to-bottom.
                val robotXPercent = 1.0f - (robotState.x / FIELD_LENGTH_METERS)
                val robotYPercent = (robotState.y / FIELD_WIDTH_METERS)

                Box(
                    modifier = Modifier
                        .offset(startOffsetX.dp, startOffsetY.dp)
                        .offsetPercentCentered(robotXPercent.toFloat(), robotYPercent.toFloat())
                        .fillMaxWidth(0.035f) // Smaller robot (3.5% of field width)
                        .aspectRatio(1f) // Keep it a perfect square
                        // FRC is counter-clockwise (+), Compose is clockwise (-).
                        // Adding +180 offsets the heading for the "other origin" (facing left)
                        .rotate(-robotState.rotation.rotations.toFloat()*360 + 180f)
                        .background(Color(0x660055FF), RoundedCornerShape(4.dp)) // Translucent base
                        .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        .padding(4.dp) // Gives the arrow some breathing room from the edge
                ) {
                    // Center Navigation Arrow
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Arrow pointing RIGHT (Standard 0° in WPILib odometry before rotation)
                        val path = Path().apply {
                            moveTo(size.width, size.height / 2f) // Nose (Front)
                            lineTo(0f, 0f) // Top wing
                            lineTo(size.width * 0.25f, size.height / 2f) // Inner back indent
                            lineTo(0f, size.height) // Bottom wing
                            close()
                        }
                        drawPath(path, color = Color.Cyan)
                    }
                }

                // 3. ROW BUTTONS
                rowButtons.forEachIndexed { index, button ->
                    val targetColor = if (button.active) Color.Cyan else Color(0xFF2C2C2C)
                    val animatedColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(300))

                    Button(
                        modifier = Modifier
                            .offsetPercent(button.initialX, button.initialY)
                            .fillMaxWidth(0.05f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = animatedColor),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = { rowButtons[index] = button.copy(active = !button.active) }
                    ) {
                        Text(
                            text = button.id,
                            color = if (button.active) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // 4. FIELD BUTTONS
                fieldButtons.forEach { button ->
                    val isSelected = selectedButtonId == button.id

                    val baseColor = if (isSelected) button.activeColor else Color(0xFF2C2C2C)
                    val targetColor = if (isSelected) baseColor else baseColor.copy(alpha = 0.5f)

                    val animatedColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(300))

                    Button(
                        modifier = Modifier
                            .offsetPercent(button.initialX, button.initialY)
                            .fillMaxWidth(0.04f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = animatedColor),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.25f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (isSelected) 6.dp else 0.dp,
                            pressedElevation = 2.dp
                        ),
                        contentPadding = PaddingValues(0.dp),
                        onClick = { selectedButtonId = if (isSelected) null else button.id }
                    ) {
                        Text(
                            text = button.id,
                            color = if (isSelected) {
                                if (targetColor.luminance() > 0.5f) Color.Black else Color.White
                            } else {
                                Color.White.copy(alpha = 0.7f)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// --- COMPOSE NATIVE EXTENSIONS ---

/** Standard top-left placement */
fun Modifier.offsetPercent(xPercent: Float, yPercent: Float) = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(
            x = (constraints.maxWidth * xPercent).toInt(),
            y = (constraints.maxHeight * yPercent).toInt()
        )
    }
}

/** Centered placement for the robot */
fun Modifier.offsetPercentCentered(xPercent: Float, yPercent: Float) = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(
            x = (constraints.maxWidth * xPercent).toInt() - (placeable.width / 2),
            y = (constraints.maxHeight * yPercent).toInt() - (placeable.height / 2)
        )
    }
}

fun Color.luminance(): Float {
    return 0.2126f * red + 0.7152f * green + 0.0722f * blue
}