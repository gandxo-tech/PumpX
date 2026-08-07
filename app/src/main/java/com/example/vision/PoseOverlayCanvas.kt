package com.example.vision

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun PoseOverlayCanvas(
    landmarkData: PoseLandmarkData?,
    feedback: PushupFeedback,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (landmarkData == null || !landmarkData.isBodyVisible) return@Canvas

        val w = size.width
        val h = size.height

        // Scaled landmark helper
        fun getScaled(point: Point3D?): Offset? {
            if (point == null || point.likelihood < 0.4f) return null
            val imgW = if (landmarkData.imageWidth > 0) landmarkData.imageWidth.toFloat() else 1f
            val imgH = if (landmarkData.imageHeight > 0) landmarkData.imageHeight.toFloat() else 1f
            val scaleX = w / imgW
            val scaleY = h / imgH
            return Offset(point.x * scaleX, point.y * scaleY)
        }

        val ls = getScaled(landmarkData.leftShoulder)
        val rs = getScaled(landmarkData.rightShoulder)
        val le = getScaled(landmarkData.leftElbow)
        val re = getScaled(landmarkData.rightElbow)
        val lw = getScaled(landmarkData.leftWrist)
        val rw = getScaled(landmarkData.rightWrist)
        val lh = getScaled(landmarkData.leftHip)
        val rh = getScaled(landmarkData.rightHip)
        val lk = getScaled(landmarkData.leftKnee)
        val rk = getScaled(landmarkData.rightKnee)
        val la = getScaled(landmarkData.leftAnkle)
        val ra = getScaled(landmarkData.rightAnkle)

        val boneColor = when (feedback.state) {
            PushupState.BOTTOM, PushupState.COMPLETED -> Color(0xFF10B981) // Green
            PushupState.DESCENDING, PushupState.ASCENDING -> Color(0xFF3B82F6) // Blue
            PushupState.READY -> Color(0xFF06B6D4) // Cyan
            PushupState.IDLE -> Color(0xFFF97316) // Orange
        }

        fun drawBone(start: Offset?, end: Offset?) {
            if (start != null && end != null) {
                drawLine(
                    color = boneColor,
                    start = start,
                    end = end,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
            }
        }

        fun drawJoint(point: Offset?) {
            if (point != null) {
                drawCircle(
                    color = Color.White,
                    radius = 12f,
                    center = point
                )
                drawCircle(
                    color = boneColor,
                    radius = 8f,
                    center = point
                )
            }
        }

        // Draw Skeletal Lines
        drawBone(ls, rs)
        drawBone(ls, le)
        drawBone(le, lw)
        drawBone(rs, re)
        drawBone(re, rw)
        drawBone(ls, lh)
        drawBone(rs, rh)
        drawBone(lh, rh)
        drawBone(lh, lk)
        drawBone(lk, la)
        drawBone(rh, rk)
        drawBone(rk, ra)

        // Draw Joint Points
        listOf(ls, rs, le, re, lw, rw, lh, rh, lk, rk, la, ra).forEach {
            drawJoint(it)
        }
    }
}
