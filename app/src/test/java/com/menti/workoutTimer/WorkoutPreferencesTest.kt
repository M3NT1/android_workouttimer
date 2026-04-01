package com.menti.workoutTimer

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unit tests for WorkoutPreferences class using Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
class WorkoutPreferencesTest {

    private lateinit var context: Context
    private lateinit var workoutPreferences: WorkoutPreferences

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        // Clear preferences before each test
        val prefs = context.getSharedPreferences("WorkoutTimerPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        workoutPreferences = WorkoutPreferences(context)
    }

    @Test
    fun `default workout duration is 30 seconds`() {
        assertEquals(30, workoutPreferences.workoutDuration)
    }

    @Test
    fun `default rest duration is 15 seconds`() {
        assertEquals(15, workoutPreferences.restDuration)
    }

    @Test
    fun `default rounds is 4`() {
        assertEquals(4, workoutPreferences.rounds)
    }

    @Test
    fun `default warmup duration is 10 seconds`() {
        assertEquals(10, workoutPreferences.warmupDuration)
    }

    @Test
    fun `default cooldown duration is 10 seconds`() {
        assertEquals(10, workoutPreferences.cooldownDuration)
    }

    @Test
    fun `default sound enabled is true`() {
        assertTrue(workoutPreferences.isSoundEnabled)
    }

    @Test
    fun `default vibration enabled is true`() {
        assertTrue(workoutPreferences.isVibrationEnabled)
    }

    @Test
    fun `default language is English`() {
        assertEquals("en", workoutPreferences.language)
    }

    @Test
    fun `default warmup enabled is false`() {
        assertFalse(workoutPreferences.isWarmupEnabled)
    }

    @Test
    fun `default cooldown enabled is false`() {
        assertFalse(workoutPreferences.isCooldownEnabled)
    }

    @Test
    fun `default total workout count is 0`() {
        assertEquals(0, workoutPreferences.totalWorkoutCount)
    }

    @Test
    fun `save and load workout duration`() {
        workoutPreferences.workoutDuration = 45
        assertEquals(45, workoutPreferences.workoutDuration)
    }

    @Test
    fun `save and load rest duration`() {
        workoutPreferences.restDuration = 20
        assertEquals(20, workoutPreferences.restDuration)
    }

    @Test
    fun `save and load rounds`() {
        workoutPreferences.rounds = 6
        assertEquals(6, workoutPreferences.rounds)
    }

    @Test
    fun `save and load sound enabled`() {
        workoutPreferences.isSoundEnabled = false
        assertFalse(workoutPreferences.isSoundEnabled)
    }

    @Test
    fun `save and load vibration enabled`() {
        workoutPreferences.isVibrationEnabled = false
        assertFalse(workoutPreferences.isVibrationEnabled)
    }

    @Test
    fun `save and load language`() {
        workoutPreferences.language = "hu"
        assertEquals("hu", workoutPreferences.language)
    }

    @Test
    fun `save and load warmup enabled`() {
        workoutPreferences.isWarmupEnabled = true
        assertTrue(workoutPreferences.isWarmupEnabled)
    }

    @Test
    fun `save and load cooldown enabled`() {
        workoutPreferences.isCooldownEnabled = true
        assertTrue(workoutPreferences.isCooldownEnabled)
    }

    @Test
    fun `save and load warmup duration`() {
        workoutPreferences.warmupDuration = 20
        assertEquals(20, workoutPreferences.warmupDuration)
    }

    @Test
    fun `save and load cooldown duration`() {
        workoutPreferences.cooldownDuration = 15
        assertEquals(15, workoutPreferences.cooldownDuration)
    }

    @Test
    fun `save and load total workout count`() {
        workoutPreferences.totalWorkoutCount = 10
        assertEquals(10, workoutPreferences.totalWorkoutCount)
    }

    @Test
    fun `preferences persist across instances`() {
        // Save with one instance
        workoutPreferences.workoutDuration = 50
        workoutPreferences.restDuration = 25
        workoutPreferences.rounds = 8

        // Create new instance and verify values
        val newPrefs = WorkoutPreferences(context)
        assertEquals(50, newPrefs.workoutDuration)
        assertEquals(25, newPrefs.restDuration)
        assertEquals(8, newPrefs.rounds)
    }
}
