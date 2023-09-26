package com.google.mediapipe.examples.poselandmarker;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import android.graphics.Bitmap;

public class BedConnerDetection {
    private static final int DESIRED_ANGLE = 70;
    private static final int MARGIN_OF_ERROR = 20;
    private static final float RATE_OF_DANGER = (float) 0.3;

    private static final double tan_pos_angle = Math.tan(Math.toRadians(DESIRED_ANGLE));
    private static final double tan_neg_angle = Math.tan(Math.toRadians(-DESIRED_ANGLE));
    private static final double tan_pos_upper_margin = Math.tan(Math.toRadians(DESIRED_ANGLE + MARGIN_OF_ERROR));
    private static final double tan_pos_lower_margin = Math.tan(Math.toRadians(DESIRED_ANGLE - MARGIN_OF_ERROR));
    private static final double tan_neg_upper_margin = Math.tan(Math.toRadians(-DESIRED_ANGLE + MARGIN_OF_ERROR));
    private static final double tan_neg_lower_margin = Math.tan(Math.toRadians(-DESIRED_ANGLE - MARGIN_OF_ERROR));

    private static int bmpWidth = 0, bmpHeight = 0;

    public int getBmpWidth() {
        return bmpWidth;
    }

    public int getBmpHeight() {
        return bmpHeight;
    }

    private static double lx1, ly1, lx2, ly2;
    private static double rx1, ry1, rx2, ry2;

    private static float normalizedLx1, normalizedLy1, normalizedLx2, normalizedLy2;
    private static float normalizedRx1, normalizedRy1, normalizedRx2, normalizedRy2;
    private static float dangerLx1, dangerLy1, dangerLx2, dangerLy2;
    private static float dangerRx1, dangerRy1, dangerRx2, dangerRy2;

    public double getLx1() {
        return lx1;
    }

    public double getLy1() {
        return ly1;
    }

    public double getLx2() {
        return lx2;
    }

    public double getLy2() {
        return ly2;
    }

    public double getRx1() {
        return rx1;
    }

    public double getRy1() {
        return ry1;
    }

    public double getRx2() {
        return rx2;
    }

    public double getRy2() {
        return ry2;
    }

    public float getNormalizedLx1() {
        return normalizedLx1;
    }

    public float getNormalizedLy1() {
        return normalizedLy1;
    }

    public float getNormalizedLx2() {
        return normalizedLx2;
    }

    public float getNormalizedLy2() {
        return normalizedLy2;
    }

    public float getNormalizedRx1() {
        return normalizedRx1;
    }

    public float getNormalizedRy1() {
        return normalizedRy1;
    }

    public float getNormalizedRx2() {
        return normalizedRx2;
    }

    public float getNormalizedRy2() {
        return normalizedRy2;
    }

    // 위험
    public float getDangerLx1() {
        return dangerLx1;
    }

    public float getDangerLy1() {
        return dangerLy1;
    }

    public float getDangerLx2() {
        return dangerLx2;
    }

    public float getDangerLy2() {
        return dangerLy2;
    }

    public float getDangerRx1() {
        return dangerRx1;
    }

    public float getDangerRy1() {
        return dangerRy1;
    }

    public float getDangerRx2() {
        return dangerRx2;
    }

    public float getDangerRy2() {
        return dangerRy2;
    }

    public Bitmap detectBedBorders(Bitmap bitmap) {
        bmpWidth = bitmap.getWidth();
        bmpHeight = bitmap.getHeight();

        Mat frame = new Mat();
        Utils.bitmapToMat(bitmap, frame);

        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

        Mat blur = new Mat();
        Imgproc.bilateralFilter(gray, blur, 10, 75, 75);

        Mat edges = new Mat();
        Imgproc.Canny(blur, edges, 50, 150);

        int height = edges.rows();
        int width = edges.cols();

        Mat left_half = edges.submat(new Rect(0, 0, width/2, height));
        Mat right_half = edges.submat(new Rect(width/2, 0, width/2, height));


        MatOfInt4 left_lines = new MatOfInt4();
        Imgproc.HoughLinesP(left_half, left_lines, 1, Math.PI/180, 40, 60, 10);
        double maxL = 0.0f;
        for (int i = 0; i < left_lines.rows(); i++) {
            double[] vec = left_lines.get(i, 0);
            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            double m = (y2 - y1) / (x2 - x1);

            if (tan_neg_lower_margin < m && m < tan_neg_upper_margin) {

                double distanceN = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                if (maxL < distanceN) {
                    lx1 = x1 - y1 / m;
                    ly1 = 0;
                    lx2 = x1 + (height - y1) / m;
                    ly2 = height;
                    maxL = distanceN;
                }
                //Imgproc.line(frame, start, end, new Scalar(0, 0, 255), 4);
            }
        }

        MatOfInt4 right_lines = new MatOfInt4();
        Imgproc.HoughLinesP(right_half, right_lines, 1, Math.PI/180, 40, 60, 10);
        maxL = 0.0f;
        for (int i = 0; i < right_lines.rows(); i++) {
            double[] vec = right_lines.get(i, 0);
            double x1 = vec[0] + width/2, y1 = vec[1], x2 = vec[2] + width/2, y2 = vec[3];
            double m = (y2 - y1) / (x2 - x1);

            if (tan_pos_lower_margin < m && m < tan_pos_upper_margin) {

                double distanceN = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                if (maxL < distanceN) {
                    rx1 = x1 - y1 / m;
                    ry1 = 0;
                    rx2 = x1 + (height - y1) / m;
                    ry2 = height;
                    maxL = distanceN;
                }
                //Imgproc.line(frame, start, end, new Scalar(0, 0, 255), 4);
            }

            // 이미지의 크기로 좌표를 정규화
            normalizedLx1 = (float) (lx1 / bmpWidth);
            normalizedLy1 = (float) (ly1 / bmpHeight);
            normalizedLx2 = (float) (lx2 / bmpWidth);
            normalizedLy2 = (float) (ly2 / bmpHeight);

            normalizedRx1 = (float) (rx1 / bmpWidth);
            normalizedRy1 = (float) (ry1 / bmpHeight);
            normalizedRx2 = (float) (rx2 / bmpWidth);
            normalizedRy2 = (float) (ry2 / bmpHeight);


            // 위험 영역
            dangerLx1 = normalizedLx1+(normalizedRx1 - normalizedLx1)*(RATE_OF_DANGER/2);
            dangerLx2 = normalizedLx2+(normalizedRx2 - normalizedLx2)*(RATE_OF_DANGER/2);
            dangerLy1 = normalizedLy1;
            dangerLy2 = normalizedLy2;

            dangerRx1 = normalizedRx1-(normalizedRx1 - normalizedLx1)*(RATE_OF_DANGER/2);
            dangerRx2 = normalizedRx2-(normalizedRx2 - normalizedLx2)*(RATE_OF_DANGER/2);
            dangerRy1 = normalizedRy1;
            dangerRy2 = normalizedRy2;

        }
        Utils.matToBitmap(frame, bitmap);
        return bitmap;
    }
}
