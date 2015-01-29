package com.serega.origamicontrol.origamicontrol.helpers;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Indicators {
	private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static final int indicatorColor = Color.LTGRAY;
	private static final int currentIndicatorColor = Color.RED;

	public void draw(Canvas canvas, int count, int currentPosition) {
		int gapBetween = Config.DEFAULT_MARGIN_BETWEEN;
		int radius = Config.INDICATOR_RADIUS;
		int circlesWidth = ((radius << 1) + gapBetween) * (count - 1);
		float centerX = canvas.getWidth() >> 1;
		float startX = centerX - (circlesWidth >> 1);
		float height = canvas.getHeight();

		for (int i = 0; i < count; i++) {
			circlePaint.setColor(i == currentPosition ? currentIndicatorColor : indicatorColor);
			canvas.drawCircle(startX, height - radius * 3, radius, circlePaint);
			startX += (radius << 1) + gapBetween;
		}
	}
}
