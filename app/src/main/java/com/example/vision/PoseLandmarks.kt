package com.example.vision

import androidx.compose.ui.geometry.Offset

data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float = 0f,
    val likelihood: Float = 1f
)

data class PoseLandmarkData(
    val leftShoulder: Point3D?,
    val rightShoulder: Point3D?,
    val leftElbow: Point3D?,
    val rightElbow: Point3D?,
    val leftWrist: Point3D?,
    val rightWrist: Point3D?,
    val leftHip: Point3D?,
    val rightHip: Point3D?,
    val leftKnee: Point3D?,
    val rightKnee: Point3D?,
    val leftAnkle: Point3D?,
    val rightAnkle: Point3D?,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0
) {
    val isBodyVisible: Boolean
        get() {
            val essential = listOf(
                leftShoulder, rightShoulder,
                leftElbow, rightElbow,
                leftWrist, rightWrist,
                leftHip, rightHip
            )
            return essential.all { it != null && it.likelihood > 0.5f }
        }
}
