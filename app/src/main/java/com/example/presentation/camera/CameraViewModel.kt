package com.example.presentation.camera

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PumpXApplication
import com.example.vision.PoseLandmarkData
import com.example.vision.PushupFeedback
import com.example.vision.PushupStateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CalibrationStatus {
    NOT_CALIBRATED,
    CALIBRATING,
    READY
}

data class CameraSessionUiState(
    val feedback: PushupFeedback = PushupFeedback(),
    val landmarkData: PoseLandmarkData? = null,
    val targetPushups: Int = 10,
    val bonusMinutesToUnlock: Int = 15,
    val targetPackage: String? = null,
    val isGoalCompleted: Boolean = false,
    val sessionDurationSeconds: Int = 0,
    val calibrationStatus: CalibrationStatus = CalibrationStatus.NOT_CALIBRATED
)

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PumpXApplication
    private val pushupRepository = app.pushupRepository
    private val monitoredAppRepository = app.monitoredAppRepository
    private val analyticsManager = app.analyticsManager

    private val stateMachine = PushupStateMachine()

    private val _uiState = MutableStateFlow(CameraSessionUiState())
    val uiState: StateFlow<CameraSessionUiState> = _uiState.asStateFlow()

    private var startTimeMs: Long = System.currentTimeMillis()

    fun setupSession(targetPackage: String?, targetPushups: Int, bonusMinutes: Int) {
        stateMachine.reset()
        startTimeMs = System.currentTimeMillis()
        _uiState.value = CameraSessionUiState(
            targetPushups = targetPushups,
            bonusMinutesToUnlock = bonusMinutes,
            targetPackage = targetPackage
        )
        analyticsManager.trackPushupSessionStarted()
    }

    fun onPoseDetected(pose: PoseLandmarkData?) {
        val currentState = _uiState.value
        if (currentState.isGoalCompleted) return

        val feedback = stateMachine.processPose(pose)

        // Calibration logic check
        var calib = currentState.calibrationStatus
        if (calib != CalibrationStatus.READY) {
            if (pose != null && pose.isBodyVisible) {
                calib = CalibrationStatus.READY
            } else {
                calib = CalibrationStatus.CALIBRATING
            }
        }

        val completed = feedback.count >= currentState.targetPushups
        val duration = ((System.currentTimeMillis() - startTimeMs) / 1000).toInt()

        _uiState.value = currentState.copy(
            feedback = feedback,
            landmarkData = pose,
            isGoalCompleted = completed,
            sessionDurationSeconds = duration,
            calibrationStatus = calib
        )

        if (completed && !currentState.isGoalCompleted) {
            recordCompletedSession()
        }
    }

    private fun recordCompletedSession() {
        val state = _uiState.value
        analyticsManager.trackPushupCompleted(state.feedback.count, state.sessionDurationSeconds.toLong())
        
        viewModelScope.launch {
            pushupRepository.recordSession(
                pushupsCount = state.feedback.count,
                targetPushups = state.targetPushups,
                unlockedBonusMinutes = state.bonusMinutesToUnlock,
                durationSeconds = state.sessionDurationSeconds,
                packageNameTarget = state.targetPackage
            )

            state.targetPackage?.let { pkg ->
                if (pkg != "general") {
                    monitoredAppRepository.addBonusMinutes(pkg, state.bonusMinutesToUnlock)
                }
            }
        }
    }

    fun resetSession() {
        stateMachine.reset()
        startTimeMs = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(
            feedback = PushupFeedback(),
            isGoalCompleted = false,
            sessionDurationSeconds = 0
        )
    }
}
