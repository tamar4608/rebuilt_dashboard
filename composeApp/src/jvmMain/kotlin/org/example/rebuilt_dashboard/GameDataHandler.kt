package org.example.rebuilt_dashboard

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.util.Color

private var isShiftOneActiveRedBackingField: Boolean? = null
private val gameSpecificSubscriber = NetworkTableInstance.getDefault().getTable("/AdvantageKit/DriverStation").getStringTopic("GameSpecificMessage").subscribe("R")
private val isRedSubscriber = NetworkTableInstance.getDefault().getTable("/AdvantageKit/RealOutputs/IS_RED").getBooleanTopic("IS_RED").subscribe(true)

private val IS_RED
    get() = isRedSubscriber.get()

private fun isShiftOneActiveRed(): Boolean? {
    if (isShiftOneActiveRedBackingField != null) {
        return isShiftOneActiveRedBackingField
    }
    val message = gameSpecificSubscriber.get()
    if (message.isEmpty()) return null

    isShiftOneActiveRedBackingField =
        when (message.firstOrNull()) {
            'R' -> false
            'B' -> true
            else -> null
        }
    return isShiftOneActiveRedBackingField
}

private val SHIFT_CHANGES =
    listOf(2*60 + 10, 1*60 + 45, 1*60 + 20, 55, 30)

val isOurHubActive: Boolean
    get() {
        // Both Hubs are active in the beginning and end of the match.
        val bothHubsActive =
            matchTime !in SHIFT_CHANGES.last()..SHIFT_CHANGES.first()

        val wasShiftOneOurs = IS_RED == isShiftOneActiveRed()

        val currentIndex = SHIFT_CHANGES.indexOfFirst { matchTime > it }
        val isCurrentShiftOdd = currentIndex % 2 == 1

        return bothHubsActive || (wasShiftOneOurs == isCurrentShiftOdd)
    }

val activeColor: Color
    get() {
        return (if (
            isShiftOneActiveRed() == null || matchTime < SHIFT_CHANGES.last()
        )
            Color.kPurple
        else {
            if (isOurHubActive)
                if (IS_RED) Color.kOrangeRed else Color.kFirstBlue
            else if (IS_RED) Color.kFirstBlue else Color.kOrangeRed
        })
    }

val timeUntilNextShift: Double
    get() {
        SHIFT_CHANGES.find { matchTime > it }
            ?.let {
                return (matchTime - it)
            }
        return 0.0
    }
