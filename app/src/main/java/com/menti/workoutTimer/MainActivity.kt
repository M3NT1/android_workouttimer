package com.menti.workoutTimer

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * A fő Activity, amely az időzítőt és az edzés/pihenés ciklusokat kezeli
 */
class MainActivity : AppCompatActivity() {

    // UI komponensek
    private lateinit var timerTextView: TextView
    private lateinit var currentPhaseTextView: TextView
    private lateinit var nextPhaseTextView: TextView
    private lateinit var roundTextView: TextView
    private lateinit var startPauseButton: Button
    private lateinit var resetButton: Button
    private lateinit var settingsButton: Button

    // Timer változók
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var originalTimeInMillis: Long = 0
    private var isWorkoutPhase = true
    private var currentRound = 1
    private var isTimerRunning = false
    private var isPaused = false

    // Beállítások
    private lateinit var workoutPreferences: WorkoutPreferences

    // Vibrator service a rezgő visszajelzéshez
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Képernyő ébren tartása
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // UI komponensek inicializálása
        initializeUI()

        // Események kezelése
        setupEventListeners()

        // Beállítások betöltése
        workoutPreferences = WorkoutPreferences(this)

        // Vibrator service inicializálása
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Kezdőállapot beállítása
        resetTimer()
    }

    /**
     * Az UI komponensek inicializálása
     */
    private fun initializeUI() {
        timerTextView = findViewById(R.id.timerTextView)
        currentPhaseTextView = findViewById(R.id.currentPhaseTextView)
        nextPhaseTextView = findViewById(R.id.nextPhaseTextView)
        roundTextView = findViewById(R.id.roundTextView)
        startPauseButton = findViewById(R.id.startPauseButton)
        resetButton = findViewById(R.id.resetButton)
        settingsButton = findViewById(R.id.settingsButton)
    }

    /**
     * Az eseménykezelők beállítása
     */
    private fun setupEventListeners() {
        startPauseButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        resetButton.setOnClickListener {
            resetTimer()
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Az időzítő indítása vagy folytatása
     */
    private fun startTimer() {
        // Ha az időzítő szüneteltetve volt, folytatjuk
        if (isPaused) {
            createAndStartTimer()
            isPaused = false
            isTimerRunning = true
            updateButtons()
            return
        }

        // Az időzítő kezdeti beállítása
        setupTimerForCurrentPhase()
        createAndStartTimer()
        isTimerRunning = true
        updateButtons()
    }

    /**
     * Az időzítő szüneteltetése
     */
    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        isPaused = true
        updateButtons()
    }

    /**
     * Az időzítő visszaállítása
     */
    private fun resetTimer() {
        if (timer != null) {
            timer?.cancel()
        }

        // Alapállapot visszaállítása
        isWorkoutPhase = true
        currentRound = 1
        isTimerRunning = false
        isPaused = false

        // Időzítő beállítása az edzés fázisra
        timeLeftInMillis = (workoutPreferences.workoutDuration * 1000).toLong()
        originalTimeInMillis = timeLeftInMillis

        // UI frissítése
        updateCountdownUI()
        updatePhaseUI()
        updateRoundUI()
        updateButtons()
    }

    /**
     * Létrehoz és elindít egy új időzítőt
     */
    private fun createAndStartTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountdownUI()
            }

            override fun onFinish() {
                vibratePhone()
                advanceToNextPhase()
            }
        }.start()
    }

    /**
     * Beállítja az időzítőt a jelenlegi fázisnak megfelelően
     */
    private fun setupTimerForCurrentPhase() {
        if (isWorkoutPhase) {
            timeLeftInMillis = (workoutPreferences.workoutDuration * 1000).toLong()
        } else {
            timeLeftInMillis = (workoutPreferences.restDuration * 1000).toLong()
        }
        originalTimeInMillis = timeLeftInMillis
    }

    /**
     * Továbblép a következő fázisba
     */
    private fun advanceToNextPhase() {
        if (!isWorkoutPhase) {
            // Ha a pihenés véget ért, növeljük a kör számát
            currentRound++
        }

        // Ellenőrizzük, hogy befejeződött-e az edzés
        if (currentRound > workoutPreferences.rounds) {
            workoutComplete()
            return
        }

        // Váltás a következő fázisra
        isWorkoutPhase = !isWorkoutPhase
        setupTimerForCurrentPhase()
        updatePhaseUI()
        updateRoundUI()

        // Újraindítjuk az időzítőt
        createAndStartTimer()
    }

    /**
     * Az edzés befejezése
     */
    private fun workoutComplete() {
        timer?.cancel()
        isTimerRunning = false
        isPaused = false

        // Gratulációs üzenet megjelenítése
        timerTextView.text = getString(R.string.workout_complete)
        currentPhaseTextView.text = ""
        nextPhaseTextView.text = ""

        // Gombok frissítése
        updateButtons()
    }

    /**
     * A visszaszámlálás UI frissítése
     */
    private fun updateCountdownUI() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        timerTextView.text = timeString
    }

    /**
     * A fázisokat megjelenítő UI frissítése
     */
    private fun updatePhaseUI() {
        if (isWorkoutPhase) {
            currentPhaseTextView.text = getString(R.string.workout)
            currentPhaseTextView.setTextColor(getColor(R.color.workout_color))
            nextPhaseTextView.text = getString(R.string.rest)
            nextPhaseTextView.setTextColor(getColor(R.color.rest_color))
        } else {
            currentPhaseTextView.text = getString(R.string.rest)
            currentPhaseTextView.setTextColor(getColor(R.color.rest_color))
            nextPhaseTextView.text = getString(R.string.workout)
            nextPhaseTextView.setTextColor(getColor(R.color.workout_color))
        }
    }

    /**
     * A körszámláló UI frissítése
     */
    private fun updateRoundUI() {
        val roundText = "${getString(R.string.round)} $currentRound / ${workoutPreferences.rounds}"
        roundTextView.text = roundText
    }

    /**
     * A gombok állapotának frissítése
     */
    private fun updateButtons() {
        if (isTimerRunning) {
            startPauseButton.text = getString(R.string.pause)
        } else if (isPaused) {
            startPauseButton.text = getString(R.string.resume)
        } else {
            startPauseButton.text = getString(R.string.start)
        }
    }

    /**
     * A telefon vibráltatása fázisváltásnál
     */
    private fun vibratePhone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    /**
     * Activity újraindítása (pl. beállítások változtatása után)
     */
    override fun onResume() {
        super.onResume()
        // Ha nem fut az időzítő, újra betöltjük a beállításokat
        if (!isTimerRunning && !isPaused) {
            resetTimer()
        }
    }

    /**
     * Időzítő leállítása az Activity leállításakor
     */
    override fun onStop() {
        super.onStop()
        if (isTimerRunning) {
            pauseTimer()
        }
    }
} 