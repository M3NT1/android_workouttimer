package com.menti.workoutTimer.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a completed workout session.
 */
@Entity(tableName = "workout_history")
data class WorkoutHistoryEntry(
    @PrimaryKey
    val id: Long = System.currentTimeMillis(),
    val date: Long = System.currentTimeMillis(),
    val workoutDuration: Int,
    val restDuration: Int,
    val rounds: Int,
    val completedRounds: Int,
    val totalTime: Long,
    val completed: Boolean
) {
    /**
     * Calculate the completion rate as a percentage.
     */
    fun completionRate(): Float {
        return if (rounds > 0) {
            completedRounds.toFloat() / rounds.toFloat()
        } else 0f
    }
    
    /**
     * Format the workout duration as a string (MM:SS).
     */
    fun formatWorkoutDuration(): String {
        val minutes = workoutDuration / 60
        val seconds = workoutDuration % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * Format the rest duration as a string (MM:SS).
     */
    fun formatRestDuration(): String {
        val minutes = restDuration / 60
        val seconds = restDuration % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * Format total time as a string (HH:MM:SS or MM:SS).
     */
    fun formatTotalTime(): String {
        val totalSeconds = totalTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
