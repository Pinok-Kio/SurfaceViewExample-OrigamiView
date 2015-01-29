package com.serega.origamicontrol;

import android.graphics.Rect;
import android.graphics.RectF;

public class RectCalculator {
	private final RectF rectAll;
	private final RectF rectTop;
	private final RectF rectBottom;
	private final RectF rectBitmapTop;
	private final RectF rectBitmapBottom;
	private final RectF innerRect;
	private final Rect bitmapAreaTop;
	private final Rect bitmapAreaBottom;

	private final int DISTANCE_STEP = Config.DEFAULT_STEP;
	private int touchY;

	public RectCalculator() {
		rectAll = new RectF();
		rectTop = new RectF();
		rectBottom = new RectF();
		rectBitmapTop = new RectF();
		rectBitmapBottom = new RectF();
		innerRect = new RectF();
		bitmapAreaBottom = new Rect();
		bitmapAreaTop = new Rect();
	}

	public void setTouchY(int touchY) {
		this.touchY = touchY;
	}

	public RectF getRectAll() {
		return rectAll;
	}

	public void setRectAll(RectF rectAll) {
		this.rectAll.set(rectAll);
	}

	public RectF getRectTop() {
		return rectTop;
	}

	public void setRectTop(RectF rectTop) {
		this.rectTop.set(rectTop);
	}

	public RectF getRectBottom() {
		return rectBottom;
	}

	public void setRectBottom(RectF rectBottom) {
		this.rectBottom.set(rectBottom);
	}

	public RectF getRectBitmapTop() {
		return rectBitmapTop;
	}

	public void setRectBitmapTop(RectF rectBitmapTop) {
		this.rectBitmapTop.set(rectBitmapTop);
	}

	public RectF getRectBitmapBottom() {
		return rectBitmapBottom;
	}

	public void setRectBitmapBottom(RectF rectBitmapBottom) {
		this.rectBitmapBottom.set(rectBitmapBottom);
	}

	public RectF getInnerRect() {
		return innerRect;
	}

	public void setInnerRect(RectF innerRect) {
		this.innerRect.set(innerRect);
	}

	public Rect getBitmapAreaTop() {
		return bitmapAreaTop;
	}

	public void setBitmapAreaTop(Rect bitmapAreaTop) {
		this.bitmapAreaTop.set(bitmapAreaTop);
	}

	public Rect getBitmapAreaBottom() {
		return bitmapAreaBottom;
	}

	public void setBitmapAreaBottom(Rect bitmapAreaBottom) {
		this.bitmapAreaBottom.set(bitmapAreaBottom);
	}

	public boolean openingRegionTop() {
		float left = rectTop.left;
		float right = rectTop.right;
		float top = rectTop.top;
		float bottom = rectTop.bottom;
		if (bottom - DISTANCE_STEP >= 0) {
			rectTop.set(left, top, right, bottom - DISTANCE_STEP);
			int bTop = bitmapAreaTop.top + DISTANCE_STEP;
			int bBottom = bitmapAreaTop.bottom;
			bitmapAreaTop.set((int) left, bTop, (int) right, bBottom);
			return true;
		} else {
			return false;
		}
	}

	public boolean closingRegionTop() {
		float left = rectTop.left;
		float right = rectTop.right;
		float top = rectTop.top;
		float bottom = rectTop.bottom;
		if (bottom <= touchY) {
			rectTop.set(left, top, right, bottom + DISTANCE_STEP);
			int bTop = bitmapAreaTop.top - DISTANCE_STEP;
			int bBottom = bitmapAreaTop.bottom;
			bitmapAreaTop.set((int) left, bTop, (int) right, bBottom);
			return true;
		} else {
			return false;
		}
	}

	public void resizeInnerRect() {
		innerRect.set(0, rectTop.bottom, rectAll.width(), rectBottom.top);
		resizeBitmapTopRect();
		resizeBitmapBottomRect();
	}

	private void resizeBitmapTopRect() {
		rectBitmapTop.set(0, innerRect.top, innerRect.right, innerRect.centerY());
	}

	private void resizeBitmapBottomRect() {
		rectBitmapBottom.set(0, innerRect.centerY(), innerRect.right, innerRect.bottom);
	}

	public boolean openingRegionBottom() {
		float left = rectBottom.left;
		float right = rectBottom.right;
		float top = rectBottom.top;
		float bottom = rectBottom.bottom;
		if (top + DISTANCE_STEP <= bottom) {
			rectBottom.set(left, top + DISTANCE_STEP, right, bottom);
			int bTop = bitmapAreaBottom.top;
			int bBottom = bitmapAreaBottom.bottom - DISTANCE_STEP;
			bitmapAreaBottom.set((int) left, bTop, (int) right, bBottom);
			return true;
		} else {
			return false;
		}
	}

	public boolean closingRegionBottom() {
		float left = rectBottom.left;
		float right = rectBottom.right;
		float top = rectBottom.top;
		float bottom = rectBottom.bottom;
		if (top >= touchY) {
			rectBottom.set(left, top - DISTANCE_STEP, right, bottom);
			int bTop = bitmapAreaBottom.top;
			int bBottom = bitmapAreaBottom.bottom + DISTANCE_STEP;
			bitmapAreaBottom.set((int) left, bTop, (int) right, bBottom);
			return true;
		} else {
			return false;
		}
	}

	public void swapRegions(){
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

	public float getCenterY(){
		return rectAll.centerY();
	}
}
