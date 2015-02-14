package com.serega.origamicontrol.origamicontrol.helpers;

import android.graphics.*;

import java.util.ArrayList;
import java.util.List;

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
	 * Inner screen area (rectBitmapInnerTop + rectBitmapInnerBottom)
	 * Hidden bitmap showing here
	 */
	private final RectF innerRect;

	/**
	 * Matrix for inner (center) top bitmap drawing
	 */
	private final Matrix matrixBitmapTop;

	/**
	 * Matrix for inner (center) top bitmap drawing
	 */
	private final Matrix matrixBitmapBottom;

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
	private float gapCalculatingHeight;
	private float currentGapFraction;

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
	private float fingerPath;
	private float viewOpeningThreshold = Constants.DEFAULT_VIEW_OPENING_THRESHOLD_DP;

	private boolean nextBitmapPrepared;
	private boolean prevBitmapPrepared;

	private static final int SEGMENTS_COUNT = 6;
	/**
	 * Inner Rectangles
	 */
	private final List<RectF> innerBitmapsRectList = new ArrayList<>(SEGMENTS_COUNT);
	/**
	 * Bitmap Areas for inner rectangles
	 * Required for taking pixels from original bitmap and not create different bitmap for current region
	 */
	private final List<Rect> innerBitmapSrc = new ArrayList<>(SEGMENTS_COUNT);

	/**
	 * Paint objects to draw shadow
	 */
	private final List<Paint> gradientPaints = new ArrayList<>(SEGMENTS_COUNT);

	private float[][] srcTopBottom;

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
		innerRect = new RectF();
		bitmapAreaForRectBottom = new Rect();
		bitmapAreaForRectTop = new Rect();

		indicators = new Indicators();
		matrixBitmapTop = new Matrix();
		matrixBitmapBottom = new Matrix();
		paint = new Paint();
		backgroundColorPaint = new Paint();
		backgroundColorPaint.setColor(Color.BLACK);

		for (int i = 0; i < SEGMENTS_COUNT; i++) {
			innerBitmapsRectList.add(new RectF());
			innerBitmapSrc.add(new Rect());
		}

		srcTopBottom = new float[SEGMENTS_COUNT][];
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

		for (RectF r : innerBitmapsRectList) {
			r.set(0, y, width, y);
		}

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
		float fStep = innerRect.height() / SEGMENTS_COUNT;
		for (int i = 0; i < SEGMENTS_COUNT; i++) {
			RectF r = innerBitmapsRectList.get(i);
			if (i == 0) {
				r.set(0, innerRect.top, innerRect.right, innerRect.top + fStep);
			} else {
				RectF prevR = innerBitmapsRectList.get(i - 1);
				r.set(0, prevR.bottom, innerRect.right, prevR.bottom + fStep);
			}
		}
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
			for (int i = 0; i < SEGMENTS_COUNT; i += 2) {
				RectF rect = innerBitmapsRectList.get(i);
				Paint gradientPain = gradientPaints.get(i);
				prepareBitmapTopMatrix(rect, srcTopBottom[i]);
				canvas.drawRect(rect, backgroundColorPaint);
				canvas.save();
				canvas.concat(matrixBitmapTop);
				Rect r = innerBitmapSrc.get(i);
				canvas.drawBitmap(innerBitmap, r, r, paint);
				if (drawGradientShadow) {
					int alpha = calculateAlpha(rect.height());
					gradientPain.setAlpha(alpha);
					canvas.drawRect(0, r.top, rectAll.width(), r.bottom, gradientPain);
				}
				canvas.restore();
			}
		}
	}

	private void prepareBitmapTopMatrix(RectF rect, float[] src) {
		float gap = calculateGap(rect.height());

		float[] dst = {
				0, rect.top, rect.width(), rect.top,
				gap, rect.bottom, rect.width() - gap, rect.bottom
		};

		matrixBitmapTop.setPolyToPoly(src, 0, dst, 0, src.length / 2);
	}


	private void drawBitmapBottom(Canvas canvas) {
		if (innerBitmap != null) {
			for (int i = 1; i < SEGMENTS_COUNT; i += 2) {
				RectF rect = innerBitmapsRectList.get(i);
				Paint gradientPaint = gradientPaints.get(i);
				prepareBitmapBottomMatrix(rect, srcTopBottom[i]);
				canvas.drawRect(rect, backgroundColorPaint);
				canvas.save();
				canvas.concat(matrixBitmapBottom);
				Rect r = innerBitmapSrc.get(i);
				canvas.drawBitmap(innerBitmap, r, r, paint);
				if (drawGradientShadow) {
					int alpha = calculateAlpha(rect.height());
					gradientPaint.setAlpha(alpha);
					canvas.drawRect(0, r.top, rectAll.width(), r.bottom, gradientPaint);
				}
				canvas.restore();
			}
		}
	}

	private void prepareBitmapBottomMatrix(RectF rect, float[] src) {
		float gap = calculateGap(rect.height());
		matrixBitmapBottom.reset();
		float[] dst = {
				gap, rect.top, rect.width() - gap, rect.top,
				0, rect.bottom, rect.width(), rect.bottom
		};

		matrixBitmapBottom.setPolyToPoly(src, 0, dst, 0, src.length / 2);
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
		gapCalculatingHeight = rectAll.height() / SEGMENTS_COUNT;


		DISTANCE_STEP = viewCenterYPoint / Constants.STEPS_COUNT;

		int step = (int) (rectAll.height() / SEGMENTS_COUNT);

		for (int i = 0; i < SEGMENTS_COUNT; i++) {
			if (i == 0) {
				innerBitmapSrc.get(i).set(0, 0, (int) rectAll.width(), step);
			} else {
				Rect prev = innerBitmapSrc.get(i - 1);
				innerBitmapSrc.get(i).set(0, prev.bottom, prev.width(), prev.bottom + step);
			}

			Rect current = innerBitmapSrc.get(i);
			srcTopBottom[i] = new float[]{
					0, current.top, current.right, current.top,
					0, current.bottom, current.right, current.bottom
			};
		}
		return this;
	}

	private void initGradients() {
		for (int i = 0; i < SEGMENTS_COUNT; i++) {
			int initValue = (int) (i * gapCalculatingHeight);
			if (i % 2 == 0) {
				LinearGradient gradientTop = new LinearGradient(0, initValue + gapCalculatingHeight, 0, initValue,
						Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
				Paint topPaint = new Paint();
				topPaint.setShader(gradientTop);
				topPaint.setStyle(Paint.Style.FILL);
				gradientPaints.add(topPaint);
			} else {
				LinearGradient gradientBottom = new LinearGradient(0, initValue, 0,
						initValue + 1.5F * gapCalculatingHeight, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
				Paint bottomPaint = new Paint();
				bottomPaint.setShader(gradientBottom);
				bottomPaint.setStyle(Paint.Style.FILL);
				gradientPaints.add(bottomPaint);
			}
		}
	}

	private float calculateGapFactor(float currentHeight) {
		currentGapFraction = currentHeight / gapCalculatingHeight;
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

