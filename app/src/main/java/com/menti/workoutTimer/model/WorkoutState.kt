package com.menti.workoutTimer.model

/**
 * Enum representing the different phases of a workout session.
 */
enum class PhaseType {
    WARMUP,
    WORKOUT,
    REST,
    COOLDOWN
}

/**
 * Data class representing the complete state of a workout session.
 * Used by WorkoutTimerViewModel to expose state via StateFlow.
 */
data class WorkoutState(
    val timeLeftInMillis: Long = 0L,
    val originalTimeInMillis: Long = 0L,
    val phaseType: PhaseType = PhaseType.WORKOUT,
    val currentRound: Int = 1,
    val isTimerRunning: Boolean = false,
    val isPaused: Boolean = false,
    val workoutComplete: Boolean = false,
    val totalWorkouts: Int = 0
) {
    /**
     * Backward-compatible property for checking if we're in workout phase.
     * @deprecated Use phaseType == PhaseType.WORKOUT instead
     */
    val isWorkoutPhase: Boolean
        get() = phaseType == PhaseType.WORKOUT

    /**
     * Calculate the progress as a percentage (0.0 to 1.0)
     */
    val progress: Float
        get() = if (originalTimeInMillis > 0) {
            (originalTimeInMillis - timeLeftInMillis).toFloat() / originalTimeInMillis.toFloat()
        } else 0f

    /**
     * Format time as MM:SS string
     */
    val formattedTime: String
        get() {
            val totalSeconds = timeLeftInMillis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

/**
 * Sealed class representing one-time events from the timer.
 * These events are consumed after being handled once.
 */
sealed class TimerEvent {
    object PhaseChanged : TimerEvent()
    object WorkoutCompleted : TimerEvent()
    object PlaySound : TimerEvent()
    object Vibrate : TimerEvent()
    object CountdownBeep : TimerEvent()  // Countdown beep (3-2-1)
}
