package com.serega.origamicontrol.origamicontrol.interfaces;

import android.view.MotionEvent;

public interface GestureObserver {
	void onDownDetected(MotionEvent e);
	void onUpDetected(MotionEvent e);
	void onScrollDetected(MotionEvent e, boolean direction);
	void onFlingDetected(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
}
