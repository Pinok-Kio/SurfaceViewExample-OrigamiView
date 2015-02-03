package com.serega.origamicontrol.ui.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.serega.origamicontrol.origamicontrol.helpers.OrigamiAdapter;
import com.serega.origamicontrol.origamicontrol.interfaces.GestureInfoProvider;
import com.serega.origamicontrol.origamicontrol.interfaces.GestureObserver;
import com.serega.origamicontrol.origamicontrol.view.OrigamiView;
import com.serega.origamicontrol.R;


public class OrigamiViewActivity extends ActionBarActivity implements GestureInfoProvider{
    private GestureObserver gestureObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        int selectedIndex = intent.getIntExtra(TestActivity.EXTRA_INDEX, 0);

	    OrigamiView origamiView = (OrigamiView) findViewById(R.id.origami_view);
        origamiView.setStartIndex(selectedIndex);

	    int[] bitmaps = {
			    R.mipmap.batman,
			    R.mipmap.jocker,
			    R.mipmap.cat,
			    R.mipmap.cobblepot,
			    R.mipmap.bane,
			    R.mipmap.jocker_dark,
			    R.mipmap.cat_white
	    };

	    OrigamiAdapter adapter = new OrigamiAdapter(this, bitmaps);
	    origamiView.setAdapter(adapter);

        final GestureDetector detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                if(gestureObserver != null){
	                gestureObserver.onDownDetected(e);
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(gestureObserver != null){
	                gestureObserver.onFlingDetected(e1, e2, velocityX, velocityY);
                }
                return true;
            }
        });

        origamiView.setOnTouchListener(new View.OnTouchListener() {
	        private float oldY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

	            switch (event.getAction()){
		            case MotionEvent.ACTION_DOWN:
			            oldY = event.getY();
			            break;

		            case MotionEvent.ACTION_MOVE:
			            float newY = event.getY();
			            if(newY != oldY) {
				            boolean moveTop = oldY > newY;
				            if (gestureObserver != null) {
					            gestureObserver.onScrollDetected(event, moveTop);
				            }
				            oldY = newY;
			            }
			            break;

		            case MotionEvent.ACTION_UP:
			            if (gestureObserver != null) {
				            gestureObserver.onUpDetected(event);
			            }
			            break;
	            }

                return detector.onTouchEvent(event);
            }
        });
    }

	@Override
	public void setGestureObserver(GestureObserver observer) {
		gestureObserver = observer;
	}
}
