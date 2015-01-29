package com.serega.origamicontrol.origamicontrol.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class OrigamiAdapter {
	private int lastIndex;
	private final int[] bitmaps;
	public Context context;


	public OrigamiAdapter(Context context, int[] bitmapsId) {
		this.bitmaps = bitmapsId;
		this.context = context;
	}

	public int getBitmapsCount() {
		return bitmaps.length;
	}


	public Bitmap getBitmap(int width, int height, int index) {
		if (index >= bitmaps.length) {
			index = 0;
		}

		if (index < 0) {
			index = bitmaps.length - 1;
		}

		lastIndex = index;
		Bitmap b = BitmapFactory.decodeResource(context.getResources(), bitmaps[index]);
		return Bitmap.createScaledBitmap(b, width, height, true);
	}

	public int getLastIndex() {
		return lastIndex;
	}
}
