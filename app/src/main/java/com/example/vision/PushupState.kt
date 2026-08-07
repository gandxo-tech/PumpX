package com.example.vision

enum class PushupState {
    IDLE,        // Standing or body not detected
    READY,       // In plank position (elbow angle ~160° - 180°)
    DESCENDING,  // Lowering body (elbow angle decreasing)
    BOTTOM,      // At lowest point (elbow angle < 90° - 100°)
    ASCENDING,   // Pushing back up (elbow angle increasing)
    COMPLETED    // Full rep validated, increments count
}

data class PushupFeedback(
    val state: PushupState = PushupState.IDLE,
    val count: Int = 0,
    val primaryAngle: Double = 180.0,
    val instructionMessage: String = "Place-toi dans le cadre pour commencer",
    val isBodyFullyVisible: Boolean = false,
    val lastRepTimeMs: Long = 0L
)
