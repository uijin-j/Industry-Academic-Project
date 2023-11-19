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
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.Delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random
import android.widget.Toast;

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

    private var prevPoint: Pair<Float, Float> = Pair(-1f, -1f)
    private var prevTime = System.currentTimeMillis()

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
        val scaleY = imageHeight * scaleFactor


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

                var l1x: Float = bedConnerDetection.normalizedLx1
                var l1y: Float = bedConnerDetection.normalizedLy1
                var l2x: Float = bedConnerDetection.normalizedLx2
                var l2y: Float = bedConnerDetection.normalizedLy2

                var r1x: Float = bedConnerDetection.normalizedRx1
                var r1y: Float = bedConnerDetection.normalizedRy1
                var r2x: Float = bedConnerDetection.normalizedRx2
                var r2y: Float = bedConnerDetection.normalizedRy2

                var dl1x: Float = bedConnerDetection.dangerLx1
                var dl1y: Float = bedConnerDetection.dangerLy1
                var dl2x: Float = bedConnerDetection.dangerLx2
                var dl2y: Float = bedConnerDetection.dangerLy2

                var dr1x: Float = bedConnerDetection.dangerRx1
                var dr1y: Float = bedConnerDetection.dangerRy1
                var dr2x: Float = bedConnerDetection.dangerRx2
                var dr2y: Float = bedConnerDetection.dangerRy2


                canvas.drawLine(
                    (dl1x * scaleX),
                    (dl1y * scaleY),
                    (dl2x * scaleX),
                    (dl2y * scaleY),
                    linePaint)

                canvas.drawLine(
                    (dr1x * scaleX),
                    (dr1y * scaleY),
                    (dr2x * scaleX),
                    (dr2y * scaleY),
                    linePaint)

                canvas.drawLine(
                    (l1x * scaleX),
                    (l1y * scaleY),
                    (l2x * scaleX),
                    (l2y * scaleY),
                    linePaint2)

                canvas.drawLine(
                    (r1x * scaleX),
                    (r1y * scaleY),
                    (r2x * scaleX),
                    (r2y * scaleY),
                    linePaint2)


                // 중심
                var center = centerOfGravity.getTotalCOG(landmark)
                var drawCenterX: Float = center.x() * scaleX
                var drawCenterY: Float = center.y() * scaleY


                var paintCenter: Paint = yellowPaint

                leftBedLim = get_Limit(l1x, l2x, l1y, l2y, center.y())
                rightBedLim = get_Limit(r1x, r2x, r1y, r2y, center.y())
                leftDangerLim = get_Limit(dl1x, dl2x, dl1y, dl2y, center.y())
                rightDangerLim = get_Limit(dr1x, dr2x, dr1y, dr2y, center.y())


                var alertType = -1
                if(center.x() > leftDangerLim && center.x() < rightDangerLim){
                    paintCenter = greenPaint
                } else if(center.x() < leftBedLim || center.x() > rightBedLim) {
                    paintCenter = blackPaint
                    alertType = 4
                } else {
                    alertType = 2
                }

                runDelay({
                    // 여기서 작업하시면 되요
                    Log.d(".Delay", "draw: $prevTime")

                    if(alertType != 2) { // 위험 영역이 아니라면 배열 초기화
                        if (prevPoint.first != -1f){
                            prevPoint = Pair(-1f, -1f)
                        }
                    }

                    if(alertType == 2) { // 이 영역 내부에서 속도 변화 감지

                        if(prevPoint.first != -1f) {
                            var limitRate: Float = 0.3f


                            if (center.x() < leftDangerLim) {
                                var limitDist: Float =
                                    limitRate * getPointDistance(l1x, l2x, l1y, l2y, dl1x, dl1y)

                                var centerDist: Float =
                                    getPointDistance(l1x, l2x, l1y, l2y, center.x(), center.y())

                                var prevDist: Float =
                                    getPointDistance(l1x, l2x, l1y, l2y, prevPoint.first, prevPoint.second)

                                if(prevDist-centerDist < limitDist){
                                    // 왼쪽 영역 3단계
                                    Toast.makeText(context,	"left", Toast.LENGTH_SHORT).show()

                                }



                            } else {
                                var limitDist: Float =
                                    limitRate * getPointDistance(r1x, r2x, r1y, r2y, dr1x, dr1y)

                                var centerDist: Float =
                                    getPointDistance(r1x, r2x, r1y, r2y,  center.x(), center.y())

                                var prevDist: Float =
                                    getPointDistance(r1x, r2x, r1y, r2y, prevPoint.first, prevPoint.second)

                                if(centerDist-prevDist > limitDist){
                                    //오른쪽 영역
                                    Toast.makeText(context,	"right", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        prevPoint = Pair(center.x(), center.y())
                    }
                    else if(alertType == 4) { // 이미 떨어짐
                        context?.sendBroadcast(Intent("com.stlinkproject.action.DANGER_ALERT").apply {
                            putExtra("key_type", alertType)
                        })
                    }
                }, 3000)


                logger.info("result : " + "(" + center.x() + ", " + center.y() + ")")
                canvas.drawPoint(
                    drawCenterX,
                    drawCenterY,
                    paintCenter
                )

            }

        }
    }
    private fun getPointDistance(x1: Float, x2: Float, y1: Float, y2: Float, danger_x: Float, danger_y: Float): Float {
        var coef: Float = (y2-y1)/(x2-x1)
        var bias: Float = y1-(x1 * coef)

        var numerator: Float = abs((coef*danger_x)-(danger_y)+bias)
        var denominator: Float = sqrt(1f + (coef*coef))
        var result: Float = numerator / denominator

        return result
    }

    private fun runDelay(block: () -> Unit, delay: Int) {
        if(System.currentTimeMillis() - prevTime >= delay) {
            prevTime = System.currentTimeMillis()
            block()
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