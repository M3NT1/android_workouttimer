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
        private const val KEY_TOTAL_WORKOUTS = "totalWorkouts"
        private const val KEY_SOUND_ENABLED = "soundEnabled"
        private const val KEY_VIBRATION_ENABLED = "vibrationEnabled"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_WARMUP_ENABLED = "warmupEnabled"
        private const val KEY_COOLDOWN_ENABLED = "cooldownEnabled"
        private const val KEY_WARMUP_DURATION = "warmupDuration"
        private const val KEY_COOLDOWN_DURATION = "cooldownDuration"

        
        // Alapértelmezett értékek
        const val DEFAULT_WORKOUT_DURATION = 30 // 30 mp edzés
        const val DEFAULT_REST_DURATION = 15 // 15 mp pihenés
        const val DEFAULT_ROUNDS = 4 // 4 kör
        const val DEFAULT_WARMUP_DURATION = 10 // 10 mp bemelegítés
        const val DEFAULT_COOLDOWN_DURATION = 10 // 10 mp levezetés
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

    /**
     * Az összes befejezett edzés számának lekérdezése
     */
    var totalWorkoutCount: Int
        get() = sharedPreferences.getInt(KEY_TOTAL_WORKOUTS, 0)
        set(value) {
            sharedPreferences.edit().putInt(KEY_TOTAL_WORKOUTS, value).apply()
        }

    /**
     * Hangjelzés engedélyezése
     */
    var isSoundEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
        }

    /**
     * Rezgés engedélyezése
     */
    var isVibrationEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()
        }

    /**
     * Választott nyelv ("en" vagy "hu")
     */
    var language: String
        get() = sharedPreferences.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) {
            sharedPreferences.edit().putString(KEY_LANGUAGE, value).apply()
        }

    /**
     * Bemelegítés engedélyezése
     */
    var isWarmupEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_WARMUP_ENABLED, false)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_WARMUP_ENABLED, value).apply()
        }

    /**
     * Levezetés engedélyezése
     */
    var isCooldownEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_COOLDOWN_ENABLED, false)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_COOLDOWN_ENABLED, value).apply()
        }

    /**
     * Bemelegítés időtartama
     */
    var warmupDuration: Int
        get() = sharedPreferences.getInt(KEY_WARMUP_DURATION, DEFAULT_WARMUP_DURATION)
        set(value) {
            sharedPreferences.edit().putInt(KEY_WARMUP_DURATION, value).apply()
        }

    /**
     * Levezetés időtartama
     */
    var cooldownDuration: Int
        get() = sharedPreferences.getInt(KEY_COOLDOWN_DURATION, DEFAULT_COOLDOWN_DURATION)
        set(value) {
            sharedPreferences.edit().putInt(KEY_COOLDOWN_DURATION, value).apply()
        }
}