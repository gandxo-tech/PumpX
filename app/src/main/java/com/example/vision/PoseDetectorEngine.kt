package com.example.vision

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class PoseDetectorEngine(
    private val onPoseDetected: (PoseLandmarkData?) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector: PoseDetector = PoseDetection.getClient(options)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        poseDetector.process(inputImage)
            .addOnSuccessListener { pose ->
                val landmarkData = extractLandmarkData(pose, imageProxy.width, imageProxy.height)
                onPoseDetected(landmarkData)
            }
            .addOnFailureListener {
                onPoseDetected(null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun extractLandmarkData(pose: Pose, width: Int, height: Int): PoseLandmarkData {
        fun getPoint(landmarkType: Int): Point3D? {
            val landmark = pose.getPoseLandmark(landmarkType) ?: return null
            val pos = landmark.position3D
            return Point3D(
                x = pos.x,
                y = pos.y,
                z = pos.z,
                likelihood = landmark.inFrameLikelihood
            )
        }

        return PoseLandmarkData(
            leftShoulder = getPoint(PoseLandmark.LEFT_SHOULDER),
            rightShoulder = getPoint(PoseLandmark.RIGHT_SHOULDER),
            leftElbow = getPoint(PoseLandmark.LEFT_ELBOW),
            rightElbow = getPoint(PoseLandmark.RIGHT_ELBOW),
            leftWrist = getPoint(PoseLandmark.LEFT_WRIST),
            rightWrist = getPoint(PoseLandmark.RIGHT_WRIST),
            leftHip = getPoint(PoseLandmark.LEFT_HIP),
            rightHip = getPoint(PoseLandmark.RIGHT_HIP),
            leftKnee = getPoint(PoseLandmark.LEFT_KNEE),
            rightKnee = getPoint(PoseLandmark.RIGHT_KNEE),
            leftAnkle = getPoint(PoseLandmark.LEFT_ANKLE),
            rightAnkle = getPoint(PoseLandmark.RIGHT_ANKLE),
            imageWidth = width,
            imageHeight = height
        )
    }

    fun close() {
        poseDetector.close()
    }
}
