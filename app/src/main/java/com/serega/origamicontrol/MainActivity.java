package com.serega.origamicontrol;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	    OrigamiView origamiView = (OrigamiView) findViewById(R.id.origami_view);

	    int[] bitmaps = {
			    R.mipmap.batman,
			    R.mipmap.jocker,
			    R.mipmap.cat,
			    R.mipmap.cobblepot,
			    R.mipmap.bane,
			    R.mipmap.jocker_dark
	    };

	    OrigamiAdapter adapter = new OrigamiAdapter(this, bitmaps);
	    origamiView.setAdapter(adapter);
    }


}
