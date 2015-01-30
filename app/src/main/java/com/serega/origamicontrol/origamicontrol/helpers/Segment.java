package com.serega.origamicontrol.origamicontrol.helpers;

import android.graphics.*;
import android.util.Log;

public class Segment {
    /**
     * Размер экрана
     */
    private final RectF rectAll;

    /**
     * Области сверху и снизу экрана, которые мы раздвигаем.
     */
    private final RectF rectTop;
    private final RectF rectBottom;

    /**
     * Области изображения, которое показывается в rectTop и rectBottom.
     */
    private final Rect bitmapAreaTop;
    private final Rect bitmapAreaBottom;

    /**
     * Области в центре, для верхней и нижней половины изображения
     */
    private final RectF rectBitmapTop;
    private final RectF rectBitmapBottom;

    /**
     * Центральная область изображения целиком (rectBitmapTop + rectBitmapBottom)
     */
    private final RectF innerRect;

    /**
     * Область оригинального изображения, которые мы показываем в центре.
     * Нужны, чтобы брать пиксели из оригинальной Bitmap и не создавать отдельно 2 дополнительных Bitmap
     */
    private final Rect bitmapTopSrc;
    private final Rect bitmapBottomSrc;

    /**
     * Матрица для рисования верхней половины центрального изображения
     */
    private final Matrix matrixBitmapTop;

    /**
     * Матрица для рисования нижней половины центрального изображения
     */
    private final Matrix matrixBitmapBottom;

//    private final Matrix bottomGradientMatrix;
//    private final Matrix topGradientMatrix;

    private final Paint paint;
    private final Indicators indicators;

    private float DISTANCE_STEP;

    private Bitmap bitmapBatman;
    private Bitmap innerBitmap;

    private boolean update;
    public float currentY;
    public float touchY;
    private boolean moveTop;
    private boolean isReady;
    private boolean isBusy;
    private boolean switched;
    private boolean proceed = true;

    private int bitmapIndex = 1;

    private OrigamiAdapter adapter;

    private State currentState = State.STATE_OPENING_CURRENT;

    private final float GAP;
//    private LinearGradient shadowGradientTop;
//    private LinearGradient shadowGradientBottom;

    private final Paint topGradientPaint;
    private final Paint bottomGradientPaint;

//    private final float GRADIENT_Y_SCALE = 0.5f;

    private float fingerPath;
    private float viewOpeningThreshold = 80;

    public enum State {
        STATE_CLOSING_CURRENT,
        STATE_OPENING_CURRENT,
    }

    public Segment(float gap) {
        GAP = gap;
        rectAll = new RectF();
        rectTop = new RectF();
        rectBottom = new RectF();
        rectBitmapTop = new RectF();
        rectBitmapBottom = new RectF();
        innerRect = new RectF();
        bitmapAreaBottom = new Rect();
        bitmapAreaTop = new Rect();

        indicators = new Indicators();
        matrixBitmapTop = new Matrix();
        matrixBitmapBottom = new Matrix();
        paint = new Paint();
        bitmapTopSrc = new Rect();
        bitmapBottomSrc = new Rect();
        topGradientPaint = new Paint();
        bottomGradientPaint = new Paint();
//        bottomGradientMatrix = new Matrix();
//        topGradientMatrix = new Matrix();
    }

    public void drawOrig(Canvas canvas) {
        if (bitmapBatman != null) {
            canvas.save();
            canvas.drawBitmap(bitmapBatman, 0, 0, paint);
            indicators.draw(canvas, adapter.getBitmapsCount(), bitmapIndex);
            canvas.restore();
        }
    }

    public void draw(Canvas canvas) {
        if (update && !isBusy) {
            move();
            update = false;
        }
        if (bitmapBatman != null) {
            if (!switched) {
                drawBitmapTop(canvas);
                drawBitmapBottom(canvas);
            }
            drawAreaTop(canvas);
            drawAreaBottom(canvas);

            canvas.save();
            indicators.draw(canvas, adapter.getBitmapsCount(), bitmapIndex);
            canvas.restore();
            switched = false;
        }
    }

    public void prepareTouch(float y, float touchY) {
        currentY = y;
        this.touchY = touchY;


        float width = rectAll.width();
        float height = rectAll.height();
        float centerY = rectAll.centerY();

        rectTop.set(0, 0, width, centerY);
        rectBottom.set(0, centerY, width, height);

//        rectTop.set(0, 0, width, y);
//        rectBottom.set(0, y, width, height);

        bitmapAreaTop.set(0, 0, (int) width, (int) centerY);
        bitmapAreaBottom.set(0, (int) centerY, (int) width, (int) height);

//        bitmapAreaTop.set(0, 0, (int) width, (int) y);
//        bitmapAreaBottom.set(0, (int) y, (int) width, (int) height);

        rectBitmapTop.set(0, y, width, y);
        rectBitmapBottom.set(0, y, width, y);


        innerRect.set(0, y, width, y);
        isReady = false;
        proceed = true;
        fingerPath = 0;
    }

    public void update(float y, boolean moveTop) {
        this.moveTop = moveTop;
        fingerPath += (currentY - y);
        Log.i("M_PATH", fingerPath + ", curr=" + currentY + ", y=" + y);
        if (!isReady) {
            prepare();
        }
        if (proceed) {
            update = true;
        }
        currentY = y;
    }

    private void prepare() {
        if (moveTop) {
            prepareNextBitmap();
        } else if (innerRect.height() == 0) {
            swapRegions();
            preparePrevBitmap();
        }
        isReady = true;
    }


    private void move() {
        if (moveTop) {
            moveTop();
        } else {
            moveBottom();
        }
        resizeInnerRect();
    }

    private void moveTop() {
        openingRegionTop();
        openingRegionBottom();
    }

    private void moveBottom() {
        if (innerRect.height() > 0) {
            closingRegionTop();
            closingRegionBottom();
        }
    }

    private void openingRegionTop() {
        float left = rectTop.left;
        float right = rectTop.right;
        float top = rectTop.top;
        float bottom = rectTop.bottom;

        if (bottom > top) {
            if (bottom - DISTANCE_STEP < top) {
                bottom = top;
            } else {
                bottom -= DISTANCE_STEP;
            }
            rectTop.set(left, top, right, bottom);
            float bTop = bitmapAreaTop.top + DISTANCE_STEP;
            float bBottom = bitmapAreaTop.bottom;
            if (bBottom < bTop) {
                bBottom = bTop;
            }
            bitmapAreaTop.set((int) left, (int) bTop, (int) right, (int) bBottom);
        } else {
            update = false;
        }
    }

    private void closingRegionTop() {
        float left = rectTop.left;
        float right = rectTop.right;
        float top = rectTop.top;
        float bottom = rectTop.bottom;

        if (bottom < touchY) {
            if (bottom + DISTANCE_STEP >= touchY) {
                bottom = touchY;
            } else {
                bottom += DISTANCE_STEP;
            }
            rectTop.set(left, top, right, bottom);
            float bTop = bitmapAreaTop.top - DISTANCE_STEP;
            float bBottom = bitmapAreaTop.bottom;
            bitmapAreaTop.set((int) left, (int) bTop, (int) right, (int) bBottom);
        } else {
            update = false;
            proceed = false;
        }
    }

    private void resizeInnerRect() {
        innerRect.set(0, rectTop.bottom, rectAll.width(), rectBottom.top);
        rectBitmapTop.set(0, innerRect.top, innerRect.right, innerRect.centerY());
        rectBitmapBottom.set(0, innerRect.centerY(), innerRect.right, innerRect.bottom);
    }

    private void openingRegionBottom() {
        float left = rectBottom.left;
        float right = rectBottom.right;
        float top = rectBottom.top;
        float bottom = rectBottom.bottom;

        if (top < bottom) {
            if (top + DISTANCE_STEP > bottom) {
                top = bottom;
            } else {
                top += DISTANCE_STEP;
            }
            rectBottom.set(left, top, right, bottom);
            float bTop = bitmapAreaBottom.top;
            float bBottom = bitmapAreaBottom.bottom - DISTANCE_STEP;
            if (bBottom < bTop) {
                bBottom = bTop;
            }
            bitmapAreaBottom.set((int) left, (int) bTop, (int) right, (int) bBottom);
        } else {
            update = false;
        }
    }

    private void closingRegionBottom() {
        float left = rectBottom.left;
        float right = rectBottom.right;
        float top = rectBottom.top;
        float bottom = rectBottom.bottom;
        if (top >= touchY) {
            if (top - DISTANCE_STEP < touchY) {
                top = touchY;
            } else {
                top -= DISTANCE_STEP;
            }
            rectBottom.set(left, top, right, bottom);
            float bTop = bitmapAreaBottom.top;
            float bBottom = bitmapAreaBottom.bottom + DISTANCE_STEP;
            bitmapAreaBottom.set((int) left, (int) bTop, (int) right, (int) bBottom);
        } else {
            update = false;
            proceed = false;
        }
    }

    static Paint p = new Paint();

    static {
        p.setColor(Color.BLACK);
    }

    private void drawBitmapTop(Canvas canvas) {
        if (innerBitmap != null) {
            prepareBitmapTopMatrix();
            canvas.drawRect(rectBitmapTop, p);
            canvas.save();
            canvas.concat(matrixBitmapTop);
            canvas.drawBitmap(innerBitmap, bitmapTopSrc, bitmapTopSrc, paint);
//            int alpha = calculateAlpha(rectBitmapTop.height());
//            topGradientPaint.setAlpha(alpha);
//            canvas.drawRect(0, 0, rectAll.width(), rectAll.centerY(), topGradientPaint);
            canvas.restore();
        }
    }

    private void prepareBitmapTopMatrix() {
        matrixBitmapTop.reset();

        float gap = calculateGap(rectBitmapTop.height());

        float[] src = {
                0, 0, bitmapTopSrc.right, 0,
                0, bitmapTopSrc.bottom, bitmapTopSrc.right, bitmapTopSrc.bottom
        };


        float[] dst = {
                0, rectBitmapTop.top, rectBitmapTop.width(), rectBitmapTop.top,
                gap, rectBitmapTop.bottom, rectBitmapTop.width() - gap, rectBitmapTop.bottom
        };

        boolean b = matrixBitmapTop.setPolyToPoly(src, 0, dst, 0, src.length / 2);
//        topGradientMatrix.set(matrixBitmapTop);
//        topGradientMatrix.setScale(1, -1.5f);
//        shadowGradientTop.setLocalMatrix(matrixBitmapTop);
    }


    private void drawBitmapBottom(Canvas canvas) {
        if (innerBitmap != null) {
            prepareBitmapBottomMatrix();
            if (rectBitmapBottom.bottom < rectAll.bottom) {
                canvas.drawRect(rectBitmapBottom, p);
            }
            canvas.save();
            canvas.concat(matrixBitmapBottom);
            canvas.drawBitmap(innerBitmap, bitmapBottomSrc, bitmapTopSrc, paint);
//            int alpha = calculateAlpha(rectBitmapBottom.height());
//            bottomGradientPaint.setAlpha(alpha);
//            canvas.drawRect(0, 0, rectAll.width(), rectAll.centerY(), bottomGradientPaint);
            canvas.restore();
        }
    }

    private void prepareBitmapBottomMatrix() {
        matrixBitmapBottom.reset();

        float gap = calculateGap(rectBitmapBottom.height());
        float[] src = {
                0, 0, bitmapBottomSrc.width(), 0,
                0, bitmapBottomSrc.height(), bitmapBottomSrc.width(), bitmapBottomSrc.height()
        };

        float[] dst = {
                gap, rectBitmapBottom.top, rectBitmapBottom.width() - gap, rectBitmapBottom.top,
                0, rectBitmapBottom.bottom, rectBitmapBottom.width(), rectBitmapBottom.bottom
        };

        matrixBitmapBottom.setPolyToPoly(src, 0, dst, 0, src.length / 2);
//        bottomGradientMatrix.set(matrixBitmapBottom);
//        bottomGradientMatrix.setScale(1, GRADIENT_Y_SCALE);
//        shadowGradientBottom.setLocalMatrix(matrixBitmapBottom);
    }

    private void drawAreaTop(Canvas canvas) {
        if (rectTop.height() > 0) {
            canvas.save();
            canvas.drawBitmap(bitmapBatman, bitmapAreaTop, rectTop, paint);
            canvas.restore();
        }
    }

    private void drawAreaBottom(Canvas canvas) {
        if (rectBottom.height() > 0) {
            canvas.save();
            canvas.drawBitmap(bitmapBatman, bitmapAreaBottom, rectBottom, paint);
            canvas.restore();
        }
    }


    public void setBitmap(Bitmap bitmap) {
        this.bitmapBatman = bitmap;
    }


    public synchronized void prepareNextBitmap() {
        int count = adapter.getBitmapsCount();
        if (bitmapIndex + 1 > count - 1) {
            bitmapIndex = 0;
        } else {
            bitmapIndex++;
        }
        innerBitmap = adapter.getBitmap((int) rectAll.width(), (int) rectAll.height(), bitmapIndex);
        bitmapTopSrc.set(0, 0, innerBitmap.getWidth(), innerBitmap.getHeight() / 2);
        bitmapTopSrc.set(0, 0, innerBitmap.getWidth(), innerBitmap.getHeight() / 2);
        bitmapBottomSrc.set(0, innerBitmap.getHeight() - innerBitmap.getHeight() / 2, innerBitmap.getWidth(), innerBitmap.getHeight());
    }

    public void preparePrevBitmap() {
        int count = adapter.getBitmapsCount();
        if (bitmapIndex - 1 < 0) {
            bitmapIndex = count - 1;
        } else {
            bitmapIndex--;
        }
        innerBitmap = bitmapBatman;
        bitmapBatman = adapter.getBitmap((int) rectAll.width(), (int) rectAll.height(), bitmapIndex);
        bitmapTopSrc.set(0, 0, innerBitmap.getWidth(), innerBitmap.getHeight() / 2);
        bitmapBottomSrc.set(0, innerBitmap.getHeight() - innerBitmap.getHeight() / 2, innerBitmap.getWidth(), innerBitmap.getHeight());
    }


    public void processFingerUp() {
        if (innerRect.height() == 0) {
            moveBitmapIndex();
            return;
        }
        isBusy = true;
//        if (innerRect.top < rectAll.height() / 3) {
//            currentState = State.STATE_OPENING_CURRENT;
//        } else {
//            currentState = State.STATE_CLOSING_CURRENT;
//        }

        if (fingerPath > 0) {
            if (fingerPath > viewOpeningThreshold) {
                currentState = State.STATE_OPENING_CURRENT;
            } else {
                currentState = State.STATE_CLOSING_CURRENT;
            }
        } else {
            fingerPath = -fingerPath;
            if (fingerPath > viewOpeningThreshold) {
                currentState = State.STATE_CLOSING_CURRENT;
            } else {
                currentState = State.STATE_OPENING_CURRENT;
            }
        }
    }

    private void moveBitmapIndex(){
        if(moveTop){
            bitmapIndex--;
        }else{
            bitmapIndex++;
        }
    }

    public void processBusy() {
        switch (currentState) {
            case STATE_OPENING_CURRENT:
                processOpening();
                if (innerRect.top <= 0) {
                    switchBitmapsNext();
                    isBusy = false;
                }
                break;

            case STATE_CLOSING_CURRENT:
                processClosing();
                if (innerRect.height() <= 0) {
                    isBusy = false;
                }
                break;
        }
    }

    private void switchBitmapsNext() {
        Bitmap tmp = bitmapBatman;
        bitmapBatman = innerBitmap;
        innerBitmap = tmp;

        switched = true;

        rectTop.set(0, 0, rectAll.right, rectAll.centerY());
        rectBottom.set(0, rectAll.centerY(), rectAll.right, rectAll.bottom);

        bitmapAreaTop.set(0, 0, (int) rectAll.right, (int) rectAll.centerY());
        bitmapAreaBottom.set(0, (int) rectAll.centerY(), (int) rectAll.right, (int) rectAll.bottom);
    }

    private void swapRegions() {
        switched = true;

        rectTop.set(0, 0, rectAll.right, 0);
        rectBottom.set(0, rectAll.height(), rectAll.right, rectAll.height());
        innerRect.set(0, rectTop.bottom, rectAll.width(), rectBottom.top);

        float left = rectTop.left;
        float right = rectTop.right;
        int bBottom = (int) rectAll.centerY();
        int bTop = bBottom - (int) rectTop.height();
        bitmapAreaTop.set((int) left, bTop, (int) right, bBottom);

        left = rectBottom.left;
        right = rectBottom.right;
        bTop = (int) rectAll.centerY();
        bBottom = bTop + (int) rectBottom.height();
        bitmapAreaBottom.set((int) left, bTop, (int) right, bBottom);
    }

    private void processOpening() {
        openingRegionTop();
        openingRegionBottom();
        resizeInnerRect();
    }

    private void processClosing() {
        closingRegionTop();
        closingRegionBottom();
        resizeInnerRect();
    }

    public void setAdapter(OrigamiAdapter adapter) {
        this.adapter = adapter;
    }

    public OrigamiAdapter getAdapter() {
        return adapter;
    }


    public void setRectAll(float left, float top, float right, float bottom) {
        rectAll.set(left, top, right, bottom);
        DISTANCE_STEP = rectAll.centerY() / Config.STEPS_COUNT;

//        initGradients();
    }

//    private void initGradients(){
//        shadowGradientTop = new LinearGradient(0, rectAll.centerY(), 0, 0, Color.RED, Color.WHITE, Shader.TileMode.CLAMP);
//        shadowGradientBottom = new LinearGradient(0, rectAll.centerY(), 0, rectAll.bottom, Color.RED, Color.WHITE, Shader.TileMode.CLAMP);
//
//        topGradientPaint.setShader(shadowGradientTop);
//        bottomGradientPaint.setShader(shadowGradientBottom);
//        topGradientPaint.setStyle(Paint.Style.FILL);
//        bottomGradientPaint.setStyle(Paint.Style.FILL);
//    }

    private float calculateGapFactor(float currentHeight) {
        return currentHeight / rectAll.centerY();
    }

    private float calculateGap(float currentHeight) {
//		if (rectAll.centerY() - currentHeight > 0) {
        float gap = GAP - GAP * calculateGapFactor(currentHeight);
//			if (gap > 1) {
        return gap;
//			}
//		}
//		return 0;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isBusy() {
        return isBusy;
    }

    private int calculateAlpha(float height) {
        return 255 - (int) (255 * calculateGapFactor(height));
    }

    public void setBitmapIndex(int index) {
        bitmapIndex = index;
    }

    public void setViewOpeningThreshold(float threshold) {
        this.viewOpeningThreshold = threshold;
    }

    public void onFling(boolean direction) {
        if (direction) {
            isBusy = true;
            prepareNextBitmap();
            fingerPath = 10_000;
            float left = innerRect.left;
            float top = rectAll.centerY() - 1;
            float right = innerRect.right;
            float bottom = rectAll.centerY() + 1;
            innerRect.set(left, top, right, bottom);
            processFingerUp();
        }
    }
}

