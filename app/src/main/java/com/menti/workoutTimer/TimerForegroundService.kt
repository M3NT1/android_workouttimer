package com.menti.workoutTimer

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.menti.workoutTimer.model.PhaseType
import com.menti.workoutTimer.model.TimerEvent
import com.menti.workoutTimer.model.WorkoutHistoryEntry
import com.menti.workoutTimer.model.WorkoutState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Foreground Service that manages the workout timer.
 * Runs in the foreground with a notification to ensure the timer continues
 * even when the app is backgrounded.
 */
class TimerForegroundService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "workout_timer_channel"
        const val ACTION_START_TIMER = "com.menti.workoutTimer.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.menti.workoutTimer.PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "com.menti.workoutTimer.RESUME_TIMER"
        const val ACTION_RESET_TIMER = "com.menti.workoutTimer.RESET_TIMER"
        const val EXTRA_WORKOUT_DURATION = "workout_duration"
        const val EXTRA_REST_DURATION = "rest_duration"
        const val EXTRA_ROUNDS = "rounds"
        const val EXTRA_WARMUP_ENABLED = "warmup_enabled"
        const val EXTRA_WARMUP_DURATION = "warmup_duration"
        const val EXTRA_COOLDOWN_ENABLED = "cooldown_enabled"
        const val EXTRA_COOLDOWN_DURATION = "cooldown_duration"

        // Action constants for service commands
        const val COMMAND_START = "START"
        const val COMMAND_PAUSE = "PAUSE"
        const val COMMAND_RESUME = "RESUME"
        const val COMMAND_RESET = "RESET"
    }

    private val binder = LocalBinder()
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // State
    private var workoutDuration: Int = WorkoutPreferences.DEFAULT_WORKOUT_DURATION
    private var restDuration: Int = WorkoutPreferences.DEFAULT_REST_DURATION
    private var rounds: Int = WorkoutPreferences.DEFAULT_ROUNDS
    private var warmupEnabled: Boolean = false
    private var warmupDuration: Int = WorkoutPreferences.DEFAULT_WARMUP_DURATION
    private var cooldownEnabled: Boolean = false
    private var cooldownDuration: Int = WorkoutPreferences.DEFAULT_COOLDOWN_DURATION

    private var timeLeftInMillis: Long = 0L
    private var originalTimeInMillis: Long = 0L
    private var currentPhase: PhaseType = PhaseType.WORKOUT
    private var currentRound = 1
    private var isTimerRunning = false
    private var isPaused = false
    private var workoutComplete = false
    private var totalWorkouts = 0
    private var workoutStartTime: Long = 0L
    
    // Countdown beep tracking
    private var lastBeepSecond = -1

    // Feedback
    private var toneGenerator: ToneGenerator? = null
    private lateinit var vibrator: Vibrator
    private lateinit var workoutPreferences: WorkoutPreferences

    // StateFlow for observers
    private val _serviceState = MutableStateFlow(WorkoutState())
    val serviceState: StateFlow<WorkoutState> = _serviceState.asStateFlow()

    private val _serviceEvents = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 1)
    val serviceEvents: SharedFlow<TimerEvent> = _serviceEvents.asSharedFlow()

    inner class LocalBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onCreate() {
        super.onCreate()
        workoutPreferences = WorkoutPreferences(this)
        totalWorkouts = workoutPreferences.totalWorkoutCount

        // Initialize vibrator
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        createNotificationChannel()
        updateState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                workoutDuration = intent.getIntExtra(EXTRA_WORKOUT_DURATION, workoutDuration)
                restDuration = intent.getIntExtra(EXTRA_REST_DURATION, restDuration)
                rounds = intent.getIntExtra(EXTRA_ROUNDS, rounds)
                warmupEnabled = intent.getBooleanExtra(EXTRA_WARMUP_ENABLED, warmupEnabled)
                warmupDuration = intent.getIntExtra(EXTRA_WARMUP_DURATION, warmupDuration)
                cooldownEnabled = intent.getBooleanExtra(EXTRA_COOLDOWN_ENABLED, cooldownEnabled)
                cooldownDuration = intent.getIntExtra(EXTRA_COOLDOWN_DURATION, cooldownDuration)
                startTimerInternal()
            }
            ACTION_PAUSE_TIMER -> pauseTimerInternal()
            ACTION_RESUME_TIMER -> resumeTimerInternal()
            ACTION_RESET_TIMER -> resetTimerInternal()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /**
     * Start the timer with given parameters
     */
    fun startTimer(workoutDuration: Int, restDuration: Int, rounds: Int,
                   warmupEnabled: Boolean = false, warmupDuration: Int = WorkoutPreferences.DEFAULT_WARMUP_DURATION,
                   cooldownEnabled: Boolean = false, cooldownDuration: Int = WorkoutPreferences.DEFAULT_COOLDOWN_DURATION) {
        this.workoutDuration = workoutDuration
        this.restDuration = restDuration
        this.rounds = rounds
        this.warmupEnabled = warmupEnabled
        this.warmupDuration = warmupDuration
        this.cooldownEnabled = cooldownEnabled
        this.cooldownDuration = cooldownDuration
        startTimerInternal()
    }

    /**
     * Pause the timer
     */
    fun pauseTimer() {
        pauseTimerInternal()
    }

    /**
     * Resume the timer
     */
    fun resumeTimer() {
        resumeTimerInternal()
    }

    /**
     * Reset the timer
     */
    fun resetTimer(workoutDuration: Int, restDuration: Int, rounds: Int,
                   warmupEnabled: Boolean = false, warmupDuration: Int = WorkoutPreferences.DEFAULT_WARMUP_DURATION,
                   cooldownEnabled: Boolean = false, cooldownDuration: Int = WorkoutPreferences.DEFAULT_COOLDOWN_DURATION) {
        this.workoutDuration = workoutDuration
        this.restDuration = restDuration
        this.rounds = rounds
        this.warmupEnabled = warmupEnabled
        this.warmupDuration = warmupDuration
        this.cooldownEnabled = cooldownEnabled
        this.cooldownDuration = cooldownDuration
        resetTimerInternal()
    }

    private fun startTimerInternal() {
        if (isPaused) {
            resumeTimerInternal()
            return
        }

        // Reset state for new workout
        currentRound = 1
        workoutComplete = false
        
        // Start with warmup if enabled
        if (warmupEnabled) {
            currentPhase = PhaseType.WARMUP
            timeLeftInMillis = (warmupDuration * 1000).toLong()
        } else {
            currentPhase = PhaseType.WORKOUT
            timeLeftInMillis = (workoutDuration * 1000).toLong()
        }
        originalTimeInMillis = timeLeftInMillis
        isTimerRunning = true
        isPaused = false
        workoutStartTime = System.currentTimeMillis()

        startForeground(NOTIFICATION_ID, createNotification())
        startTimerTick()
        updateState()
    }

    private fun pauseTimerInternal() {
        timerJob?.cancel()
        isTimerRunning = false
        isPaused = true
        updateState()
        updateNotification()
    }

    private fun resumeTimerInternal() {
        isPaused = false
        isTimerRunning = true
        startTimerTick()
        updateState()
        updateNotification()
    }

    private fun resetTimerInternal() {
        timerJob?.cancel()
        currentPhase = if (warmupEnabled) PhaseType.WARMUP else PhaseType.WORKOUT
        currentRound = 1
        isTimerRunning = false
        isPaused = false
        workoutComplete = false
        timeLeftInMillis = if (warmupEnabled) {
            (warmupDuration * 1000).toLong()
        } else {
            (workoutDuration * 1000).toLong()
        }
        originalTimeInMillis = timeLeftInMillis
        updateState()
        updateNotification()
    }

    private fun startTimerTick() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            val startTime = System.currentTimeMillis()
            val startRemaining = timeLeftInMillis
            lastBeepSecond = -1 // Reset beep tracking

            while (isActive) {
                delay(100) // Update every 100ms for smooth UI
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = startRemaining - elapsed

                if (remaining <= 0) {
                    timeLeftInMillis = 0
                    updateState()
                    onTimerFinished()
                    break
                }

                timeLeftInMillis = remaining
                
                // Countdown beep logic: beep at 3, 2, 1 seconds
                val secondsLeft = (remaining / 1000).toInt()
                if (secondsLeft <= 3 && secondsLeft > 0 && secondsLeft != lastBeepSecond) {
                    lastBeepSecond = secondsLeft
                    _serviceEvents.tryEmit(TimerEvent.CountdownBeep)
                }
                
                updateState()
                updateNotification()
            }
        }
    }

    private suspend fun onTimerFinished() {
        // Trigger feedback events
        _serviceEvents.tryEmit(TimerEvent.Vibrate)
        _serviceEvents.tryEmit(TimerEvent.PlaySound)

        // Vibrate
        if (workoutPreferences.isVibrationEnabled) {
            vibratePhone()
        }

        // Play sound
        if (workoutPreferences.isSoundEnabled) {
            playSound()
        }

        // Advance to next phase
        advanceToNextPhase()
    }

    private fun advanceToNextPhase() {
        when (currentPhase) {
            PhaseType.WARMUP -> {
                // After warmup, go to first workout round
                currentPhase = PhaseType.WORKOUT
                timeLeftInMillis = (workoutDuration * 1000).toLong()
            }
            PhaseType.WORKOUT -> {
                // After workout, go to rest
                currentPhase = PhaseType.REST
                timeLeftInMillis = (restDuration * 1000).toLong()
            }
            PhaseType.REST -> {
                // After rest, check if more rounds or go to cooldown
                currentRound++
                if (currentRound > rounds) {
                    // All rounds complete, check for cooldown
                    if (cooldownEnabled) {
                        currentPhase = PhaseType.COOLDOWN
                        timeLeftInMillis = (cooldownDuration * 1000).toLong()
                    } else {
                        completeWorkout()
                        return
                    }
                } else {
                    // More rounds to go
                    currentPhase = PhaseType.WORKOUT
                    timeLeftInMillis = (workoutDuration * 1000).toLong()
                }
            }
            PhaseType.COOLDOWN -> {
                // After cooldown, workout is complete
                completeWorkout()
                return
            }
        }
        originalTimeInMillis = timeLeftInMillis

        _serviceEvents.tryEmit(TimerEvent.PhaseChanged)
        updateState()
        startTimerTick()
    }

    private fun completeWorkout() {
        totalWorkouts++
        workoutPreferences.totalWorkoutCount = totalWorkouts
        workoutComplete = true
        isTimerRunning = false
        isPaused = false
        updateState()
        
        // Save workout history entry
        saveWorkoutHistoryEntry()
        
        _serviceEvents.tryEmit(TimerEvent.WorkoutCompleted)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun updateState() {
        _serviceState.value = WorkoutState(
            timeLeftInMillis = timeLeftInMillis,
            originalTimeInMillis = originalTimeInMillis,
            phaseType = currentPhase,
            currentRound = currentRound,
            isTimerRunning = isTimerRunning,
            isPaused = isPaused,
            workoutComplete = workoutComplete,
            totalWorkouts = totalWorkouts
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows current workout timer status"
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val phaseText = when (currentPhase) {
            PhaseType.WARMUP -> getString(R.string.warmup)
            PhaseType.WORKOUT -> getString(R.string.workout)
            PhaseType.REST -> getString(R.string.rest)
            PhaseType.COOLDOWN -> getString(R.string.cooldown)
        }
        val roundText = "${getString(R.string.round)} $currentRound/$rounds"

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action buttons
        val pauseResumeAction = if (isTimerRunning) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                getString(R.string.pause),
                getPendingIntentForAction(ACTION_PAUSE_TIMER)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                getString(R.string.resume),
                getPendingIntentForAction(ACTION_RESUME_TIMER)
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(phaseText)
            .setContentText(roundText)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(pauseResumeAction)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.reset),
                getPendingIntentForAction(ACTION_RESET_TIMER)
            )
            .setProgress(100, (getProgressPercent() * 100).toInt(), false)
            .build()
    }

    private fun updateNotification() {
        if (isTimerRunning || isPaused) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
    }

    private fun getProgressPercent(): Float {
        return if (originalTimeInMillis > 0) {
            (originalTimeInMillis - timeLeftInMillis).toFloat() / originalTimeInMillis.toFloat()
        } else 0f
    }

    private fun getPendingIntentForAction(action: String): PendingIntent {
        val intent = Intent(this, TimerForegroundService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun vibratePhone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    private fun playSound() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
        } catch (e: Exception) {
            // Ignore audio errors
        }
    }

    /**
     * Save a workout history entry when the workout completes.
     */
    private fun saveWorkoutHistoryEntry() {
        val totalTime = System.currentTimeMillis() - workoutStartTime
        val completedRounds = if (workoutComplete) rounds else currentRound - 1
        
        val entry = WorkoutHistoryEntry(
            id = System.currentTimeMillis(),
            date = System.currentTimeMillis(),
            workoutDuration = workoutDuration,
            restDuration = restDuration,
            rounds = rounds,
            completedRounds = completedRounds,
            totalTime = totalTime,
            completed = workoutComplete && completedRounds >= rounds
        )
        
        // Save to repository using service scope
        serviceScope.launch {
            val repository = WorkoutHistoryRepository(this@TimerForegroundService)
            repository.addEntry(entry)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
        toneGenerator?.release()
        toneGenerator = null
    }
}
