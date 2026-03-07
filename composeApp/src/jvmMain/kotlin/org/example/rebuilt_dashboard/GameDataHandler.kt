package org.example.rebuilt_dashboard

import edu.wpi.first.networktables.NetworkTableInstance

enum class ShiftType {
    WON_AUTO,
    LOST_AUTO,
    ALL
}

data class GameShift(
    val startTime: Int,
    val endTime: Int,
    val shiftType: ShiftType
)

private val gameSpecificMessageSubscriber = NetworkTableInstance.getDefault().getTable("/AdvantageKit/DriverStation").getStringTopic("GameSpecificMessage").subscribe("R")
private val isRedSubscriber = NetworkTableInstance.getDefault().getTable("/AdvantageKit/RealOutputs/IS_RED").getBooleanTopic("IS_RED").subscribe(true)

private var overrideGameSpecific = ""

var gameSpecific: String
    get() = overrideGameSpecific.ifEmpty { gameSpecificMessageSubscriber.get() }
    set(value) { overrideGameSpecific = value }

fun didWeWinAuto(): Boolean{
    val msg = gameSpecific
    if(msg.isEmpty()) return true

    return when(msg.firstOrNull()?.uppercaseChar()){
        'R' -> isRedSubscriber.get()
        'B' -> !isRedSubscriber.get()
        else -> true
    }
}

private val SHIFTS = listOf(
    GameShift(2 * 60 + 40, 2 * 60 + 10, ShiftType.ALL), // auto + first shift
    GameShift(2 * 60 + 10, 1 * 60 + 45, ShiftType.LOST_AUTO),
    GameShift(1 * 60 + 45, 1 * 60 + 20, ShiftType.WON_AUTO),
    GameShift(1 * 60 + 20, 55, ShiftType.LOST_AUTO),
    GameShift(55, 30, ShiftType.WON_AUTO),
    GameShift(30, 0, ShiftType.ALL) // endgame
)

val matchTime: Long
    get() = MatchTime.matchTime.value / 1000

val currentShift: GameShift?
    get() = SHIFTS.find { matchTime in it.endTime..it.startTime }

val isOurHubActive: Boolean
    get() = when((currentShift?.shiftType)) {
        ShiftType.ALL -> true
        ShiftType.WON_AUTO -> didWeWinAuto()
        ShiftType.LOST_AUTO -> !didWeWinAuto()
        else -> true
    }

val timeUntilNextShift: Long
    get() = currentShift?.endTime?.let { matchTime - it } ?: 0