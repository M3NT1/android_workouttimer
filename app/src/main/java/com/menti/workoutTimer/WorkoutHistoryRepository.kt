package com.menti.workoutTimer

import android.content.Context
import com.menti.workoutTimer.data.WorkoutHistoryDatabase
import com.menti.workoutTimer.model.WorkoutHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Data class holding workout statistics.
 */
data class WorkoutStatistics(
    val totalWorkouts: Int,
    val totalWorkoutTime: Long,
    val averageCompletionRate: Float,
    val completedWorkouts: Int
) {
    /**
     * Format total workout time as a human-readable string.
     */
    fun formatTotalTime(): String {
        val totalSeconds = totalWorkoutTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}

/**
 * Repository for managing workout history data.
 */
class WorkoutHistoryRepository(context: Context) {
    
    private val dao = WorkoutHistoryDatabase.getDatabase(context).workoutHistoryDao()
    
    /**
     * Add a new workout entry to the history.
     */
    suspend fun addEntry(entry: WorkoutHistoryEntry): Long {
        return dao.insertEntry(entry)
    }
    
    /**
     * Get all workout entries as a Flow.
     */
    fun getAllEntries(): Flow<List<WorkoutHistoryEntry>> {
        return dao.getAllEntries()
    }
    
    /**
     * Get all workout entries synchronously.
     */
    suspend fun getAllEntriesSync(): List<WorkoutHistoryEntry> {
        return dao.getAllEntriesSync()
    }
    
    /**
     * Get entries within a specific date range.
     */
    fun getEntriesByDate(startDate: Long, endDate: Long): Flow<List<WorkoutHistoryEntry>> {
        return dao.getEntriesByDate(startDate, endDate)
    }
    
    /**
     * Get entries within a specific date range synchronously.
     */
    suspend fun getEntriesByDateSync(startDate: Long, endDate: Long): List<WorkoutHistoryEntry> {
        return dao.getEntriesByDateSync(startDate, endDate)
    }
    
    /**
     * Get recent entries (limited to specified count).
     */
    fun getRecentEntries(limit: Int): Flow<List<WorkoutHistoryEntry>> {
        return dao.getRecentEntries(limit)
    }
    
    /**
     * Get workout statistics as a Flow.
     */
    fun getStatisticsFlow(): Flow<WorkoutStatistics> {
        return dao.getTotalWorkoutCount().map { totalCount ->
            val totalTime = dao.getTotalWorkoutTime().first() ?: 0L
            val avgRate = dao.getAverageCompletionRate().first() ?: 0f
            val completedCount = dao.getEntriesByDateSync(0, Long.MAX_VALUE)
                .count { it.completed }
            
            WorkoutStatistics(
                totalWorkouts = totalCount,
                totalWorkoutTime = totalTime,
                averageCompletionRate = avgRate,
                completedWorkouts = completedCount
            )
        }
    }
    
    /**
     * Get workout statistics synchronously.
     */
    suspend fun getStatistics(): WorkoutStatistics {
        val totalCount = dao.getAllEntriesSync().size
        val entries = dao.getAllEntriesSync()
        val totalTime = entries.sumOf { it.totalTime }
        val avgRate = if (entries.isNotEmpty()) {
            entries.map { it.completionRate() }.average().toFloat()
        } else 0f
        val completedCount = entries.count { it.completed }
        
        return WorkoutStatistics(
            totalWorkouts = totalCount,
            totalWorkoutTime = totalTime,
            averageCompletionRate = avgRate,
            completedWorkouts = completedCount
        )
    }
    
    /**
     * Delete a specific entry.
     */
    suspend fun deleteEntry(entry: WorkoutHistoryEntry): Int {
        return dao.deleteEntry(entry)
    }
    
    /**
     * Delete all entries.
     */
    suspend fun deleteAllEntries(): Int {
        return dao.deleteAllEntries()
    }
}
