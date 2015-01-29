package com.serega.origamicontrol.ui.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import com.serega.origamicontrol.origamicontrol.helpers.OrigamiAdapter;
import com.serega.origamicontrol.origamicontrol.view.OrigamiView;
import com.serega.origamicontrol.R;


public class MainActivity extends ActionBarActivity {

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
			    R.mipmap.jocker_dark
	    };

	    OrigamiAdapter adapter = new OrigamiAdapter(this, bitmaps);
	    origamiView.setAdapter(adapter);
    }


}
