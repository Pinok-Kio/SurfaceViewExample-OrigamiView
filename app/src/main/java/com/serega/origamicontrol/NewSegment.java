package com.serega.origamicontrol;

import android.graphics.*;
import android.util.Log;

public class NewSegment {
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

	private final Paint paint;
	private final Indicators indicators;

    private float DISTANCE_STEP;

	private Bitmap bitmapBatman;
	private Bitmap innerBitmap;

	boolean update;
	public float currentY;
	public float touchY;
	boolean moveTop;
	boolean isPrepared;
	boolean closing;

    private int bitmapIndex = 2;

	private OrigamiAdapter adapter;

	private State currentState = State.STATE_WAIT;

    private final float GAP;

	public enum State {
		STATE_WAIT,
		STATE_FINISH,
		STATE_CLOSING_CURRENT,
		STATE_OPENING_CURRENT,
	}

	public NewSegment(float gap) {
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
			if(!switched) {
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
		currentState = State.STATE_WAIT;

		float width = rectAll.width();
		float height = rectAll.height();
		float centerY = rectAll.centerY();

		rectTop.set(0, 0, width, centerY);
		rectBottom.set(0, centerY, width, height);

		bitmapAreaTop.set(0, 0, (int) width, (int) centerY);
		bitmapAreaBottom.set(0, (int) centerY, (int) width, (int) height);
//		bitmapAreaTop.set(0, 0, (int) width, (int) y);
//		bitmapAreaBottom.set(0, (int) y, (int) width, (int) height);
		rectBitmapTop.set(0, y, width, y);
		rectBitmapBottom.set(0, y, width, y);


		innerRect.set(0, y, width, y);
		isPrepared = false;
        proceed = true;
	}

    boolean proceed = true;
	public void update(float y) {
		moveTop = y < currentY;

		if (!isPrepared) {
			if (moveTop) {
				prepareNextBitmap();
			} else {
				preparePrevBitmap();
			}
			isPrepared = true;
		}
        if(proceed) {
            update = true;
            currentY = y;
        }
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

			closingRegionTop();
			closingRegionBottom();
		} else {
			closing = true;
			switchRegions();
		}
	}

	private void openingRegionTop() {
		float left = rectTop.left;
		float right = rectTop.right;
		float top = rectTop.top;
		float bottom = rectTop.bottom;

		if (bottom > 0) {
			if (bottom - DISTANCE_STEP < 0) {
				bottom = 0;
			} else {
				bottom -= DISTANCE_STEP;
			}
			rectTop.set(left, top, right, bottom);
			float bTop = bitmapAreaTop.top + DISTANCE_STEP;
			float bBottom = bitmapAreaTop.bottom;
			if (bBottom < bTop) {
				bBottom = bTop;
			}
			bitmapAreaTop.set((int) left, (int)bTop, (int) right, (int)bBottom);
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
			if (bottom + DISTANCE_STEP > touchY) {
				bottom = touchY;
			} else {
				bottom += DISTANCE_STEP;
			}
			rectTop.set(left, top, right, bottom);
			float bTop = bitmapAreaTop.top - DISTANCE_STEP;
			float bBottom = bitmapAreaTop.bottom;
			bitmapAreaTop.set((int) left, (int)bTop, (int) right,(int) bBottom);
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
			bitmapAreaBottom.set((int) left, (int)bTop, (int) right, (int)bBottom);
		} else {
			update = false;
		}
	}

	private void closingRegionBottom() {
		float left = rectBottom.left;
		float right = rectBottom.right;
		float top = rectBottom.top;
		float bottom = rectBottom.bottom;
		if (top > touchY) {
			if (top - DISTANCE_STEP < touchY) {
				top = touchY;
			} else {
				top -= DISTANCE_STEP;
			}
			rectBottom.set(left, top, right, bottom);
			float bTop = bitmapAreaBottom.top;
			float bBottom = bitmapAreaBottom.bottom + DISTANCE_STEP;
			bitmapAreaBottom.set((int) left, (int)bTop, (int) right, (int)bBottom);
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
			prepareBitmapTop();
			if (rectBitmapTop.top > 0) {
				canvas.drawColor(Color.BLACK);
			}else{
				matrixBitmapTop.reset();
			}
			canvas.save();
			canvas.concat(matrixBitmapTop);
			canvas.drawBitmap(innerBitmap, bitmapTopSrc, bitmapTopSrc, paint);
			canvas.restore();
		}
	}

	private void prepareBitmapTop() {
		matrixBitmapTop.reset();

		float gap = calculateGap(rectBitmapTop.height());
		float[] src = {
				0, 0, bitmapTopSrc.width(), 0,
				0, bitmapTopSrc.height(), bitmapTopSrc.width(), bitmapTopSrc.height()
		};

		float[] dst = {
				0, rectBitmapTop.top, rectBitmapTop.width(), rectBitmapTop.top,
				gap, rectBitmapTop.bottom, rectBitmapTop.width() - gap, rectBitmapTop.bottom
		};

		matrixBitmapTop.setPolyToPoly(src, 0, dst, 0, src.length / 2);

	}



	private void drawBitmapBottom(Canvas canvas) {
		if (innerBitmap != null) {
			prepareBitmapBottom();
			canvas.save();
			canvas.concat(matrixBitmapBottom);
			canvas.drawBitmap(innerBitmap, bitmapBottomSrc, bitmapTopSrc, paint);
			canvas.restore();
		}
	}

	private void prepareBitmapBottom() {
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


	public void prepareNextBitmap() {
        int count = adapter.getBitmapsCount();
        bitmapIndex++;
        if(bitmapIndex >= count){
            bitmapIndex = 0;
        }
		innerBitmap = adapter.getBitmap((int) rectAll.width(), (int) rectAll.height(), bitmapIndex);
		bitmapTopSrc.set(0, 0, innerBitmap.getWidth(), innerBitmap.getHeight() / 2);
		bitmapTopSrc.set(0, 0, innerBitmap.getWidth(), innerBitmap.getHeight() / 2);
		bitmapBottomSrc.set(0, innerBitmap.getHeight() - innerBitmap.getHeight() / 2, innerBitmap.getWidth(), innerBitmap.getHeight());
	}


	public void preparePrevBitmap() {
		innerBitmap = bitmapBatman;
        int count = adapter.getBitmapsCount();
        if(bitmapIndex <0){
            bitmapIndex = count - 1;
        }
        bitmapIndex--;
        bitmapBatman = adapter.getBitmap((int) rectAll.width(), (int) rectAll.height(), bitmapIndex);
		bitmapTopSrc.set(0, 0, innerBitmap.getWidth(), innerBitmap.getHeight() / 2);
		bitmapBottomSrc.set(0, innerBitmap.getHeight() - innerBitmap.getHeight() / 2, innerBitmap.getWidth(), innerBitmap.getHeight());
	}

	public void setBitmapAreaTop(int left, int top, int right, int bottom) {
		bitmapAreaTop.set(left, top, right, bottom);
	}

	public void setBitmapAreaBottom(int left, int top, int right, int bottom) {
		bitmapAreaBottom.set(left, top, right, bottom);
	}


	boolean isBusy;

	public void processFingerUp() {
		if (innerRect.height() == 0) {
			return;
		}
		isBusy = true;
		if (innerRect.top < rectAll.height() / 3) {
			currentState = State.STATE_OPENING_CURRENT;
		} else {
			currentState = State.STATE_CLOSING_CURRENT;
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
                    bitmapIndex--;
					isBusy = false;
				}
				break;
		}
	}

    private boolean switched;
	private void switchBitmapsNext() {
		Bitmap tmp = bitmapBatman;
		bitmapBatman = innerBitmap;
		innerBitmap = tmp;
        switched = true;
//		bitmapTopSrc.set(0, 0, (int) rectAll.right, (int) rectAll.centerY());
//		bitmapBottomSrc.set(0, (int) rectAll.centerY(), (int) rectAll.right, (int) rectAll.bottom);

        rectTop.set(0, 0, rectAll.right, rectAll.centerY());
        rectBottom.set(0, rectAll.centerY(), rectAll.right, rectAll.bottom);

        bitmapAreaTop.set(0, 0, (int)rectAll.right, (int)rectAll.centerY());
        bitmapAreaBottom.set(0, (int)rectAll.centerY(), (int)rectAll.right, (int)rectAll.bottom);
	}

	private void switchRegions() {
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
	}

    private float calculateGap(float currentHeight) {
        if(rectAll.centerY() - currentHeight > 0) {
            float gap = GAP - GAP * currentHeight / rectAll.centerY();
            if(gap > 1){
                return gap;
            }
        }
        return 0;
    }

	public void setRectTop(float left, float top, float right, float bottom) {
		rectTop.set(left, top, right, bottom);
	}

	public void setRectBottom(float left, float top, float right, float bottom) {
		rectBottom.set(left, top, right, bottom);
	}

	public void setRectBitmapTop(float left, float top, float right, float bottom) {
		rectBitmapTop.set(left, top, right, bottom);
	}

	public void setRectBitmapBottom(float left, float top, float right, float bottom) {
		rectBitmapBottom.set(left, top, right, bottom);
	}

	public void setInnerRect(float left, float top, float right, float bottom) {
		innerRect.set(left, top, right, bottom);
	}
}

