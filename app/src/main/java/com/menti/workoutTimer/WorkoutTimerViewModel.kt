package com.menti.workoutTimer

import android.app.Application
import android.content.*
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.menti.workoutTimer.model.PhaseType
import com.menti.workoutTimer.model.TimerEvent
import com.menti.workoutTimer.model.WorkoutState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the workout timer state.
 * Survives configuration changes and communicates with the TimerForegroundService.
 */
class WorkoutTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val workoutPreferences = WorkoutPreferences(application)
    private val context: Application = application

    // StateFlow for UI to observe
    private val _uiState = MutableStateFlow(WorkoutState())
    val uiState: StateFlow<WorkoutState> = _uiState.asStateFlow()

    // SingleEvent channel for one-time events (sounds, vibrations)
    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<TimerEvent> = _events.asSharedFlow()

    // Service connection
    private var timerService: TimerForegroundService? = null
    private var bound = false

    /**
     * Service connection callback
     */
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerForegroundService.LocalBinder
            timerService = binder.getService()
            bound = true
            // Collect service state updates
            viewModelScope.launch {
                timerService?.serviceState?.collect { state ->
                    _uiState.value = state
                }
            }
            viewModelScope.launch {
                timerService?.serviceEvents?.collect { event ->
                    _events.tryEmit(event)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            bound = false
        }
    }

    /**
     * Bind to the timer service
     */
    fun bindToService() {
        val intent = Intent(context, TimerForegroundService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Unbind from the timer service
     */
    fun unbindFromService() {
        if (bound) {
            context.unbindService(serviceConnection)
            bound = false
        }
    }

    /**
     * Start or resume the timer via the service
     */
    fun startTimer() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.isPaused) {
                // Resume
                timerService?.resumeTimer()
            } else {
                // Start fresh
                timerService?.startTimer(
                    workoutDuration = workoutPreferences.workoutDuration,
                    restDuration = workoutPreferences.restDuration,
                    rounds = workoutPreferences.rounds,
                    warmupEnabled = workoutPreferences.isWarmupEnabled,
                    warmupDuration = workoutPreferences.warmupDuration,
                    cooldownEnabled = workoutPreferences.isCooldownEnabled,
                    cooldownDuration = workoutPreferences.cooldownDuration
                )
            }
        }
    }

    /**
     * Pause the timer via the service
     */
    fun pauseTimer() {
        timerService?.pauseTimer()
    }

    /**
     * Reset the timer to initial state
     */
    fun resetTimer() {
        timerService?.resetTimer(
            workoutDuration = workoutPreferences.workoutDuration,
            restDuration = workoutPreferences.restDuration,
            rounds = workoutPreferences.rounds,
            warmupEnabled = workoutPreferences.isWarmupEnabled,
            warmupDuration = workoutPreferences.warmupDuration,
            cooldownEnabled = workoutPreferences.isCooldownEnabled,
            cooldownDuration = workoutPreferences.cooldownDuration
        )
    }

    /**
     * Get the current phase label string resource ID
     */
    fun getCurrentPhaseLabel(): Int {
        return when (_uiState.value.phaseType) {
            PhaseType.WARMUP -> R.string.warmup
            PhaseType.WORKOUT -> R.string.workout
            PhaseType.REST -> R.string.rest
            PhaseType.COOLDOWN -> R.string.cooldown
        }
    }

    /**
     * Get the next phase label string resource ID
     */
    fun getNextPhaseLabel(): Int {
        return when (_uiState.value.phaseType) {
            PhaseType.WARMUP -> R.string.workout
            PhaseType.WORKOUT -> R.string.rest
            PhaseType.REST -> R.string.workout
            PhaseType.COOLDOWN -> R.string.workout // Should not happen
        }
    }

    override fun onCleared() {
        super.onCleared()
        unbindFromService()
    }
}
