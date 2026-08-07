package com.example.vision

import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

class PushupStateMachine {

    private var currentState: PushupState = PushupState.IDLE
    private var repCount: Int = 0
    private var lastStateChangeTimeMs: Long = System.currentTimeMillis()
    private var minBottomAngleReached: Double = 180.0

    fun reset() {
        currentState = PushupState.IDLE
        repCount = 0
        lastStateChangeTimeMs = System.currentTimeMillis()
        minBottomAngleReached = 180.0
    }

    fun processPose(pose: PoseLandmarkData?): PushupFeedback {
        val now = System.currentTimeMillis()

        if (pose == null || !pose.isBodyVisible) {
            currentState = PushupState.IDLE
            return PushupFeedback(
                state = PushupState.IDLE,
                count = repCount,
                primaryAngle = 180.0,
                instructionMessage = "Tout ton corps doit être visible dans le cadre.",
                isBodyFullyVisible = false
            )
        }

        // Calculate left & right elbow angles
        val leftAngle = calculateAngle(pose.leftShoulder!!, pose.leftElbow!!, pose.leftWrist!!)
        val rightAngle = calculateAngle(pose.rightShoulder!!, pose.rightElbow!!, pose.rightWrist!!)

        // Pick average or clearer side
        val avgElbowAngle = (leftAngle + rightAngle) / 2.0

        var instructionMessage = ""

        when (currentState) {
            PushupState.IDLE -> {
                if (avgElbowAngle >= 145.0) {
                    currentState = PushupState.READY
                    instructionMessage = "Position parfaite ! Maintiens le corps droit."
                } else {
                    instructionMessage = "Tends les bras en position de planche."
                }
            }

            PushupState.READY -> {
                instructionMessage = "Position prête. Commence à descendre."
                if (avgElbowAngle < 140.0) {
                    currentState = PushupState.DESCENDING
                    minBottomAngleReached = avgElbowAngle
                    instructionMessage = "Descends doucement..."
                }
            }

            PushupState.DESCENDING -> {
                if (avgElbowAngle < minBottomAngleReached) {
                    minBottomAngleReached = avgElbowAngle
                }
                instructionMessage = "Descends encore un peu..."

                if (avgElbowAngle <= 95.0) {
                    currentState = PushupState.BOTTOM
                    lastStateChangeTimeMs = now
                    instructionMessage = "Point le plus bas atteint ! Repousse le sol."
                } else if (avgElbowAngle > 155.0) {
                    // Canceled move, returned up without going deep
                    currentState = PushupState.READY
                    instructionMessage = "Descends plus bas pour valider la pompe."
                }
            }

            PushupState.BOTTOM -> {
                instructionMessage = "Repousse le sol !"
                if (avgElbowAngle > 105.0) {
                    currentState = PushupState.ASCENDING
                    instructionMessage = "Remonte jusqu'en haut !"
                }
            }

            PushupState.ASCENDING -> {
                instructionMessage = "Presque en haut..."
                if (avgElbowAngle >= 155.0) {
                    // Ensure cooldown of at least 300ms between reps to filter fake bounces
                    if (now - lastStateChangeTimeMs > 250) {
                        repCount++
                        currentState = PushupState.COMPLETED
                        lastStateChangeTimeMs = now
                        instructionMessage = "Pompe validée ! +1 🔥"
                    } else {
                        currentState = PushupState.READY
                    }
                } else if (avgElbowAngle < 90.0) {
                    currentState = PushupState.BOTTOM
                }
            }

            PushupState.COMPLETED -> {
                currentState = PushupState.READY
                instructionMessage = "Super ! Continue pour la suivante."
            }
        }

        return PushupFeedback(
            state = currentState,
            count = repCount,
            primaryAngle = avgElbowAngle,
            instructionMessage = instructionMessage,
            isBodyFullyVisible = true,
            lastRepTimeMs = lastStateChangeTimeMs
        )
    }

    private fun calculateAngle(
        first: Point3D,
        middle: Point3D,
        last: Point3D
    ): Double {
        val rad = atan2((last.y - middle.y).toDouble(), (last.x - middle.x).toDouble()) -
                atan2((first.y - middle.y).toDouble(), (first.x - middle.x).toDouble())
        var angle = Math.toDegrees(kotlin.math.abs(rad))
        if (angle > 180.0) {
            angle = 360.0 - angle
        }
        return angle
    }
}
