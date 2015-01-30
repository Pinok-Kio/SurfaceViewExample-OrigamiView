package com.serega.origamicontrol.origamicontrol.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.serega.origamicontrol.origamicontrol.helpers.Config;
import com.serega.origamicontrol.origamicontrol.helpers.OrigamiAdapter;
import com.serega.origamicontrol.origamicontrol.helpers.Segment;
import com.serega.origamicontrol.origamicontrol.interfaces.GestureInfoProvider;
import com.serega.origamicontrol.origamicontrol.interfaces.GestureObserver;

public class OrigamiView extends SurfaceView implements SurfaceHolder.Callback, GestureObserver {
	private DrawThread drawThread;
	private OrigamiAdapter adapter;
	private int startIndex;

	public OrigamiView(Context context) {
		super(context);
	}

	public OrigamiView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public OrigamiView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@TargetApi(21)
	public OrigamiView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Context context = getContext();
		if (context instanceof GestureInfoProvider) {
			((GestureInfoProvider) context).setGestureObserver(this);
		} else {
			Log.w("ORIGAMI_VIEW", "Activity doesn't implemented GestureInfoProvider interface. Gestures doesn't work");
		}
	}

	private void init() {
		getHolder().addCallback(this);
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		drawThread = new DrawThread();
		drawThread.setAdapter(adapter);
		drawThread.setRunning(true);
		drawThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		drawThread.setRunning(false);
		while (retry) {
			try {
				drawThread.join();
				retry = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDownDetected(MotionEvent e) {
		if (drawThread != null) {
			drawThread.touch(e.getX(), e.getY());
		}
	}

	@Override
	public void onUpDetected(MotionEvent e) {
		if (drawThread != null) {
			drawThread.fingerUp(e.getX(), e.getY());
		}
	}

	@Override
	public void onScrollDetected(MotionEvent e, boolean moveTop) {
		if (drawThread != null) {
			drawThread.update(e.getX(), e.getY(), moveTop);
		}
	}

	@Override
	public void onFlingDetected(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		boolean moveUp = e2.getY() - e1.getY() < 0;
		if (drawThread != null) {
			drawThread.flingDetected(moveUp);
		}
	}

	class DrawThread extends Thread {
		private boolean isRunning;
		private boolean inTouch;
		private final Segment s;

		DrawThread() {
			float gap = dpToPix(Config.DEFAULT_GAP_DP);
			float threshold = dpToPix(Config.DEFAULT_VIEV_OPENING_THRESHOLD_DP);
			s = new Segment(gap);
//			s.setViewOpeningThreshold(threshold);
		}

		private void setAdapter(OrigamiAdapter adapter) {
			s.setAdapter(adapter);
		}

		private void setRunning(boolean running) {
			isRunning = running;
		}

		private void touch(float x, float y) {
			inTouch = true;
			if (!s.isBusy()) {
				prepareTouch(x, y);
			}
		}

		private void update(float x, float y, boolean moveTop) {
			if (!s.isBusy()) {
				s.update(y, moveTop);
			}
		}

		private void fingerUp(float x, float y) {
			inTouch = false;
			if (!s.isBusy()) {
				s.processFingerUp();
			}
		}

		private void flingDetected(boolean direction) {
			if (!s.isBusy()) {
				s.onFling(direction);
			}
		}

		@Override
		public void run() {
			super.run();
			SurfaceHolder holder = getHolder();

			prepare();

			while (isRunning) {
				Canvas canvas = holder.lockCanvas();
				if (canvas == null) {
					return;
				}

				//If we doing some animation
				if (s.isBusy()) {
					s.processBusy();
					s.draw(canvas);
				} else if (!inTouch) {  //we don;t touch this view
					s.drawOrig(canvas);
				} else if (s.isReady()) { //bitmaps is loaded and ready to showing
					s.draw(canvas);
				} else {
					s.drawOrig(canvas);
				}
				holder.unlockCanvasAndPost(canvas);
			}
		}

		private void prepare() {
			OrigamiAdapter adapter = s.getAdapter();
			if (adapter == null) {
				throw new IllegalStateException("OrigamiAdapter is NULL");
			}
			Bitmap bitmap = adapter.getBitmap(getWidth(), getHeight(), startIndex);
			s.setBitmap(bitmap);
			s.setRectAll(0, 0, getWidth(), getHeight());
			s.setBitmapIndex(startIndex);
		}

		private void prepareTouch(float x, float y) {
	        s.prepareTouch(y);
		}
	}

	public void setAdapter(OrigamiAdapter adapter) {
		this.adapter = adapter;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	private float dpToPix(float dp) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

}
