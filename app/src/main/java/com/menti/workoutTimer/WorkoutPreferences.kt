package com.menti.workoutTimer

import android.content.Context
import android.content.SharedPreferences

/**
 * Az edzés beállításokat kezelő osztály
 */
class WorkoutPreferences(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "WorkoutTimerPrefs"
        private const val KEY_WORKOUT_DURATION = "workoutDuration"
        private const val KEY_REST_DURATION = "restDuration"
        private const val KEY_ROUNDS = "rounds"
        
        // Alapértelmezett értékek
        const val DEFAULT_WORKOUT_DURATION = 30 // 30 mp edzés
        const val DEFAULT_REST_DURATION = 15 // 15 mp pihenés
        const val DEFAULT_ROUNDS = 4 // 4 kör
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Az edzés időtartamának lekérdezése
     * @return Az edzés időtartama másodpercben
     */
    var workoutDuration: Int
        get() = sharedPreferences.getInt(KEY_WORKOUT_DURATION, DEFAULT_WORKOUT_DURATION)
        set(value) {
            sharedPreferences.edit().putInt(KEY_WORKOUT_DURATION, value).apply()
        }
    
    /**
     * A pihenés időtartamának lekérdezése
     * @return A pihenés időtartama másodpercben
     */
    var restDuration: Int
        get() = sharedPreferences.getInt(KEY_REST_DURATION, DEFAULT_REST_DURATION)
        set(value) {
            sharedPreferences.edit().putInt(KEY_REST_DURATION, value).apply()
        }
    
    /**
     * A körök számának lekérdezése
     * @return A körök száma
     */
    var rounds: Int
        get() = sharedPreferences.getInt(KEY_ROUNDS, DEFAULT_ROUNDS)
        set(value) {
            sharedPreferences.edit().putInt(KEY_ROUNDS, value).apply()
        }
} 