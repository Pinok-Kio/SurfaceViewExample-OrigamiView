package com.serega.origamicontrol;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OrigamiView extends SurfaceView implements SurfaceHolder.Callback {
    private DrawThread drawThread;

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
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (drawThread != null) {
                            drawThread.touch(event.getX(), event.getY());
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (drawThread != null) {
                            drawThread.update(event.getX(), event.getY());
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
        private float touchX;
        private float touchY;
        private List<Segment> segments;
        private boolean inTouch;
        private boolean isPrepared = false;
        private boolean isPrepared1 = false;

        DrawThread() {
            segments = new ArrayList<>();

        }

        private void setRunning(boolean running) {
            isRunning = running;
        }

        private void touch(float x, float y) {
            touchX = x;
            touchY = y;
            inTouch = true;
            isPrepared = false;
            prepareTouch(x, y);
        }

        private void update(float x, float y) {
            touchX = x;
            touchY = y;
            for (Segment s : segments) {
                if (s.process) {
                    s.update(y);
                }
            }
        }

        private void fingerUp(float x, float y) {
            inTouch = false;
            segments.get(0).process = true;
            if (segments.size() > 1) {
                segments.get(1).process = false;
            }
            if (segments.size() > 2) {
                segments.get(2).process = false;
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
                canvas.drawColor(Color.RED);
                if (!inTouch) {
//                    for (Segment s : segments) {
//                        if (s.process) {
//                            s.draw(canvas);
//                        }
//                    }
                    segments.get(0).draw(canvas);
                } else {
                    if(isPrepared) {
                        for (Segment s : segments) {
                            if (s.process) {
                                s.draw(canvas);
                            }
                        }
                    }
                }

                holder.unlockCanvasAndPost(canvas);
            }
        }

        private void prepare() {
            Segment segment = new Segment();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.batman);
            int width = getWidth();
            int height = getHeight();
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            segment.setBitmap(bitmap);
            segment.setRegion(new Rect(0, 0, getWidth(), getHeight()));
            segment.process = true;
            segments.add(segment);
        }

        private void prepareTouch(float x, float y) {
            Bitmap bitmap = segments.get(0).getBitmap();
            segments.get(0).process = false;
            Rect regionTop = new Rect(0, 0, getWidth(), (int) y);
            Rect regionBottom = new Rect(0, (int) y, getWidth(), getHeight());
            Segment topS = new Segment();
            topS.type = Segment.SType.TOP;
            topS.setBitmap(bitmap);
            topS.setRegion(regionTop);
            topS.initY = y;
            topS.process = true;
            Segment bottomS = new Segment();
            bottomS.setBitmap(bitmap);
            bottomS.setRegion(regionBottom);
            bottomS.type = Segment.SType.BOTTOM;
            segments.add(topS);
            segments.add(bottomS);
            bottomS.initY = y;
            bottomS.process = true;
            isPrepared = true;
        }
    }
}
