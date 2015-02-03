package com.serega.origamicontrol.origamicontrol.helpers;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Indicators {
	private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static int indicatorColor = Color.LTGRAY;
	private static int currentIndicatorColor = Color.RED;
	public static final int NO_ANIMATION = -1;
	private final int radius = Constants.INDICATOR_RADIUS;

	public Indicators() {
		circlePaint.setStrokeWidth(radius);
		circlePaint.setStrokeCap(Paint.Cap.ROUND);
	}

	//	public void draw(Canvas canvas, int count, int currentPosition, float fraction) {
//		int gapBetween = Config.DEFAULT_MARGIN_BETWEEN;
//		int radius = Config.INDICATOR_RADIUS;
//		int circlesWidth = ((radius << 1) + gapBetween) * (count - 1);
//		float centerX = canvas.getWidth() >> 1;
//		float startX = centerX - (circlesWidth >> 1);
//		float height = canvas.getHeight();
//
//		for (int i = 0; i < count; i++) {
//			circlePaint.setColor(i == currentPosition ? currentIndicatorColor : indicatorColor);
//			canvas.drawCircle(startX, height - radius * 3, radius, circlePaint);
//			startX += (radius << 1) + gapBetween;
//		}
//	}

	public void draw(Canvas canvas, int count, int currentPosition, float fraction, boolean moveTop) {
		int gapBetween = Constants.DEFAULT_MARGIN_BETWEEN;
		int diameter = radius << 1;
		int circlesWidth = (diameter + gapBetween) * (count - 1);
		float centerX = canvas.getWidth() >> 1;
		float startX = centerX - (circlesWidth >> 1);
		float endX = diameter + gapBetween;
		float height = canvas.getHeight();
		float y = height - radius * 3;

		for (int i = 0; i < count; i++) {
			if ((i == currentPosition) ||
					(i == currentPosition + 1 && moveTop && fraction > 0.9 && fraction < 1) ||
					(i == currentPosition - 1 && !moveTop && fraction < 0.1 && fraction > 0 && currentPosition > 0)) {
				circlePaint.setColor(currentIndicatorColor);
			} else {
				circlePaint.setColor(indicatorColor);
			}

			canvas.drawCircle(startX, y, radius, circlePaint);

			if (fraction != NO_ANIMATION && fraction < 1 && fraction > 0) {

				if (moveTop) {
					if (i <= currentPosition) {
						if (i == currentPosition && i < count - 1) {
							float lineLength = gapBetween * fraction + radius;
							canvas.drawLine(startX + radius, y, startX + lineLength, y, circlePaint);
						}
					}
				} else {
					if (i <= currentPosition) {
						if (i == currentPosition && i > 0) {
							float lineLength = gapBetween * (1 - fraction) + radius;
							canvas.drawLine(startX - lineLength, y, startX, y, circlePaint);
						}
					}
				}
			}
			startX += endX;
		}
	}

	public void draw(Canvas canvas, int count, int currentPosition) {
		draw(canvas, count, currentPosition, NO_ANIMATION, false);
	}

	public void setIndicatorColor(int color){
		if(color != Constants.NOT_SET) {
			indicatorColor = color;
		}
	}

	public void setCurrentIndicatorColor(int color){
		if(color != Constants.NOT_SET) {
			currentIndicatorColor = color;
		}
	}
}
