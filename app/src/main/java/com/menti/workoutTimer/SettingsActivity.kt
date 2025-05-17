package com.menti.workoutTimer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * A beállítások képernyőt kezelő Activity
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var workoutDurationEditText: EditText
    private lateinit var restDurationEditText: EditText
    private lateinit var roundsEditText: EditText
    private lateinit var saveButton: Button
    
    private lateinit var workoutPreferences: WorkoutPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        // ActionBar konfiguráció
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        
        // UI elemek inicializálása
        workoutDurationEditText = findViewById(R.id.workoutDurationEditText)
        restDurationEditText = findViewById(R.id.restDurationEditText)
        roundsEditText = findViewById(R.id.roundsEditText)
        saveButton = findViewById(R.id.saveButton)
        
        // Beállítások betöltése
        workoutPreferences = WorkoutPreferences(this)
        loadSettings()
        
        // Mentés gomb eseménykezelő
        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveSettings()
                finish()
            } else {
                Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * A beállítások betöltése a SharedPreferences-ből és megjelenítése a mezőkben
     */
    private fun loadSettings() {
        workoutDurationEditText.setText(workoutPreferences.workoutDuration.toString())
        restDurationEditText.setText(workoutPreferences.restDuration.toString())
        roundsEditText.setText(workoutPreferences.rounds.toString())
    }
    
    /**
     * A beállítások mentése a SharedPreferences-be
     */
    private fun saveSettings() {
        workoutPreferences.workoutDuration = workoutDurationEditText.text.toString().toInt()
        workoutPreferences.restDuration = restDurationEditText.text.toString().toInt()
        workoutPreferences.rounds = roundsEditText.text.toString().toInt()
    }
    
    /**
     * A bevitt értékek ellenőrzése
     * @return Igaz, ha minden érték érvényes, hamis, ha bármelyik érvénytelen
     */
    private fun validateInputs(): Boolean {
        // Ellenőrizzük, hogy minden mező ki van-e töltve
        if (workoutDurationEditText.text.isBlank() || 
            restDurationEditText.text.isBlank() || 
            roundsEditText.text.isBlank()) {
            return false
        }
        
        try {
            // Ellenőrizzük, hogy a bevitt értékek pozitív számok-e
            val workoutDuration = workoutDurationEditText.text.toString().toInt()
            val restDuration = restDurationEditText.text.toString().toInt()
            val rounds = roundsEditText.text.toString().toInt()
            
            return workoutDuration > 0 && restDuration > 0 && rounds > 0
        } catch (e: NumberFormatException) {
            return false
        }
    }
    
    /**
     * A vissza gomb kezelése
     */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
} 