package com.menti.workoutTimer

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.materialswitch.MaterialSwitch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * A beállítások képernyőt kezelő Activity
 */
class SettingsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.applyLocale(newBase)
        super.attachBaseContext(context)
    }
    
    private lateinit var workoutDurationEditText: EditText
    private lateinit var restDurationEditText: EditText
    private lateinit var roundsEditText: EditText
    private lateinit var warmupEnabledSwitch: MaterialSwitch
    private lateinit var cooldownEnabledSwitch: MaterialSwitch
    private lateinit var warmupDurationEditText: EditText
    private lateinit var cooldownDurationEditText: EditText
    private lateinit var soundSwitch: MaterialSwitch
    private lateinit var vibrationSwitch: MaterialSwitch
    private lateinit var languageSpinner: Spinner
    private lateinit var saveButton: Button
    
    private lateinit var workoutPreferences: WorkoutPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Handle window insets to avoid overlap with system bars
        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // ActionBar konfiguráció
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        
        // UI elemek inicializálása
        workoutDurationEditText = findViewById(R.id.workoutDurationEditText)
        restDurationEditText = findViewById(R.id.restDurationEditText)
        roundsEditText = findViewById(R.id.roundsEditText)
        warmupEnabledSwitch = findViewById(R.id.warmupEnabledSwitch)
        cooldownEnabledSwitch = findViewById(R.id.cooldownEnabledSwitch)
        warmupDurationEditText = findViewById(R.id.warmupDurationEditText)
        cooldownDurationEditText = findViewById(R.id.cooldownDurationEditText)
        soundSwitch = findViewById(R.id.soundSwitch)
        vibrationSwitch = findViewById(R.id.vibrationSwitch)
        languageSpinner = findViewById(R.id.languageSpinner)
        saveButton = findViewById(R.id.saveButton)

        // Beállítások betöltése
        workoutPreferences = WorkoutPreferences(this)
        
        // Spinner inicializálása hiba nélkül
        setupLanguageSpinner()
        
        loadSettings()
        
        // Add listeners for warmup/cooldown switches
        warmupEnabledSwitch.setOnCheckedChangeListener { _, _ ->
            updateWarmupCooldownFieldsEnabled()
        }
        cooldownEnabledSwitch.setOnCheckedChangeListener { _, _ ->
            updateWarmupCooldownFieldsEnabled()
        }
        
        // Mentés gomb eseménykezelő
        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveSettings()
                // Ha a nyelv megváltozott, célszerű az egész app-ot frissíteni
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf(getString(R.string.english), getString(R.string.hungarian))
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        // Jelenlegi nyelv beállítása
        if (workoutPreferences.language == "hu") {
            languageSpinner.setSelection(1)
        } else {
            languageSpinner.setSelection(0)
        }
    }
    
    /**
     * A beállítások betöltése a SharedPreferences-ből és megjelenítése a mezőkben
     */
    private fun loadSettings() {
        workoutDurationEditText.setText(workoutPreferences.workoutDuration.toString())
        restDurationEditText.setText(workoutPreferences.restDuration.toString())
        roundsEditText.setText(workoutPreferences.rounds.toString())
        warmupEnabledSwitch.isChecked = workoutPreferences.isWarmupEnabled
        cooldownEnabledSwitch.isChecked = workoutPreferences.isCooldownEnabled
        warmupDurationEditText.setText(workoutPreferences.warmupDuration.toString())
        cooldownDurationEditText.setText(workoutPreferences.cooldownDuration.toString())
        soundSwitch.isChecked = workoutPreferences.isSoundEnabled
        vibrationSwitch.isChecked = workoutPreferences.isVibrationEnabled
        updateWarmupCooldownFieldsEnabled()
    }
    
    private fun updateWarmupCooldownFieldsEnabled() {
        warmupDurationEditText.isEnabled = warmupEnabledSwitch.isChecked
        cooldownDurationEditText.isEnabled = cooldownEnabledSwitch.isChecked
    }
    
    /**
     * A beállítások mentése a SharedPreferences-be
     */
    private fun saveSettings() {
        workoutPreferences.workoutDuration = workoutDurationEditText.text.toString().toInt()
        workoutPreferences.restDuration = restDurationEditText.text.toString().toInt()
        workoutPreferences.rounds = roundsEditText.text.toString().toInt()
        workoutPreferences.isWarmupEnabled = warmupEnabledSwitch.isChecked
        workoutPreferences.isCooldownEnabled = cooldownEnabledSwitch.isChecked
        workoutPreferences.warmupDuration = warmupDurationEditText.text.toString().toInt()
        workoutPreferences.cooldownDuration = cooldownDurationEditText.text.toString().toInt()
        workoutPreferences.isSoundEnabled = soundSwitch.isChecked
        workoutPreferences.isVibrationEnabled = vibrationSwitch.isChecked
        
        // Nyelv mentése
        workoutPreferences.language = if (languageSpinner.selectedItemPosition == 1) "hu" else "en"
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
            
            // Validate warmup/cooldown durations if enabled
            if (warmupEnabledSwitch.isChecked && warmupDurationEditText.text.isBlank()) {
                return false
            }
            if (cooldownEnabledSwitch.isChecked && cooldownDurationEditText.text.isBlank()) {
                return false
            }
            
            val warmupDuration = if (warmupEnabledSwitch.isChecked) {
                warmupDurationEditText.text.toString().toInt()
            } else { 0 }
            val cooldownDuration = if (cooldownEnabledSwitch.isChecked) {
                cooldownDurationEditText.text.toString().toInt()
            } else { 0 }
            
            return workoutDuration > 0 && restDuration > 0 && rounds > 0 &&
                   warmupDuration >= 0 && cooldownDuration >= 0
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