/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.poselandmarker.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private var logger: Logger = LoggerFactory.getLogger(OverlayView::class.java)
    private var centerOfGravity : CenterOfGravity = CenterOfGravity()

    private var bedConnerDetection : BedConnerDetection = BedConnerDetection()

    private var results: PoseLandmarkerResult? = null

    private var pointPaint = Paint()
    private var greenPaint = Paint()
    private var yellowPaint = Paint()
    private var redPaint = Paint()
    private var blackPaint = Paint()
    private var linePaint = Paint()
    private var linePaint2 = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private var leftBedLim: Float = 0f
    private var rightBedLim: Float = 0f
    private var leftDangerLim: Float = 0f
    private var rightDangerLim: Float = 0f


    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        redPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_secondary_variant)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        linePaint2.color =
            ContextCompat.getColor(context!!, R.color.mp_color_error)
        linePaint2.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint2.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        greenPaint.color = Color.GREEN
        greenPaint.strokeWidth = 24F
        greenPaint.style = Paint.Style.FILL

        yellowPaint.color = Color.YELLOW
        yellowPaint.strokeWidth = 24F
        yellowPaint.style = Paint.Style.FILL

        redPaint.color = Color.RED
        redPaint.strokeWidth = 24F
        redPaint.style = Paint.Style.FILL

        blackPaint.color = Color.BLACK
        blackPaint.strokeWidth = 24F
        blackPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val scaleX = imageWidth * scaleFactor
        val scaleY = scaleY


        results?.let { poseLandmarkerResult ->
            for(landmark in poseLandmarkerResult.landmarks()) {
                // 포인트
                for(normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * scaleX,
                        normalizedLandmark.y() * scaleY,
                        pointPaint
                    )
                }

                // 라인
                PoseLandmarker.POSE_LANDMARKS.forEach {
                    canvas.drawLine(
                        poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * scaleX,
                        poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * scaleY,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).x() * scaleX,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * scaleY,
                        linePaint)
                }

                canvas.drawLine(
                    (bedConnerDetection.normalizedLx1 * scaleX),
                    (bedConnerDetection.normalizedLy1 * scaleY),
                    (bedConnerDetection.normalizedLx2 * scaleX),
                    (bedConnerDetection.normalizedLy2 * scaleY),
                    linePaint2)

                canvas.drawLine(
                    (bedConnerDetection.normalizedRx1 * scaleX),
                    (bedConnerDetection.normalizedRy1 * scaleY),
                    (bedConnerDetection.normalizedRx2 * scaleX),
                    (bedConnerDetection.normalizedRy2 * scaleY),
                    linePaint2)

                canvas.drawLine(
                    (bedConnerDetection.dangerLx1 * scaleX),
                    (bedConnerDetection.dangerLy1 * scaleY),
                    (bedConnerDetection.dangerLx2 * scaleX),
                    (bedConnerDetection.dangerLy2 * scaleY),
                    linePaint2)

                canvas.drawLine(
                    (bedConnerDetection.dangerRx1 * scaleX),
                    (bedConnerDetection.dangerRy1 * scaleY),
                    (bedConnerDetection.dangerRx2 * scaleX),
                    (bedConnerDetection.dangerRy2 * scaleY),
                    linePaint2)



                // 중심
                var center = centerOfGravity.getTotalCOG(landmark)

                leftBedLim = get_Limit(bedConnerDetection.normalizedLx1 * imageWidth * scaleFactor,
                    bedConnerDetection.normalizedLx2 * imageWidth * scaleFactor,
                    bedConnerDetection.normalizedLy1 * scaleY,
                    bedConnerDetection.normalizedLy2 * scaleY,
                    center.y() * scaleY)

                rightBedLim = get_Limit(bedConnerDetection.normalizedRx1 * imageWidth * scaleFactor,
                    bedConnerDetection.normalizedRx2 * imageWidth * scaleFactor,
                    bedConnerDetection.normalizedRy1 * scaleY,
                    bedConnerDetection.normalizedRy2 * scaleY,
                    center.y() * scaleY)

                leftDangerLim = get_Limit(bedConnerDetection.dangerLx1 * imageWidth * scaleFactor,
                    bedConnerDetection.dangerLx2 * imageWidth * scaleFactor,
                    bedConnerDetection.dangerLy1 * scaleY,
                    bedConnerDetection.dangerLy2 * scaleY,
                    center.y() * scaleY)

                rightDangerLim = get_Limit(bedConnerDetection.dangerRx1 * imageWidth * scaleFactor,
                    bedConnerDetection.dangerRx2 * imageWidth * scaleFactor,
                    bedConnerDetection.dangerRy1 * scaleY,
                    bedConnerDetection.dangerRy2 * scaleY,
                    center.y() * scaleY)



                var centerX: Float = center.x() * imageWidth * scaleFactor

                if ((centerX > leftDangerLim) and (centerX < rightDangerLim)){ // phase 1

                    logger.info("result : " + "(" + center.x() + ", " + center.y() + ")")
                    canvas.drawPoint(
                        center.x() * imageWidth * scaleFactor,
                        center.y() * scaleY,
                        greenPaint
                    )

                }
                else if ((centerX <= leftDangerLim)
                    and (centerX >= rightDangerLim)
                    and (centerX > leftBedLim)
                    and (centerX < rightBedLim)){ // phase 2~3

                    logger.info("result : " + "(" + center.x() + ", " + center.y() + ")")
                    canvas.drawPoint(
                        center.x() * imageWidth * scaleFactor,
                        center.y() * scaleY,
                        redPaint
                    )

                }
                else if ((centerX <= leftBedLim)
                    and (centerX >= rightBedLim)){ // phase 4

                    logger.info("result : " + "(" + center.x() + ", " + center.y() + ")")
                    canvas.drawPoint(
                        center.x() * imageWidth * scaleFactor,
                        center.y() * scaleY,
                        blackPaint
                    )

                }


            }

        }
    }

    private fun get_Limit(x1: Float, x2: Float, y1: Float, y2: Float, centerY: Float): Float{
        var coe: Float = (y2-y1)/(x2-x1)
        var limi = ((centerY-y1)/(coe))+x1

        return limi
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}