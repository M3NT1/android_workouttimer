package com.menti.workoutTimer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.menti.workoutTimer.model.WorkoutHistoryEntry

/**
 * Room Database for storing workout history.
 */
@Database(
    entities = [WorkoutHistoryEntry::class],
    version = 1,
    exportSchema = false
)
abstract class WorkoutHistoryDatabase : RoomDatabase() {
    
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
    
    companion object {
        private const val DATABASE_NAME = "workout_history_db"
        
        @Volatile
        private var INSTANCE: WorkoutHistoryDatabase? = null
        
        fun getDatabase(context: Context): WorkoutHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutHistoryDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
