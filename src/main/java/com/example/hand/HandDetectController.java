package com.example.hand;

import java.util.Base64;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.bytedeco.opencv.global.opencv_core.CV_8U;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
import static org.bytedeco.opencv.global.opencv_core.countNonZero;
import static org.bytedeco.opencv.global.opencv_core.inRange;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgproc.CHAIN_APPROX_SIMPLE;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2YCrCb;
import static org.bytedeco.opencv.global.opencv_imgproc.MORPH_CLOSE;
import static org.bytedeco.opencv.global.opencv_imgproc.MORPH_OPEN;
import static org.bytedeco.opencv.global.opencv_imgproc.RETR_EXTERNAL;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.erode;
import static org.bytedeco.opencv.global.opencv_imgproc.findContours;
import static org.bytedeco.opencv.global.opencv_imgproc.getStructuringElement;
import static org.bytedeco.opencv.global.opencv_imgproc.morphologyEx;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.javacpp.indexer.IntIndexer;

@RestController
public class HandDetectController {

    @PostMapping(value = "/api/hand", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HandResponse detect(@RequestBody HandRequest request) {
        if (request == null || request.image == null || request.image.isEmpty()) {
            return HandResponse.empty("empty");
        }
        byte[] bytes = decodeBase64(request.image);
        if (bytes == null || bytes.length == 0) {
            return HandResponse.empty("decode_failed");
        }
        return detectHand(bytes);
    }

    private static byte[] decodeBase64(String dataUrl) {
        String payload = dataUrl;
        int comma = dataUrl.indexOf(',');
        if (comma >= 0) {
            payload = dataUrl.substring(comma + 1);
        }
        try {
            return Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static HandResponse detectHand(byte[] bytes) {
        Mat buf = new Mat(1, bytes.length, CV_8U);
        buf.data().put(bytes);
        Mat input = imdecode(buf, 1);
        if (input == null || input.empty()) {
            return HandResponse.empty("image_decode_failed");
        }

        int targetWidth = 320;
        double scale = targetWidth / (double) input.cols();
        int targetHeight = Math.max(1, (int) Math.round(input.rows() * scale));
        Mat resized = new Mat();
        resize(input, resized, new Size(targetWidth, targetHeight));

        Mat ycrcb = new Mat();
        cvtColor(resized, ycrcb, COLOR_BGR2YCrCb);

        Mat lower = new Mat(ycrcb.size(), ycrcb.type(), new Scalar(0, 133, 77, 0));
        Mat upper = new Mat(ycrcb.size(), ycrcb.type(), new Scalar(255, 173, 127, 0));
        Mat mask = new Mat(resized.size(), CV_8UC1);
        inRange(ycrcb, lower, upper, mask);

        Mat kernel = getStructuringElement(2, new Size(5, 5));
        morphologyEx(mask, mask, MORPH_OPEN, kernel);
        morphologyEx(mask, mask, MORPH_CLOSE, kernel);
        erode(mask, mask, getStructuringElement(2, new Size(3, 3)));

        if (countNonZero(mask) < 2000) {
            return HandResponse.empty("mask_too_small");
        }

        MatVector contours = new MatVector();
        findContours(mask, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        if (contours.size() == 0) {
            return HandResponse.empty("no_contour");
        }

        int bestIdx = -1;
        double bestArea = 0;
        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double area = Math.abs(org.bytedeco.opencv.global.opencv_imgproc.contourArea(contour));
            if (area > bestArea) {
                bestArea = area;
                bestIdx = i;
            }
        }
        if (bestIdx < 0 || bestArea < 1500) {
            return HandResponse.empty("small_contour");
        }

        Mat contour = contours.get(bestIdx);
        Rect box = org.bytedeco.opencv.global.opencv_imgproc.boundingRect(contour);

        Point topPoint = new Point(box.x() + box.width() / 2, box.y());
        for (int i = 0; i < contour.rows(); i++) {
            IntPointer ptr = new IntPointer(contour.ptr(i));
            int px = ptr.get(0);
            int py = ptr.get(1);
            if (py < topPoint.y()) {
                topPoint = new Point(px, py);
            }
        }

        Mat hull = new Mat();
        org.bytedeco.opencv.global.opencv_imgproc.convexHull(contour, hull, false, false);

        Mat defects = new Mat();
        int defectCount = 0;
        if (hull.rows() > 3) {
            org.bytedeco.opencv.global.opencv_imgproc.convexityDefects(contour, hull, defects);
            if (!defects.empty()) {
                long total = defects.total();
                IntIndexer idx = defects.createIndexer();
                for (int i = 0; i + 3 < total; i += 4) {
                    int depth = idx.get(i + 3);
                    if (depth > 4000) {
                        defectCount++;
                    }
                }
                idx.release();
            }
        }

        String gesture = "unknown";
        if (defectCount >= 3) {
            gesture = "open";
        } else if (defectCount <= 1) {
            gesture = "pinch";
        } else if (defectCount == 2) {
            gesture = "peace";
        }

        double x = clamp(topPoint.x() / (double) resized.cols());
        double y = clamp(topPoint.y() / (double) resized.rows());
        return new HandResponse(true, x, y, gesture, defectCount);
    }

    private static double clamp(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    public static class HandRequest {
        public String image;
    }

    public static class HandResponse {
        public boolean hasHand;
        public double x;
        public double y;
        public String gesture;
        public int defects;
        public String status;

        public HandResponse(boolean hasHand, double x, double y, String gesture, int defects) {
            this.hasHand = hasHand;
            this.x = x;
            this.y = y;
            this.gesture = gesture;
            this.defects = defects;
            this.status = "ok";
        }

        public static HandResponse empty(String status) {
            HandResponse resp = new HandResponse(false, 0, 0, "none", 0);
            resp.status = status;
            return resp;
        }
    }
}
