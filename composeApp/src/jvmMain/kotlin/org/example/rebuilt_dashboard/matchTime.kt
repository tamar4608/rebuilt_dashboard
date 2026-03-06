package org.example.rebuilt_dashboard

import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

object MatchTime {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _matchTime = MutableStateFlow(160000L) // 02:30 במילישניות
    var matchTime = _matchTime.asStateFlow()

    private var timerJob: Job? = null

    private val ntInstance = NetworkTableInstance.getDefault()
    private val isGameOnTopic = ntInstance.getTable("AdvantageKit/DriverStation")
        .getBooleanTopic("Enabled")

    private val isGameOnSubscriber = isGameOnTopic.subscribe(false)

    init {
        observeNetworkTables()
    }

    private fun observeNetworkTables() {
        scope.launch {
            while (isActive) {
                val isGameOn = isGameOnSubscriber.get()

                if (isGameOn && timerJob?.isActive != true) {
                    startCountdown()
                } else if (!isGameOn && timerJob?.isActive == true) {
                    stopCountdown()
                }

                delay(100)
            }
        }
    }

    private fun startCountdown() {
        if (timerJob?.isActive == true) return

        timerJob = scope.launch {
            while (matchTime.value > 0) {
                delay(100)
                _matchTime.value -= 100
            }
        }
    }

    private fun stopCountdown() {
        timerJob?.cancel()
    }

    // פונקציה לאיפוס השעון במידת הצורך
    fun resetTimer() {
        stopCountdown()
        _matchTime.value = 150_000L
    }
}