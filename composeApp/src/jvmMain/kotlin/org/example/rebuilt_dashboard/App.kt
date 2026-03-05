package org.example.rebuilt_dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.wpi.first.networktables.NetworkTableInstance
import org.jetbrains.compose.resources.painterResource
import rebuilt_dashboard.composeapp.generated.resources.Res
import rebuilt_dashboard.composeapp.generated.resources.arrow_right
import rebuilt_dashboard.composeapp.generated.resources.field_2026_RB


// --- Logic & Enums ---

internal enum class Alliance {
    RED, BLUE
}

internal object DashboardData {
    private val ntInstance = NetworkTableInstance.getDefault()
    private val isRedSubscriber = ntInstance.getTable("/AdvantageKit/RealOutputs")
        .getBooleanTopic("IS_RED")
        .subscribe(true)

    val currentAlliance: Alliance
        get() = if (isRedSubscriber.get()) Alliance.RED else Alliance.BLUE



     internal val _isCompetition = ntInstance.getTable("AdvantageKit/DriverStation/FMSAttached")
            .getBooleanTopic("_isCompetition")
            .subscribe(false) //די כבר להתעצל תסדרי שאם זה true ויש רק 4 כפתורים - המיקומים שלהם ישתנו ויהיו סימטריים :)

        val isCompetition: Boolean
            get() = _isCompetition.get()

    val robotPoseSubscriber = ntInstance.getTable("/AdvantageKit/RealOutputs/Odometry")
        .getStructTopic("Robot", edu.wpi.first.math.geometry.Pose2d.struct)
        .subscribe(edu.wpi.first.math.geometry.Pose2d())


}

data class FieldLocationButton(
    val id: String,
    val initialX: Float,
    val initialY: Float
)

data class RowButtons(
    val id: String,
    val initialX: Float,
    val initialY: Float,
    var active: Boolean
)



// ... (אותם Imports ו-Logic) ...

@Composable
fun App() {
    MaterialTheme {
        var selectedButtonId by remember { mutableStateOf<String?>(null) }
        val currentAlliance = DashboardData.currentAlliance
        val timeInMillis by MatchTime.matchTime.collectAsState()
        val isInMatch = DashboardData.isCompetition

        val rowButtons = remember {
            mutableStateListOf(
                RowButtons("Climb Alignment", 0.09f, 1.02f, true),
                RowButtons("Static Shooting", 0.27f, 1.02f, false),
                RowButtons("Shoot On Move", 0.45f, 1.02f, false),
                RowButtons("Auto Intake", 0.63f, 1.02f, false),
                RowButtons("Shooting Calibration", 0.81f, 1.02f, false),
            )
        }

        val fieldButtonsBase = remember {
            listOf(
                FieldLocationButton("Upper Corner", 0.015f, 0.025f),
                FieldLocationButton("Climb", 0.015f, 0.46f),
                FieldLocationButton("Lower Corner", 0.015f, 0.83f),
                FieldLocationButton("Upper Trench", 0.245f, 0.025f),
                FieldLocationButton("Upper Bumper", 0.245f, 0.23f),
                FieldLocationButton("Hub", 0.245f, 0.43f),
                FieldLocationButton("Lower Bumper", 0.245f, 0.63f),
                FieldLocationButton("Lower Trench", 0.245f, 0.83f)
            )
        }

        val isStaticShootingActive = rowButtons.find { it.id == "Static Shooting" }?.active ?: false
        val robotPose = DashboardData.robotPoseSubscriber.get()
        val fieldWidthMeters = 16.54f
        val fieldHeightMeters = 8.07f

        Box(
            modifier = Modifier.fillMaxSize().background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val dynamicTimerFontSize = (maxWidth.value * 0.05f).sp

                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = formatTime(timeInMillis),
                            color = Color.White,
                            fontSize = dynamicTimerFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.background(
                                Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ).padding(horizontal = 20.dp, vertical = 5.dp)
                        )
                    }

                    BoxWithConstraints(
                        modifier = Modifier.align(Alignment.Center).fillMaxSize(0.70f).background(Color.Black)
                    ) {
                        val boardWidthPx = constraints.maxWidth
                        val boardHeightPx = constraints.maxHeight
                        val fieldBtnWidth = maxWidth * 0.08f
                        val fieldBtnHeight = maxHeight * 0.15f

                        // כפתורי שורה - חישוב גדלים ומיקומים
                        val visibleButtons = rowButtons.filter { !isInMatch || it.id != "Shooting Calibration" }
                        val rowBtnWidth = maxWidth * (if (isInMatch) 0.18f else 0.17f)
                        val rowBtnHeight = maxHeight * 0.15f

                        Image(
                            painter = painterResource(Res.drawable.field_2026_RB),
                            contentDescription = "field",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        // כפתורי מגרש
                        if (isStaticShootingActive) {
                            fieldButtonsBase.forEach { button ->
                                val isSelected = selectedButtonId == button.id
                                val finalX = if (currentAlliance == Alliance.RED) button.initialX else 1.0f - button.initialX - (fieldBtnWidth.value / maxWidth.value)
                                val finalY = if (button.id == "Climb" && currentAlliance == Alliance.BLUE) button.initialY - 0.067f else button.initialY

                                AllianceStyledButton(
                                    modifier = Modifier
                                        .size(width = fieldBtnWidth, height = fieldBtnHeight)
                                        .offset { IntOffset((finalX * boardWidthPx).toInt(), (finalY * boardHeightPx).toInt()) },
                                    isSelected = isSelected,
                                    alliance = currentAlliance,
                                    label = "",
                                    boardWidthPx = boardWidthPx.toFloat(),
                                    onClick = { selectedButtonId = if (isSelected) null else button.id }
                                )
                            }
                        }

                        // כפתורי שורה סימטריים
                        visibleButtons.forEachIndexed { index, button ->
                            val spacing = 1f / (visibleButtons.size + 1)
                            val symX = (index + 1) * spacing - (rowBtnWidth.value / maxWidth.value / 2)
                            val finalX = if (isInMatch) symX else button.initialX

                            AllianceStyledButton(
                                modifier = Modifier
                                    .size(width = rowBtnWidth, height = rowBtnHeight)
                                    .offset { IntOffset((finalX * boardWidthPx).toInt(), (button.initialY * boardHeightPx).toInt()) },
                                isSelected = button.active,
                                alliance = currentAlliance,
                                label = button.id,
                                boardWidthPx = boardWidthPx.toFloat(),
                                onClick = {
                                    val originalIndex = rowButtons.indexOfFirst { it.id == button.id }
                                    if (originalIndex != -1) rowButtons[originalIndex] = button.copy(active = !button.active)
                                }
                            )
                        }

                        // רובוט (מחוץ ללולאה!)
                        val rawXPercent = robotPose.x.toFloat() / fieldWidthMeters
                        val robotXPercent = 1.0f - rawXPercent // הופך את צד שמאל לימין
                        val robotYPercent = robotPose.y.toFloat()  / fieldHeightMeters

                        val robotSizePx = boardWidthPx * 0.06f
                        val robotSizeDp = with(LocalDensity.current) { robotSizePx.toDp() }

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = (robotXPercent * boardWidthPx - robotSizePx / 2).toInt(),
                                        y = (robotYPercent * boardHeightPx - robotSizePx / 2).toInt()
                                    )
                                }
                                .size(robotSizeDp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = robotPose.rotation.degrees.toFloat() + 135f),
                                shape = RoundedCornerShape(8.dp),
                                color = (if (currentAlliance == Alliance.RED) Color(0xFFE53935) else Color(0xFF1E88E5)).copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.8f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(Res.drawable.arrow_right),
                                        contentDescription = "Robot",
                                        tint = Color.White,
                                        modifier = Modifier.fillMaxSize(0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AllianceStyledButton(
    modifier: Modifier,
    isSelected: Boolean,
    alliance: Alliance,
    label: String,
    boardWidthPx: Float,
    onClick: () -> Unit
) {
    val baseColor = if (alliance == Alliance.RED) Color(0xFFE53935) else Color(0xFF1E88E5)
    val activeColor = if (alliance == Alliance.RED) Color(0xFFFF7043) else Color(0xFF87CEEB)
    val backgroundColor = if (isSelected) activeColor.copy(alpha = 0.8f) else baseColor.copy(alpha = 0.35f)
    val borderColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f)

    // פונט דינמי - בערך 1.2% מרוחב המגרש
    val dynamicFontSize = (boardWidthPx * 0.012f).coerceAtLeast(8f).sp

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = dynamicFontSize,
                fontWeight = FontWeight.Bold,
                softWrap = true,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

