package com.menti.workoutTimer.data

import androidx.room.*
import com.menti.workoutTimer.model.WorkoutHistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for workout history operations.
 */
@Dao
interface WorkoutHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WorkoutHistoryEntry): Long
    
    @Query("SELECT * FROM workout_history ORDER BY date DESC")
    fun getAllEntries(): Flow<List<WorkoutHistoryEntry>>
    
    @Query("SELECT * FROM workout_history ORDER BY date DESC")
    suspend fun getAllEntriesSync(): List<WorkoutHistoryEntry>
    
    @Query("SELECT * FROM workout_history WHERE date >= :startDate AND date < :endDate ORDER BY date DESC")
    fun getEntriesByDate(startDate: Long, endDate: Long): Flow<List<WorkoutHistoryEntry>>
    
    @Query("SELECT * FROM workout_history WHERE date >= :startDate AND date < :endDate ORDER BY date DESC")
    suspend fun getEntriesByDateSync(startDate: Long, endDate: Long): List<WorkoutHistoryEntry>
    
    @Query("SELECT COUNT(*) FROM workout_history")
    fun getTotalWorkoutCount(): Flow<Int>
    
    @Query("SELECT SUM(totalTime) FROM workout_history")
    fun getTotalWorkoutTime(): Flow<Long?>
    
    @Query("SELECT AVG(CAST(completedRounds AS REAL) / CAST(rounds AS REAL)) FROM workout_history WHERE rounds > 0")
    fun getAverageCompletionRate(): Flow<Float?>
    
    @Query("SELECT * FROM workout_history ORDER BY date DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): Flow<List<WorkoutHistoryEntry>>
    
    @Delete
    suspend fun deleteEntry(entry: WorkoutHistoryEntry): Int
    
    @Query("DELETE FROM workout_history")
    suspend fun deleteAllEntries(): Int
}
