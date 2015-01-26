package com.serega.origamicontrol;

import android.graphics.*;
import android.util.Log;

public class Segment {
    private Bitmap bitmap;
    private int color;
    private final Paint paint;
    private final Camera camera;
    private float currentAngle;
    private int currentDistanceStep = 10;
    private final Rect region;
    public boolean update;

    private float currentY;

    public float initY;

    boolean moveTop;

    boolean process = false;

    public SType type = SType.DEFAULT;

    public enum SType {
        DEFAULT,
        TOP,
        BOTTOM,
        TOP_ROTATE,
        BOTTOM_ROTATE
    }

    public Segment() {
        paint = new Paint();
        camera = new Camera();
        region = new Rect();
    }

    public void setRegion(Rect r) {
        region.set(r);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }


    public void update(float y) {
        Log.i("M_MOVE", "y=" + y + ", " + currentY + ", moveTop=" +moveTop);
        moveTop = y < currentY;
        update = true;
        currentY = y;
    }

    public void draw(Canvas canvas) {
        if (update) {
            move();
            update = false;
        }
        update = false;
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, region, region, paint);
        }
    }

    private void move() {
        int rtop = region.top;
        int left = region.left;
        int right = region.right;
        int bottom = region.bottom;
        switch (type) {
            case DEFAULT:
                return;

            case TOP:
                if (moveTop) {
                    Log.i("M_DRAW", "TOP, bottom=" + bottom + ", initY=" + initY + ", moveTop=" + moveTop);
                    if (bottom - currentDistanceStep > 0) {
                        region.set(left, rtop, right, bottom - currentDistanceStep);
                    } else {
                        update = false;
                    }
                } else {
                    Log.i("M_DRAW", "TOP, bottom=" + bottom + ", initY=" + initY + ", moveTop=" + moveTop);
                    if (bottom + currentDistanceStep < initY) {
                        region.set(left, rtop, right, bottom + currentDistanceStep);
                    } else {
                        update = false;
                    }
                }
                break;

            case BOTTOM:
                if(moveTop){
                    Log.i("M_DRAW", "BOTTOM, rtop=" + rtop + ", bottom=" + bottom + ", initY=" + initY + ", moveTop=" + moveTop);
                    if (rtop + currentDistanceStep < bottom) {
                        region.set(left, rtop + currentDistanceStep, right, bottom);
                    } else {
                        update = false;
                    }

                }else{
                    Log.i("M_DRAW", "BOTTOM, rtop=" + rtop + ", initY=" + initY + ", moveTop=" + moveTop);
                    if (rtop - currentDistanceStep > initY) {
                        region.set(left, rtop - currentDistanceStep, right, bottom);
                    } else {
                        update = false;
                    }
                }
                break;
        }

    }
}
