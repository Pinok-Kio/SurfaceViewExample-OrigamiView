package com.serega.origamicontrol.origamicontrol.helpers;

import android.graphics.*;

public class Segment {
	/**
	 * Screen dimensions
	 */
	private final RectF rectAll;

	/**
	 * Top screen area to move on
	 */
	private final RectF rectTop;

	/**
	 * Bottom screen area to move on
	 */
	private final RectF rectBottom;

	/**
	 * Bitmap area to show in rectTop
	 */
	private final Rect bitmapAreaForRectTop;

	/**
	 * Bitmap area to show in rectBottom
	 */
	private final Rect bitmapAreaForRectBottom;

	/**
	 * Top center area
	 */
	private final RectF rectBitmapInnerTop;

	/**
	 * Bottom center area
	 */
	private final RectF rectBitmapInnerBottom;

	/**
	 * Inner screen area (rectBitmapInnerTop + rectBitmapInnerBottom)
	 * Hidden bitmap showing here
	 */
	private final RectF innerRect;

	/**
	 * Original Bitmap area to show in rectBitmapInnerTop.
	 * Required for take pixels from original bitmap and not create different bitmap for this region
	 */
	private final Rect bitmapTopSrc;

	/**
	 * Original Bitmap area to show in rectBitmapInnerBottom.
	 * Required for take pixels from original bitmap and not create different bitmap for this region
	 */
	private final Rect bitmapBottomSrc;

	/**
	 * Matrix for inner (center) top bitmap drawing
	 */
	private final Matrix matrixBitmapTop;

	/**
	 * Matrix for inner (center) top bitmap drawing
	 */
	private final Matrix matrixBitmapBottom;

	private final Matrix bottomGradientMatrix;
	private final Matrix topGradientMatrix;

	private final Paint paint;
	private final Paint backgroundColorPaint;
	private final Indicators indicators;

	private float DISTANCE_STEP;

	private Bitmap mainBitmap;
	private Bitmap innerBitmap;

	private boolean update;
	private float currentY;
	private float touchY;
	private float viewCenterYPoint;
	private float currentGapFraction;
	private float[] srcBottom;
	private float[] srcTop;

	private boolean moveTop;
	private boolean isReady;
	private boolean isBusy;
	private boolean switched;
	private boolean proceed = true;
	private boolean useCenterPoint = false;
	private boolean fingerWasUp = true;
	private boolean drawGradientShadow = true;

	private int bitmapIndex = 1;

	private OrigamiAdapter adapter;

	private State currentState = State.STATE_WAIT;

	private final float GAP;
	private LinearGradient shadowGradientTop;
	private LinearGradient shadowGradientBottom;

	private final Paint topGradientPaint;
	private final Paint bottomGradientPaint;

	private float fingerPath;
	private float viewOpeningThreshold = Constants.DEFAULT_VIEW_OPENING_THRESHOLD_DP;

	private boolean nextBitmapPrepared;
	private boolean prevBitmapPrepared;

	public enum State {
		STATE_WAIT,
		STATE_CLOSING_BUSY,
		STATE_OPENING_BUSY,
		STATE_OPENING,
		STATE_CLOSING
	}

	public Segment(float gap) {
		GAP = gap;
		rectAll = new RectF();
		rectTop = new RectF();
		rectBottom = new RectF();
		rectBitmapInnerTop = new RectF();
		rectBitmapInnerBottom = new RectF();
		innerRect = new RectF();
		bitmapAreaForRectBottom = new Rect();
		bitmapAreaForRectTop = new Rect();

		indicators = new Indicators();
		matrixBitmapTop = new Matrix();
		matrixBitmapBottom = new Matrix();
		paint = new Paint();
		bitmapTopSrc = new Rect();
		bitmapBottomSrc = new Rect();
		backgroundColorPaint = new Paint();
		backgroundColorPaint.setColor(Color.BLACK);

		if (drawGradientShadow) {
			bottomGradientMatrix = new Matrix();
			topGradientMatrix = new Matrix();
			topGradientPaint = new Paint();
			bottomGradientPaint = new Paint();
		} else {
			bottomGradientMatrix = null;
			topGradientMatrix = null;
			topGradientPaint = null;
			bottomGradientPaint = null;
		}
	}

	public void drawOrig(Canvas canvas) {
		if (mainBitmap != null) {
			canvas.drawBitmap(mainBitmap, 0, 0, paint);
			indicators.draw(canvas, adapter.getBitmapsCount(), bitmapIndex, currentGapFraction, moveTop);
		}
	}

	public void draw(Canvas canvas) {
		if (update && !isBusy) {
			move();
			update = false;
		}
		if (mainBitmap != null) {
			if (!switched) {
				drawBitmapTop(canvas);
				drawBitmapBottom(canvas);
			}
			drawAreaTop(canvas);
			drawAreaBottom(canvas);
			indicators.draw(canvas, adapter.getBitmapsCount(), bitmapIndex, currentGapFraction, moveTop);
			switched = false;
		}
	}

	public void prepareTouch(float y) {
		currentY = y;
		if (useCenterPoint && fingerWasUp) {
			this.touchY = viewCenterYPoint;
			prepareSameSizeRects();
		} else {
			this.touchY = y;
			prepareDifferentSizeRects(y);
		}

		float width = rectAll.width();

		rectBitmapInnerTop.set(0, y, width, y);
		rectBitmapInnerBottom.set(0, y, width, y);
		innerRect.set(0, y, width, y);

		if (drawGradientShadow) {
			initGradients();
		}

		isReady = false;
		proceed = true;
		fingerWasUp = false;
		fingerPath = 0;
		nextBitmapPrepared = false;
		prevBitmapPrepared = false;
	}

	private void prepareSameSizeRects() {
		float width = rectAll.width();
		float height = rectAll.height();
		float centerY = viewCenterYPoint;
		rectTop.set(0, 0, width, centerY);
		rectBottom.set(0, centerY, width, height);

		bitmapAreaForRectTop.set(0, 0, (int) width, (int) centerY);
		bitmapAreaForRectBottom.set(0, (int) centerY, (int) width, (int) height);
	}

	private void prepareDifferentSizeRects(float y) {
		float width = rectAll.width();
		float height = rectAll.height();

		rectTop.set(0, 0, width, y);
		rectBottom.set(0, y, width, height);

		bitmapAreaForRectTop.set(0, 0, (int) width, (int) y);
		bitmapAreaForRectBottom.set(0, (int) y, (int) width, (int) height);
	}

	public void update(float y, boolean moveTop) {
		if (fingerWasUp) {
			return;
		}
		this.moveTop = moveTop;
		currentState = moveTop ? State.STATE_OPENING : State.STATE_CLOSING;

		fingerPath += (currentY - y);
		if (!isReady && Math.abs(fingerPath) > 10) {
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
		float top = 0;
		float bottom = rectTop.bottom;

		if (bottom > top) {
			if (bottom - DISTANCE_STEP < top) {
				bottom = top;
			} else {
				bottom -= DISTANCE_STEP;
			}
			rectTop.set(left, top, right, bottom);
			float bTop = bitmapAreaForRectTop.top + DISTANCE_STEP;
			float bBottom = bitmapAreaForRectTop.bottom;
			if (bBottom < bTop) {
				bBottom = bTop;
			}
			bitmapAreaForRectTop.set((int) left, (int) bTop, (int) right, (int) bBottom);
		} else {
			if (innerRect.height() == rectAll.height()) {
				proceed = false;
			}
			update = false;
		}
	}

	private void closingRegionTop() {
		float left = rectTop.left;
		float right = rectTop.right;
		float top = 0;
		float bottom = rectTop.bottom;

		if (bottom < touchY) {
			if (bottom + DISTANCE_STEP > touchY) {
				bottom = touchY;
			} else {
				bottom += DISTANCE_STEP;
			}
			rectTop.set(left, top, right, bottom);
			float bTop = bitmapAreaForRectTop.top - DISTANCE_STEP;
			if (bTop < 0) {
				bTop = 0;
			}
			float bBottom = bitmapAreaForRectTop.bottom;
			bitmapAreaForRectTop.set((int) left, (int) bTop, (int) right, (int) bBottom);
		} else if (innerRect.height() == 0) {
			proceed = false;
		}
	}

	private void resizeInnerRect() {
		innerRect.set(0, rectTop.bottom, rectAll.width(), rectBottom.top);
		rectBitmapInnerTop.set(0, innerRect.top, innerRect.right, innerRect.centerY());
		rectBitmapInnerBottom.set(0, innerRect.centerY(), innerRect.right, innerRect.bottom);
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
			float bTop = bitmapAreaForRectBottom.top;
			float bBottom = bitmapAreaForRectBottom.bottom - DISTANCE_STEP;
			if (bBottom < bTop) {
				bBottom = bTop;
			}
			bitmapAreaForRectBottom.set((int) left, (int) bTop, (int) right, (int) bBottom);
		} else {
			if (innerRect.height() == rectAll.height()) {
				proceed = false;
			}
			update = false;
		}
	}

	private void closingRegionBottom() {
		float left = rectBottom.left;
		float right = rectBottom.right;
		float top = rectBottom.top;
		float bottom = rectAll.bottom;
		if (top > touchY) {
			if (top - DISTANCE_STEP < touchY) {
				top = touchY;
			} else {
				top -= DISTANCE_STEP;
			}
			rectBottom.set(left, top, right, bottom);
			float bTop = bitmapAreaForRectBottom.top;
			float bBottom = bitmapAreaForRectBottom.bottom + DISTANCE_STEP;
			if (bBottom > rectAll.bottom) {
				bBottom = rectAll.bottom;
			}
			bitmapAreaForRectBottom.set((int) left, (int) bTop, (int) right, (int) bBottom);
		} else if (innerRect.height() == 0) {
			proceed = false;
		}

	}

	private void drawBitmapTop(Canvas canvas) {
		if (innerBitmap != null) {
			prepareBitmapTopMatrix();
			canvas.drawRect(rectBitmapInnerTop, backgroundColorPaint);
			canvas.save();
			canvas.concat(matrixBitmapTop);
			canvas.drawBitmap(innerBitmap, bitmapTopSrc, bitmapTopSrc, paint);
			if (drawGradientShadow) {
				int alpha = calculateAlpha(rectBitmapInnerTop.height());
				topGradientPaint.setAlpha(alpha);
				canvas.drawRect(0, 0, rectAll.width(), viewCenterYPoint, topGradientPaint);
			}
			canvas.restore();
		}
	}

	private void prepareBitmapTopMatrix() {
		float gap = calculateGap(rectBitmapInnerTop.height());

		float[] dst = {
				0, rectBitmapInnerTop.top, rectBitmapInnerTop.width(), rectBitmapInnerTop.top,
				gap, rectBitmapInnerTop.bottom, rectBitmapInnerTop.width() - gap, rectBitmapInnerTop.bottom
		};

		matrixBitmapTop.setPolyToPoly(srcTop, 0, dst, 0, srcTop.length / 2);

		if (drawGradientShadow) {
			if (!useCenterPoint) {
				shadowGradientTop = new LinearGradient(0, rectBitmapInnerTop.top, 0, rectBitmapInnerTop.bottom,
						Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);
				shadowGradientTop.setLocalMatrix(matrixBitmapTop);
			}
		}
	}


	private void drawBitmapBottom(Canvas canvas) {

		if (innerBitmap != null) {
			prepareBitmapBottomMatrix();
			canvas.drawRect(rectBitmapInnerBottom, backgroundColorPaint);
			canvas.save();
			canvas.concat(matrixBitmapBottom);
			canvas.drawBitmap(innerBitmap, bitmapBottomSrc, bitmapTopSrc, paint);
			if (drawGradientShadow) {
				int alpha = calculateAlpha(rectBitmapInnerBottom.height());
				bottomGradientPaint.setAlpha(alpha);
				canvas.drawRect(0, 0, rectAll.width(), viewCenterYPoint, bottomGradientPaint);
			}
			canvas.restore();
		}
	}

	private void prepareBitmapBottomMatrix() {
		float gap = calculateGap(rectBitmapInnerBottom.height());

		float[] dst = {
				gap, rectBitmapInnerBottom.top, rectBitmapInnerBottom.width() - gap, rectBitmapInnerBottom.top,
				0, rectBitmapInnerBottom.bottom, rectBitmapInnerBottom.width(), rectBitmapInnerBottom.bottom
		};

		matrixBitmapBottom.setPolyToPoly(srcBottom, 0, dst, 0, srcBottom.length / 2);

		if (drawGradientShadow) {
			if (!useCenterPoint) {
				shadowGradientBottom = new LinearGradient(0, rectBitmapInnerBottom.top, 0, rectBitmapInnerBottom.bottom,
						Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
				bottomGradientMatrix.set(matrixBitmapBottom);
				bottomGradientMatrix.postScale(1, 0.5f);
				shadowGradientBottom.setLocalMatrix(bottomGradientMatrix);
			}
		}
	}

	private void drawAreaTop(Canvas canvas) {
		if (rectTop.height() > 0) {
			canvas.drawBitmap(mainBitmap, bitmapAreaForRectTop, rectTop, paint);
		}
	}

	private void drawAreaBottom(Canvas canvas) {
		if (rectBottom.height() > 0) {
			canvas.drawBitmap(mainBitmap, bitmapAreaForRectBottom, rectBottom, paint);
		}
	}

	public void setBitmap(Bitmap bitmap) {
		this.mainBitmap = bitmap;
	}

	private void prepareNextBitmap() {
		nextBitmapPrepared = true;
		int count = adapter.getBitmapsCount();
		int index = bitmapIndex + 1;
		if (index > count - 1) {
			index = 0;
		}
		innerBitmap = adapter.getBitmap((int) rectAll.width(), (int) rectAll.height(), index);
	}

	private void preparePrevBitmap() {
		prevBitmapPrepared = true;
		int count = adapter.getBitmapsCount();
		int index = bitmapIndex - 1;
		if (index < 0) {
			index = count - 1;
		}
		innerBitmap = mainBitmap;
		mainBitmap = adapter.getBitmap((int) rectAll.width(), (int) rectAll.height(), index);
	}

	public void processFingerUp() {
		fingerWasUp = true;
		fingerPath = Math.abs(fingerPath);
		switch (currentState) {
			case STATE_OPENING:
				if (fingerPath > viewOpeningThreshold) {
					currentState = State.STATE_OPENING_BUSY;
				} else {
					currentState = State.STATE_CLOSING_BUSY;
				}
				break;
			case STATE_CLOSING:
				if (fingerPath > viewOpeningThreshold) {
					currentState = State.STATE_CLOSING_BUSY;
				} else {
					currentState = State.STATE_OPENING_BUSY;
				}
				break;
		}

		isBusy = true;
	}

	private void changeBitmapIndex(int val) {
		int count = adapter.getBitmapsCount();
		bitmapIndex += val;
		if (bitmapIndex < 0) {
			bitmapIndex = count - 1;
		}

		if (bitmapIndex > count - 1) {
			bitmapIndex = 0;
		}
	}

	public void processBusy() {
		switch (currentState) {
			case STATE_OPENING_BUSY:
				if (innerRect.top <= 0 && innerRect.bottom >= rectAll.bottom) {
					switchBitmapsNext();
					if (nextBitmapPrepared) {
						changeBitmapIndex(1);
					}
					currentState = State.STATE_WAIT;
					isBusy = false;
					update = false;
				} else {
					processOpening();
				}
				break;

			case STATE_CLOSING_BUSY:
				if (innerRect.height() <= 0) {
					if (prevBitmapPrepared) {
						changeBitmapIndex(-1);
					}
					isBusy = false;
					update = false;
					currentState = State.STATE_WAIT;
				} else {
					processClosing();
				}
				break;

			default:
				isBusy = false;
		}
	}

	private void switchBitmapsNext() {
		rectTop.set(0, 0, rectAll.right, viewCenterYPoint);
		rectBottom.set(0, viewCenterYPoint, rectAll.right, rectAll.bottom);

		bitmapAreaForRectTop.set(0, 0, (int) rectAll.right, (int) viewCenterYPoint);
		bitmapAreaForRectBottom.set(0, (int) viewCenterYPoint, (int) rectAll.right, (int) rectAll.bottom);

		fingerPath = 0;
		matrixBitmapTop.reset();
		matrixBitmapBottom.reset();
		Bitmap tmp = mainBitmap;
		mainBitmap = innerBitmap;
		innerBitmap = tmp;

		switched = true;
	}

	private void swapRegions() {
		switched = true;

		rectTop.set(0, 0, rectAll.right, 0);
		rectBottom.set(0, rectAll.height(), rectAll.right, rectAll.height());
		innerRect.set(0, rectTop.bottom, rectAll.width(), rectBottom.top);

		float left = rectTop.left;
		float right = rectTop.right;
		int bBottom = useCenterPoint ? (int) innerRect.centerY() : (int) touchY;
		int bTop = bBottom - (int) rectTop.height();
		bitmapAreaForRectTop.set((int) left, bTop, (int) right, bBottom);

		left = rectBottom.left;
		right = rectBottom.right;
		bTop = useCenterPoint ? (int) innerRect.centerY() : (int) touchY;
		bBottom = bTop + (int) rectBottom.height();
		bitmapAreaForRectBottom.set((int) left, bTop, (int) right, bBottom);
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

	public Segment setAdapter(OrigamiAdapter adapter) {
		this.adapter = adapter;
		return this;
	}

	public OrigamiAdapter getAdapter() {
		return adapter;
	}

	public Segment setRectAll(float left, float top, float right, float bottom) {
		rectAll.set(left, top, right, bottom);
		viewCenterYPoint = rectAll.centerY();

		DISTANCE_STEP = viewCenterYPoint / Constants.STEPS_COUNT;

		bitmapTopSrc.set(0, 0, (int) rectAll.width(), (int) viewCenterYPoint);
		bitmapBottomSrc.set(0, (int) (rectAll.height() - viewCenterYPoint), (int) rectAll.width(), (int) rectAll.height());

		srcTop = new float[]{
				0, 0, bitmapTopSrc.right, 0,
				0, bitmapTopSrc.bottom, bitmapTopSrc.right, bitmapTopSrc.bottom
		};


		srcBottom = new float[]{
				0, 0, bitmapBottomSrc.width(), 0,
				0, bitmapBottomSrc.height(), bitmapBottomSrc.width(), bitmapBottomSrc.height()
		};

		return this;
	}

	private void initGradients() {
		shadowGradientTop = new LinearGradient(0, 0, 0, viewCenterYPoint, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);
		shadowGradientBottom = new LinearGradient(0, 0, 0, rectAll.bottom, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);

		topGradientPaint.setShader(shadowGradientTop);
		bottomGradientPaint.setShader(shadowGradientBottom);
		topGradientPaint.setStyle(Paint.Style.FILL);
		bottomGradientPaint.setStyle(Paint.Style.FILL);
	}

	private float calculateGapFactor(float currentHeight) {
		currentGapFraction = currentHeight / viewCenterYPoint;
		return currentGapFraction;
	}

	private float calculateGap(float currentHeight) {
		return GAP - GAP * calculateGapFactor(currentHeight);
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

	public Segment setViewOpeningThreshold(float threshold) {
		this.viewOpeningThreshold = threshold;
		return this;
	}

	public Segment setBackgroundColor(int color) {
		backgroundColorPaint.setColor(color);
		return this;
	}

	public void onFling(boolean direction) {
		if (direction) {
			isBusy = true;
			prepareNextBitmap();
			processFingerUp();
		}
	}

	public Segment setUseCenterOnly(boolean value) {
		useCenterPoint = value;
		return this;
	}

	public Segment setDrawGradientShadow(boolean value) {
		drawGradientShadow = value;
		return this;
	}

	public Segment setIndicatorColor(int color) {
		indicators.setIndicatorColor(color);
		return this;
	}

	public Segment setCurrentIndicatorColor(int color) {
		indicators.setCurrentIndicatorColor(color);
		return this;
	}

}

