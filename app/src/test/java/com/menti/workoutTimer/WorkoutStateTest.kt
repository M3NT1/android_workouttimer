package com.menti.workoutTimer

import com.menti.workoutTimer.model.PhaseType
import com.menti.workoutTimer.model.WorkoutState
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for WorkoutState data class and PhaseType enum.
 */
class WorkoutStateTest {

    @Test
    fun `default state has correct values`() {
        val state = WorkoutState()

        assertEquals(0L, state.timeLeftInMillis)
        assertEquals(0L, state.originalTimeInMillis)
        assertEquals(PhaseType.WORKOUT, state.phaseType)
        assertEquals(1, state.currentRound)
        assertFalse(state.isTimerRunning)
        assertFalse(state.isPaused)
        assertFalse(state.workoutComplete)
        assertEquals(0, state.totalWorkouts)
    }

    @Test
    fun `progress is zero when original time is zero`() {
        val state = WorkoutState(
            timeLeftInMillis = 0,
            originalTimeInMillis = 0
        )

        assertEquals(0f, state.progress)
    }

    @Test
    fun `progress is zero when time left equals original time`() {
        val state = WorkoutState(
            timeLeftInMillis = 30000,
            originalTimeInMillis = 30000
        )

        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun `progress is one when time left is zero`() {
        val state = WorkoutState(
            timeLeftInMillis = 0,
            originalTimeInMillis = 30000
        )

        assertEquals(1f, state.progress, 0.001f)
    }

    @Test
    fun `progress is half when half time elapsed`() {
        val state = WorkoutState(
            timeLeftInMillis = 15000,
            originalTimeInMillis = 30000
        )

        assertEquals(0.5f, state.progress, 0.001f)
    }

    @Test
    fun `formatted time shows MM SS format`() {
        val state = WorkoutState(
            timeLeftInMillis = 30000 // 30 seconds
        )

        assertEquals("00:30", state.formattedTime)
    }

    @Test
    fun `formatted time shows minutes and seconds correctly`() {
        val state = WorkoutState(
            timeLeftInMillis = 90000 // 1 minute 30 seconds
        )

        assertEquals("01:30", state.formattedTime)
    }

    @Test
    fun `formatted time shows zero when no time left`() {
        val state = WorkoutState(
            timeLeftInMillis = 0
        )

        assertEquals("00:00", state.formattedTime)
    }

    @Test
    fun `formatted time handles large values`() {
        val state = WorkoutState(
            timeLeftInMillis = 3600000 // 1 hour
        )

        assertEquals("60:00", state.formattedTime)
    }

    @Test
    fun `isWorkoutPhase returns true for WORKOUT phase`() {
        val state = WorkoutState(
            phaseType = PhaseType.WORKOUT
        )

        assertTrue(state.isWorkoutPhase)
    }

    @Test
    fun `isWorkoutPhase returns false for REST phase`() {
        val state = WorkoutState(
            phaseType = PhaseType.REST
        )

        assertFalse(state.isWorkoutPhase)
    }

    @Test
    fun `isWorkoutPhase returns false for WARMUP phase`() {
        val state = WorkoutState(
            phaseType = PhaseType.WARMUP
        )

        assertFalse(state.isWorkoutPhase)
    }

    @Test
    fun `isWorkoutPhase returns false for COOLDOWN phase`() {
        val state = WorkoutState(
            phaseType = PhaseType.COOLDOWN
        )

        assertFalse(state.isWorkoutPhase)
    }

    @Test
    fun `PhaseType enum has all expected values`() {
        val phases = PhaseType.values()

        assertEquals(4, phases.size)
        assertTrue(phases.contains(PhaseType.WARMUP))
        assertTrue(phases.contains(PhaseType.WORKOUT))
        assertTrue(phases.contains(PhaseType.REST))
        assertTrue(phases.contains(PhaseType.COOLDOWN))
    }

    @Test
    fun `state with custom values`() {
        val state = WorkoutState(
            timeLeftInMillis = 15000,
            originalTimeInMillis = 30000,
            phaseType = PhaseType.REST,
            currentRound = 3,
            isTimerRunning = true,
            isPaused = false,
            workoutComplete = false,
            totalWorkouts = 5
        )

        assertEquals(15000L, state.timeLeftInMillis)
        assertEquals(30000L, state.originalTimeInMillis)
        assertEquals(PhaseType.REST, state.phaseType)
        assertEquals(3, state.currentRound)
        assertTrue(state.isTimerRunning)
        assertFalse(state.isPaused)
        assertFalse(state.workoutComplete)
        assertEquals(5, state.totalWorkouts)
        assertEquals(0.5f, state.progress, 0.001f)
        assertEquals("00:15", state.formattedTime)
    }

    @Test
    fun `copy creates new state with modified values`() {
        val original = WorkoutState(
            timeLeftInMillis = 30000,
            phaseType = PhaseType.WORKOUT
        )

        val copied = original.copy(
            timeLeftInMillis = 15000,
            isTimerRunning = true
        )

        assertEquals(30000L, original.timeLeftInMillis)
        assertEquals(15000L, copied.timeLeftInMillis)
        assertEquals(PhaseType.WORKOUT, original.phaseType)
        assertEquals(PhaseType.WORKOUT, copied.phaseType)
        assertFalse(original.isTimerRunning)
        assertTrue(copied.isTimerRunning)
    }
}
