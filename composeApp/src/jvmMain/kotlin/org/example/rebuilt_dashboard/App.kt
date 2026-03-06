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



internal enum class Alliance {
    RED, BLUE
}

internal object DashboardData {
    private val isRedSubscriber = NetworkTableInstance.getDefault().getTable("/AdvantageKit/RealOutputs")
        .getBooleanTopic("IS_RED")
        .subscribe(true)

    val currentAlliance: Alliance
        get() = if (isRedSubscriber.get()) Alliance.RED else Alliance.BLUE



     internal val _isCompetition = NetworkTableInstance.getDefault().getTable("AdvantageKit/DriverStation/FMSAttached")
            .getBooleanTopic("_isCompetition")
            .subscribe(false)

        val isCompetition: Boolean
            get() = _isCompetition.get()

    val robotPoseSubscriber = NetworkTableInstance.getDefault().getTable("/AdvantageKit/RealOutputs/Odometry")
        .getStructTopic("Robot", edu.wpi.first.math.geometry.Pose2d.struct)
        .subscribe(edu.wpi.first.math.geometry.Pose2d())
    val robotPoseFlow = kotlinx.coroutines.flow.MutableStateFlow(edu.wpi.first.math.geometry.Pose2d())

    fun updatePosePeriodically() {
        val pose = robotPoseSubscriber.get()
        robotPoseFlow.value = pose
    }
}

data class FieldLocationButton(
    val id: String,
    val initialX: Float,
    val initialY: Float,
)

data class RowButtons(
    val id: String,
    val initialX: Float,
    val initialY: Float,
    var active: Boolean

)




@Composable
fun App() {
    startClient(ConnectionType.LOCAL)
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

        fun updateRowButtonInNT(id: String, isActive: Boolean) {
            NetworkTableInstance.getDefault()
                .getTable("AdvantageKit/DriverDashboard/RowButtons/$id")
                .getBooleanTopic("$id")
                .publish()
                .set(isActive)
        }

        val fieldButtonsBase = remember {
            listOf(
                FieldLocationButton("Upper Corner", 0.015f, 0.025f,),
                FieldLocationButton("Climb", 0.015f, 0.46f),
                FieldLocationButton("Lower Corner", 0.015f, 0.83f),
                FieldLocationButton("Upper Trench", 0.245f, 0.025f),
                FieldLocationButton("Upper Bumper", 0.245f, 0.23f),
                FieldLocationButton("Hub", 0.245f, 0.43f),
                FieldLocationButton("Lower Bumper", 0.245f, 0.63f),
                FieldLocationButton("Lower Trench", 0.245f, 0.83f)
            )
        }

        //נו כל ההגדרות המציקות של לעדכן את הרובוט והאם השוטינג סטטי קיים ומידות וכל מיני שטויות קיצר זה פה
        val isStaticShootingActive = rowButtons.find { it.id == "Static Shooting" }?.active ?: false
        val robotPose by produceState(initialValue = edu.wpi.first.math.geometry.Pose2d()) {
            while (true) {
                value = DashboardData.robotPoseSubscriber.get()
                kotlinx.coroutines.delay(20)
            }
        }
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

                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center) {

                            // הטיימר הראשי
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

                            // האקטיב המציק של אדר
                            val statusText = when {
                                timeInMillis > 140000L -> "Auto ends in: ${timeInMillis / 1000 - 140}s"
                                timeInMillis in 130001L..140000L -> "Everyone active ends in: ${timeInMillis / 1000 - 130}s"
                                timeInMillis < 30000L -> "EndGame ends in: ${timeInMillis / 1000}s"
                                timeUntilNextShift > 0 -> if (isOurHubActive) "Inactive in: ${timeUntilNextShift}s" else "Active in: ${timeUntilNextShift}s"
                                else -> ""
                            }

                            val statusColor = when {
                                timeInMillis > 130000L || isOurHubActive -> Color.Green.copy(alpha = 0.8f)
                                else -> Color.Yellow.copy(alpha = 0.8f)
                            }

                            if (statusText.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(12.dp)) // רווח מהטיימר
                                Text(
                                    text = statusText,
                                    color = statusColor,
                                    fontSize = (dynamicTimerFontSize.value * 0.35f).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    BoxWithConstraints(
                        modifier = Modifier.align(Alignment.Center).fillMaxSize(0.70f).background(Color.Black)
                    ) {
                        val boardWidthPx = constraints.maxWidth
                        val boardHeightPx = constraints.maxHeight
                        val fieldBtnWidth = maxWidth * 0.08f
                        val fieldBtnHeight = maxHeight * 0.15f

                        // פה הכפתורי שורה :) (כולל כזה להעלים בתחרות בלה בלה לשנות גדלים בלה בלה)
                        val visibleButtons = rowButtons.filter { !isInMatch || it.id != "Shooting Calibration" }
                        val rowBtnWidth = maxWidth * (if (isInMatch) 0.18f else 0.17f)
                        val rowBtnHeight = maxHeight * 0.15f

                        Image(
                            painter = painterResource(Res.drawable.field_2026_RB),
                            contentDescription = "field",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        // פה הכפתורי מגרש (כולל הסידור מיקום של הטיפוס בברית הכחולה)
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
                                    onClick = { selectedButtonId = if (isSelected) null else button.id
                                        NetworkTableInstance.getDefault()
                                            .getTable("AdvantageKit/DriverDashboard/shootLocation")
                                            .getStringTopic("shootLocation")
                                            .publish().set(selectedButtonId ?: "None")

                                    }
                                )
                            }
                        }

                        // יאי הפסקתי להתעצל וסידרתי כפתורי שורה סימטריים כשמבוטל הכפתור החמישי ועושים עליו חרם
                        visibleButtons.forEachIndexed { index, button ->
                            val spacing = 1f / (visibleButtons.size + 1)
                            val symX = (index + 1) * spacing - (rowBtnWidth.value / maxWidth.value / 2)
                            val finalX = if (isInMatch) symX else button.initialX
                            LaunchedEffect(button.active) {
                                updateRowButtonInNT(button.id, button.active)
                            }

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

                        // אחלה רובוט
                        val rawXPercent = robotPose.x.toFloat() / fieldWidthMeters
                        val robotXPercent = 1.0f - rawXPercent // הופך את צד שמאל לימין כי האפליקציה השתגעה והיא שונאת את הטבלה (יאי אנחנו באותה דעה)
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
                                modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = robotPose.rotation.degrees.toFloat() + 90f),
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

