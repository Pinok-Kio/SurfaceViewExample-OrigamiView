package com.serega.origamicontrol.origamicontrol.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import com.serega.origamicontrol.origamicontrol.helpers.Config;
import com.serega.origamicontrol.origamicontrol.helpers.Segment;
import com.serega.origamicontrol.origamicontrol.helpers.OrigamiAdapter;

public class OrigamiView extends SurfaceView implements SurfaceHolder.Callback {
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

    private void init() {
        getHolder().addCallback(this);

        setOnTouchListener(new OnTouchListener() {
            private static final float TOUCH_TOLERANCE = 10;
            float prevY = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        prevY = event.getY();
                        if (drawThread != null) {
                            drawThread.touch(event.getX(), event.getY());
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (drawThread != null) {
                            float y = event.getY();
                            if (Math.abs(prevY - y) > TOUCH_TOLERANCE) {
                                drawThread.update(event.getX(), y);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (drawThread != null) {
                            drawThread.fingerUp(event.getX(), event.getY());
                        }

                        return true;
                }
                return false;
            }
        });
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

    class DrawThread extends Thread {
        private boolean isRunning;
        private boolean inTouch;
        private final Segment s;

        DrawThread() {
            float gap = dpToPix(Config.DEFAULT_GAP_DP);
            float threshold = dpToPix(Config.DEFAULT_VIEV_OPENING_THRESHOLD_DP);
            s = new Segment(gap);
            s.setViewOpeningThreshold(threshold);
        }

        private void setAdapter(OrigamiAdapter adapter) {
            s.setAdapter(adapter);
        }

        private void setRunning(boolean running) {
            isRunning = running;
        }

        private void touch(float x, float y) {
            inTouch = true;
            prepareTouch(x, y);
        }

        private void update(float x, float y) {
            s.update(y);
        }

        private void fingerUp(float x, float y) {
            inTouch = false;
            s.processFingerUp();
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
                if (s.isBusy()) {
                    s.processBusy();
                    s.draw(canvas);

                } else if (!inTouch) {
                    s.drawOrig(canvas);
                } else {
                    if (s.isReady()) {
                        s.draw(canvas);
                    }
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
            s.prepareTouch(y, getHeight() / 2);
//	        s.prepareTouch(y, y);
        }

//        private void makeNotCentered(float y) {
//            RectF regionTop = new RectF(0, 0, getWidth(), y);
//            RectF regionBottom = new RectF(0, y, getWidth(), getHeight());
//            s.setRectTop(0, 0, getWidth(), y);
//            s.setRectBottom(0, y, getWidth(), getHeight());
//
//            s.setBitmapAreaTop(0, 0, getWidth(), (int) y);
//            s.setBitmapAreaBottom(0, (int) y, getWidth(), getHeight());
//            s.setRectBitmapTop(0, y, getWidth(), y);
//            s.setRectBitmapBottom(0, y, getWidth(), y);
//
//
//            s.setInnerRect(0, y, getWidth(), y);
//            s.touchY = y;
//        }
    }

    public void setAdapter(OrigamiAdapter adapter) {
        this.adapter = adapter;
    }

    public void setStartIndex(int startIndex){
        this.startIndex = startIndex;
    }

    private float dpToPix(float dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
