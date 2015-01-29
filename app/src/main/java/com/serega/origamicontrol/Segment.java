package com.serega.origamicontrol;

import android.graphics.*;
import android.util.Log;

public class Segment {
    private Bitmap bitmap;
    private int color;
    private final Paint paint;
    private final Camera camera;
    private float currentAngle = 0;
    private int currentDistanceStep = 25;
    private final Rect region;
    public boolean update;

    private float currentY;

    public float initY;

    boolean moveTop;

    boolean process = false;

    public SType type = SType.DEFAULT;

    private final Paint linePaint = new Paint();

    public final Rect bitmapArea = new Rect();

    public int maxHeight;

    public Rect rectTop = new Rect();
    public Rect rectBottom = new Rect();

    public Rect bitmapTop = new Rect();
    public Rect bitmapBottom = new Rect();

    public Rect bitmapAreaTop = new Rect();
    public Rect bitmapAreaBottom = new Rect();
    public Bitmap topBitmap;

    public void setTopBitmap() {
        topBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapAreaTop.width(), bitmapAreaTop.height());
    }

    public enum SType {
        DEFAULT,
        TOP,
        BOTTOM,
        ROTATE_AREA
    }

    public Segment() {
        paint = new Paint();
        camera = new Camera();
        region = new Rect();
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(15);
    }

    public void setRegion(Rect r) {
        region.set(r);
        bitmapArea.set(r);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }


    public void update(float y) {
        moveTop = y < currentY;
        update = true;
        currentY = y;
    }

    public void draw(Canvas canvas) {
        if (update) {
            move();
            update = false;
        }
        if (bitmap != null) {
            canvas.save();
            if (type == SType.ROTATE_AREA) {
                drawRotate(canvas);
            } else {
                canvas.drawBitmap(bitmap, bitmapArea, region, paint);
            }
            canvas.restore();
        }
    }

    private void drawRotate1(Canvas canvas) {
//		calculateRotateTop(bitmapTop.left, bitmapTop.top, bitmapTop.right, bitmapTop.bottom);

        camera.save();
//        currentAngle = calculateAngleTop(bitmapTop.bottom - bitmapTop.top);
        currentAngle = MIN_ANGLE * (bitmapTop.top) / (initY);
        Log.i("m_lll", currentAngle + "");
        int tX = (bitmapTop.width()) / 2;
        int tY = (int) bitmapTop.top;
        float koeff = bitmapTop.top / initY;
        if (koeff + 0.2 <= 1) {
            koeff += 0.2f;
        }
        Log.i("M_KOEFF", "" + koeff);
        double val = bitmapTop.top;
        camera.rotateX(currentAngle * koeff);
        camera.translate(0, -(float) val, 0);
        camera.getMatrix(matrix);

//        matrix.preTranslate(-tX, -tY);
//        matrix.postTranslate(tX, tY);

        canvas.save();
        canvas.concat(matrix);

//        canvas.drawBitmap(bitmap, bitmapAreaTop, bitmapAreaTop, paint);

        canvas.restore();
        camera.restore();
        int width = bitmapAreaTop.width();
        int height = bitmapAreaTop.height();
        if (width == 0 || height == 0) {
            return;
        }
        Bitmap b = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        canvas.drawBitmap(b, 0, 0, paint);
        canvas.drawLine(0, bitmapTop.bottom, bitmapTop.right, bitmapTop.bottom, linePaint);

//		calculateRotateBottom(bitmapBottom.left, bitmapBottom.top, bitmapBottom.right, bitmapBottom.bottom);

//        camera.save();
//        currentAngle = - currentAngle;
//        tY = bitmapBottom.bottom;
//        camera.rotateX(currentAngle);
//        camera.translate(0, maxHeight - bitmapBottom.bottom, 0);
//        camera.getMatrix(matrix);
//        camera.restore();
//
//        matrix.preTranslate(-tX, -tY);
//        matrix.postTranslate(tX, tY);
//
//		canvas.save();
//		canvas.concat(matrix);
//		canvas.drawBitmap(bitmap, bitmapAreaBottom, bitmapAreaBottom, paint);
//		canvas.restore();
    }

    private void drawRotate(Canvas canvas) {
        matrix.reset();
        camera.save();
        currentAngle = MIN_ANGLE * (bitmapTop.top) / (initY);

        Log.i("m_lll", currentAngle + "");
        int tX = (bitmapTop.width()) / 2;
        int tY = (int) bitmapTop.top;
        double val = bitmapTop.top;
        camera.rotateX(currentAngle);
        camera.translate(0, -(float) val, 0);
        camera.getMatrix(matrix);

        matrix.preTranslate(-tX, -tY);
        matrix.postTranslate(tX, tY);
        camera.restore();
        canvas.save();

        int width = topBitmap.getWidth();
        int height = topBitmap.getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        if (currentAngle > MIN_ANGLE+2) {
            Bitmap b = Bitmap.createBitmap(topBitmap, 0, 0, width, height, matrix, false);
            int left = bitmapTop.left;
            int bottom = bitmapTop.bottom;
            int right = bitmapTop.right;
            int top = bottom - b.getHeight();
            bitmapTop.set(left, top, right, bottom);
            canvas.drawBitmap(b, bitmapTop.left, bitmapTop.bottom - b.getHeight(), paint);
            canvas.drawLine(0, bitmapTop.bottom, bitmapTop.right, bitmapTop.bottom, linePaint);
            Log.i("M_TOP_RECT", bitmapTop.toString() + ", rectHeight=" + bitmapTop.height() + ", bitmapHeight=" + b.getHeight());
        }
        canvas.restore();
//		calculateRotateBottom(bitmapBottom.left, bitmapBottom.top, bitmapBottom.right, bitmapBottom.bottom);

//        camera.save();
//        currentAngle = - currentAngle;
//        tY = bitmapBottom.bottom;
//        camera.rotateX(currentAngle);
//        camera.translate(0, maxHeight - bitmapBottom.bottom, 0);
//        camera.getMatrix(matrix);
//        camera.restore();
//
//        matrix.preTranslate(-tX, -tY);
//        matrix.postTranslate(tX, tY);
//
//		canvas.save();
//		canvas.concat(matrix);
//		canvas.drawBitmap(bitmap, bitmapAreaBottom, bitmapAreaBottom, paint);
//		canvas.restore();
    }

    public Rect getBitmapTop() {
        return bitmapTop;
    }

    public void setBitmapTop(Rect rect) {
        this.bitmapTop = rect;
    }

    private void move() {
        switch (type) {
            case DEFAULT:

                return;

            case ROTATE_AREA:
                calculateMoveRotateTop(bitmapTop.left, bitmapTop.top, bitmapTop.right, bitmapTop.bottom);
                calculateMoveRotateBottom(bitmapBottom.left, bitmapBottom.top, bitmapBottom.right, bitmapBottom.bottom);

                break;

            case TOP:
                calculateTop(region.left, region.top, region.right, region.bottom);
                break;

            case BOTTOM:
                calculateBottom(region.left, region.top, region.right, region.bottom);
                break;
        }
    }

    private void calculateTop(int left, int top, int right, int bottom) {
//        if (moveTop) {
//            if (bottom - currentDistanceStep > 0) {
//                region.set(left, top, right, bottom - currentDistanceStep);
//
//                int bTop = bitmapArea.top + currentDistanceStep;
//                int bBottom = bitmapArea.bottom;
//                bitmapArea.set(left, bTop, right, bBottom);
//            } else {
//                update = false;
//            }
//        } else {
//            if (bottom + currentDistanceStep < initY) {
//                region.set(left, top, right, bottom + currentDistanceStep);
//                int bTop = bitmapArea.top - currentDistanceStep;
//                int bBottom = bitmapArea.bottom;
//                bitmapArea.set(left, bTop, right, bBottom);
//            } else {
//                update = false;
//            }
//        }

        region.set(left, top, right, bitmapTop.top);

//        int bTop = bitmapArea.top + currentDistanceStep;
//        int bBottom = bitmapArea.bottom;
//        bitmapArea.set(left, bTop, right, bBottom);
    }

    private void calculateBottom(int left, int top, int right, int bottom) {
        if (moveTop) {
            if (top + currentDistanceStep < bottom) {
                region.set(left, top + currentDistanceStep, right, bottom);
                int bTop = bitmapArea.top;
                int bBottom = bitmapArea.bottom - currentDistanceStep;
                bitmapArea.set(left, bTop, right, bBottom);

            } else {
                update = false;
            }

        } else {
            if (top - currentDistanceStep > initY) {
                region.set(left, top - currentDistanceStep, right, bottom);
                int bTop = bitmapArea.top;
                int bBottom = bitmapArea.bottom + currentDistanceStep;
                bitmapArea.set(left, bTop, right, bBottom);

            } else {
                update = false;
            }
        }
    }

    private void calculateMoveRotateTop(int left, int top, int right, int bottom) {
        if (moveTop) {
            if (top - currentDistanceStep > 0) {
                bitmapTop.set(left, top - currentDistanceStep, right, bottom);
            } else {
                update = false;
            }
        } else {
            if (top + currentDistanceStep < initY) {
                bitmapTop.set(left, top + currentDistanceStep, right, bottom);
            } else {
                update = false;
            }
        }
    }

    private void calculateMoveRotateBottom(int left, int top, int right, int bottom) {
        if (moveTop) {
            if (bottom + currentDistanceStep < maxHeight) {
                bitmapBottom.set(left, top, right, bottom + currentDistanceStep);
            } else {
                update = false;
            }

        } else {
            if (bottom - currentDistanceStep > initY) {
                bitmapBottom.set(left, top, right, bottom - currentDistanceStep);
            } else {
                update = false;
            }
        }
    }

    Matrix matrix = new Matrix();

    private void calculateRotateTop(int left, int top, int right, int bottom) {
        camera.save();
        currentAngle = calculateAngleTop(bottom - top);
        Log.i("m_lll", currentAngle + "");
//	    Log.i("m_angle_rotate", "angle=" + currentAngle + ", initY=" + initY + ", top=" + top + ", bottom=" + bottom );
        int tX = (right - left) / 2;
        int tY = (int) top;
//		double val = top * Math.cos(currentAngle);
        double val = top;
//		double val = top * Math.cos(currentAngle)*Math.sqrt(initY*initY-bitmapTop.height()*bitmapTop.height());
//		Log.i("M_VAL", val+"");
        camera.rotateX(currentAngle);
        camera.translate(0, -(float) val, 0);
        camera.getMatrix(matrix);
        matrix.preTranslate(-tX, -tY);
        matrix.postTranslate(tX, tY);


        camera.restore();

    }

    private void calculateRotateBottom(int left, int top, int right, int bottom) {
        camera.save();
        currentAngle = calculateAngleBottom(bottom - top);
//		currentAngle = -currentAngle;
//	    currentAngle = 45;
//        int bb = bottom;
//        double cX = bb * Math.cos(Math.toRadians(currentAngle));
//        Log.i("M_TOP", "BOTTOM_ROTATE bb=" + bb + ", cX=" + cX + ", angle=" + currentAngle);
        Log.i("m_angle_rotate", "angle=" + currentAngle + ", initY=" + initY + ", top=" + top + ", bottom=" + bottom);
        int tX = (right - left) / 2;
        float tY = bottom;
        camera.rotateX(currentAngle);
        camera.translate(0, maxHeight - bottom, 0);
        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-tX, -tY);
        matrix.postTranslate(tX, tY);
    }

    private static final int MAX_ANGLE = 0;
    private static final float MIN_ANGLE = -90;

    private float calculateAngleBottom(int AB) {
        double w = maxHeight - initY;
        double sinAlpha = AB / w;
        double alpha = Math.asin(sinAlpha);
        Log.i("m_angle_calc", "finAngle=" + (MIN_ANGLE + (float) Math.toDegrees(alpha)) + ", angle=" + Math.toDegrees(alpha) +
                ", initY=" + initY + ", w=" + w + ", height=" + AB + ", alpha=" + alpha + ", sinAlpha=" + sinAlpha);
//				return MIN_ANGLE + (float) Math.toDegrees(alpha);
        return -currentAngle;
//				return 90 * bitmapBottom.bottom/(maxHeight-bitmapBottom.bottom);

    }

    private float calculateAngleTop(int AB) {
//		double sinAlpha = AB / initY;
        double sinAlpha = bitmapTop.height() / (float) bitmapAreaTop.height();

        double alpha = Math.asin(sinAlpha);
        Log.i("m_angle_calc", "finAngle=" + (MIN_ANGLE + (float) Math.toDegrees(alpha)) + ", angle=" + Math.toDegrees(alpha) +
                ", initY=" + initY + ", height=" + AB + ", alpha=" + alpha + ", sinAlpha=" + sinAlpha + ", top=" + bitmapTop.top +
                ", bottom=" + bitmapTop.bottom + ", current=" + currentAngle + ", bTop=" + bitmapAreaTop.top + ", bBottom=" + bitmapAreaTop.bottom);
//		return MIN_ANGLE + (float) Math.toDegrees(alpha);
//		return  (float) Math.toDegrees(alpha);
        Log.i("M_kkkk", "top=" + bitmapTop.top + ", initY=" + initY + ", angle=" + (MIN_ANGLE * bitmapTop.top / initY) + ", height=" + bitmapTop.height());
        return MIN_ANGLE * (bitmapTop.top) / (initY);
    }
}
