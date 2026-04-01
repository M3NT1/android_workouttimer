package com.menti.workoutTimer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.menti.workoutTimer.databinding.ActivityMainBinding
import com.menti.workoutTimer.model.PhaseType
import com.menti.workoutTimer.model.TimerEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A fő Activity, amely az időzítő UI-ját kezeli.
 * A timer logika a TimerForegroundService-ben fut, az állapotot a ViewModel kezeli.
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: WorkoutTimerViewModel by viewModels()

    // ToneGenerator for local sound playback (UI feedback)
    private var toneGenerator: android.media.ToneGenerator? = null

    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.applyLocale(newBase)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets to avoid overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Képernyő ébren tartása
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize ToneGenerator
        toneGenerator = android.media.ToneGenerator(
            android.media.AudioManager.STREAM_NOTIFICATION, 100
        )

        // Setup UI
        setupEventListeners()

        // Observe ViewModel state
        observeState()

        // Observe events
        observeEvents()

        // Bind to service
        viewModel.bindToService()
    }

    private fun setupEventListeners() {
        binding.startPauseButton.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.isTimerRunning) {
                viewModel.pauseTimer()
            } else {
                viewModel.startTimer()
            }
        }

        binding.resetButton.setOnClickListener {
            viewModel.resetTimer()
        }

        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.historyButton.setOnClickListener {
            val intent = Intent(this, WorkoutHistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                // Update timer display
                binding.timerTextView.text = state.formattedTime

                // Update progress bar
                updateProgressBar(state)

                // Update phase display
                updatePhaseUI(state)

                // Update round display
                val roundText = "${getString(R.string.round)} ${state.currentRound} / ${WorkoutPreferences(this@MainActivity).rounds}"
                binding.roundTextView.text = roundText

                // Update buttons
                updateButtons(state)

                // Handle workout complete
                if (state.workoutComplete) {
                    binding.timerTextView.text = getString(R.string.workout_complete)
                    binding.currentPhaseTextView.text = ""
                    binding.nextPhaseTextView.text = ""
                    val totalWorkouts = state.totalWorkouts
                    binding.roundTextView.text = "${getString(R.string.round)} ${getString(R.string.workout_complete)} (Total: $totalWorkouts)"
                }

                // Trigger transition animation on phase change
                if (state.isTimerRunning && state.timeLeftInMillis == state.originalTimeInMillis) {
                    triggerTransitionAnimation(state.isWorkoutPhase)
                }
            }
        }
    }

    /**
     * Update the progress bar based on the current state
     */
    private fun updateProgressBar(state: com.menti.workoutTimer.model.WorkoutState) {
        val progress = (state.progress * 1000).toInt() // Scale to 0-1000 for smoother animation
        binding.progressBar.progress = progress
        
        // Update progress bar color based on phase
        val color = when (state.phaseType) {
            PhaseType.WARMUP -> getColor(R.color.warmup_color)
            PhaseType.WORKOUT -> getColor(R.color.workout_color)
            PhaseType.REST -> getColor(R.color.rest_color)
            PhaseType.COOLDOWN -> getColor(R.color.cooldown_color)
        }
        binding.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is TimerEvent.PlaySound -> playSound()
                    is TimerEvent.Vibrate -> {
                        // Vibration is handled by the service
                    }
                    is TimerEvent.PhaseChanged -> {
                        // Phase change is handled by state observation
                    }
                    is TimerEvent.WorkoutCompleted -> {
                        // Workout complete is handled by state observation
                    }
                    is TimerEvent.CountdownBeep -> {
                        playCountdownBeep()
                    }
                }
            }
        }
    }

    private fun updatePhaseUI(state: com.menti.workoutTimer.model.WorkoutState) {
        if (state.workoutComplete) return

        val (currentText, currentColor) = when (state.phaseType) {
            PhaseType.WARMUP -> getString(R.string.warmup) to getColor(R.color.warmup_color)
            PhaseType.WORKOUT -> getString(R.string.workout) to getColor(R.color.workout_color)
            PhaseType.REST -> getString(R.string.rest) to getColor(R.color.rest_color)
            PhaseType.COOLDOWN -> getString(R.string.cooldown) to getColor(R.color.cooldown_color)
        }

        val (nextText, nextColor) = when (state.phaseType) {
            PhaseType.WARMUP -> getString(R.string.workout) to getColor(R.color.workout_color)
            PhaseType.WORKOUT -> getString(R.string.rest) to getColor(R.color.rest_color)
            PhaseType.REST -> getString(R.string.workout) to getColor(R.color.workout_color)
            PhaseType.COOLDOWN -> "" to getColor(R.color.text_secondary)
        }

        binding.currentPhaseTextView.text = currentText
        binding.currentPhaseTextView.setTextColor(currentColor)
        binding.nextPhaseTextView.text = nextText
        binding.nextPhaseTextView.setTextColor(nextColor)
    }

    private fun updateButtons(state: com.menti.workoutTimer.model.WorkoutState) {
        if (state.workoutComplete) {
            binding.startPauseButton.text = getString(R.string.start)
            return
        }

        if (state.isTimerRunning) {
            binding.startPauseButton.text = getString(R.string.pause)
        } else if (state.isPaused) {
            binding.startPauseButton.text = getString(R.string.resume)
        } else {
            binding.startPauseButton.text = getString(R.string.start)
        }
    }

    private fun playSound() {
        try {
            toneGenerator?.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 500)
        } catch (e: Exception) {
            // Ignore audio errors
        }
    }

    /**
     * Play a short countdown beep sound (3-2-1)
     */
    private fun playCountdownBeep() {
        try {
            toneGenerator?.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 150)
        } catch (e: Exception) {
            // Ignore audio errors
        }
    }

    private fun triggerTransitionAnimation(@Suppress("UNUSED_PARAMETER") isWorkoutPhase: Boolean) {
        binding.transitionOverlay.visibility = View.VISIBLE
        val state = viewModel.uiState.value
        val color = when (state.phaseType) {
            PhaseType.WARMUP -> getColor(R.color.warmup_color)
            PhaseType.WORKOUT -> getColor(R.color.workout_color)
            PhaseType.REST -> getColor(R.color.rest_color)
            PhaseType.COOLDOWN -> getColor(R.color.cooldown_color)
        }
        binding.transitionOverlay.setBackgroundColor(color)
        binding.transitionOverlay.alpha = 0.6f

        binding.transitionOverlay.animate()
            .alpha(0f)
            .setDuration(600)
            .withEndAction {
                binding.transitionOverlay.visibility = View.GONE
            }
            .start()
    }

    override fun onResume() {
        super.onResume()
        // Re-bind to service if needed
        if (!viewModel.uiState.value.isTimerRunning && !viewModel.uiState.value.isPaused) {
            // Reset UI to initial state if not running
        }
    }

    override fun onStop() {
        super.onStop()
        // Don't unbind here - service continues in foreground
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        toneGenerator?.release()
        toneGenerator = null
    }
}
