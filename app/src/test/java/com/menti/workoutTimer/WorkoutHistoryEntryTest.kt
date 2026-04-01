package com.menti.workoutTimer

import com.menti.workoutTimer.model.WorkoutHistoryEntry
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for WorkoutHistoryEntry data class.
 */
class WorkoutHistoryEntryTest {

    @Test
    fun `create WorkoutHistoryEntry with default id`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000,
            completed = true
        )

        assertTrue(entry.id > 0)
        assertEquals(30, entry.workoutDuration)
        assertEquals(15, entry.restDuration)
        assertEquals(4, entry.rounds)
        assertEquals(4, entry.completedRounds)
        assertEquals(180000L, entry.totalTime)
        assertTrue(entry.completed)
    }

    @Test
    fun `create WorkoutHistoryEntry with custom values`() {
        val entry = WorkoutHistoryEntry(
            id = 12345L,
            date = 1000000L,
            workoutDuration = 45,
            restDuration = 20,
            rounds = 6,
            completedRounds = 5,
            totalTime = 300000,
            completed = false
        )

        assertEquals(12345L, entry.id)
        assertEquals(1000000L, entry.date)
        assertEquals(45, entry.workoutDuration)
        assertEquals(20, entry.restDuration)
        assertEquals(6, entry.rounds)
        assertEquals(5, entry.completedRounds)
        assertEquals(300000L, entry.totalTime)
        assertFalse(entry.completed)
    }

    @Test
    fun `completionRate returns 100 percent when all rounds completed`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000,
            completed = true
        )

        assertEquals(1.0f, entry.completionRate(), 0.001f)
    }

    @Test
    fun `completionRate returns 50 percent when half rounds completed`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 2,
            totalTime = 90000,
            completed = false
        )

        assertEquals(0.5f, entry.completionRate(), 0.001f)
    }

    @Test
    fun `completionRate returns zero when no rounds`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 15,
            rounds = 0,
            completedRounds = 0,
            totalTime = 0,
            completed = false
        )

        assertEquals(0f, entry.completionRate(), 0.001f)
    }

    @Test
    fun `completionRate returns 75 percent`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 3,
            totalTime = 135000,
            completed = false
        )

        assertEquals(0.75f, entry.completionRate(), 0.001f)
    }

    @Test
    fun `formatWorkoutDuration formats 30 seconds correctly`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000,
            completed = true
        )

        assertEquals("00:30", entry.formatWorkoutDuration())
    }

    @Test
    fun `formatWorkoutDuration formats 90 seconds correctly`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 90,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000,
            completed = true
        )

        assertEquals("01:30", entry.formatWorkoutDuration())
    }

    @Test
    fun `formatWorkoutDuration formats 0 seconds correctly`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 0,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000,
            completed = true
        )

        assertEquals("00:00", entry.formatWorkoutDuration())
    }

    @Test
    fun `formatRestDuration formats 15 seconds correctly`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000,
            completed = true
        )

        assertEquals("00:15", entry.formatRestDuration())
    }

    @Test
    fun `formatRestDuration formats 60 seconds correctly`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 60,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000,
            completed = true
        )

        assertEquals("01:00", entry.formatRestDuration())
    }

    @Test
    fun `formatTotalTime formats minutes and seconds`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000, // 3 minutes
            completed = true
        )

        assertEquals("03:00", entry.formatTotalTime())
    }

    @Test
    fun `formatTotalTime formats hours minutes and seconds`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 3661000, // 1 hour 1 minute 1 second
            completed = true
        )

        assertEquals("01:01:01", entry.formatTotalTime())
    }

    @Test
    fun `formatTotalTime formats zero correctly`() {
        val entry = WorkoutHistoryEntry(
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 0,
            completed = true
        )

        assertEquals("00:00", entry.formatTotalTime())
    }

    @Test
    fun `data class equality works correctly`() {
        val entry1 = WorkoutHistoryEntry(
            id = 1L,
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000,
            completed = true
        )

        val entry2 = WorkoutHistoryEntry(
            id = 1L,
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000,
            completed = true
        )

        assertEquals(entry1, entry2)
        assertEquals(entry1.hashCode(), entry2.hashCode())
    }

    @Test
    fun `data class toString contains values`() {
        val entry = WorkoutHistoryEntry(
            id = 1L,
            workoutDuration = 30,
            restDuration = 15,
            rounds = 4,
            completedRounds = 4,
            totalTime = 180000,
            completed = true
        )

        val toString = entry.toString()
        assertTrue(toString.contains("WorkoutHistoryEntry"))
        assertTrue(toString.contains("id=1"))
    }
}
