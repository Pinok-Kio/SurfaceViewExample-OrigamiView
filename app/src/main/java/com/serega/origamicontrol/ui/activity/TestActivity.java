package com.serega.origamicontrol.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.serega.origamicontrol.R;
import com.serega.origamicontrol.origamicontrol.helpers.OrigamiAdapter;

public class TestActivity extends Activity {
	public static final String EXTRA_INDEX = "INDEX";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_gallery);
		ImageView image1 = (ImageView) findViewById(R.id.image_1);
		ImageView image2 = (ImageView) findViewById(R.id.image_2);
		ImageView image3 = (ImageView) findViewById(R.id.image_3);
		ImageView image4 = (ImageView) findViewById(R.id.image_4);
		ImageView image5 = (ImageView) findViewById(R.id.image_5);
		ImageView image6 = (ImageView) findViewById(R.id.image_6);

		image1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TestActivity.this, OrigamiViewActivity.class);
				intent.putExtra(EXTRA_INDEX, 0);
				startActivity(intent);
			}
		});

		image2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TestActivity.this, OrigamiViewActivity.class);
				intent.putExtra(EXTRA_INDEX, 1);
				startActivity(intent);
			}
		});

		image3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TestActivity.this, OrigamiViewActivity.class);
				intent.putExtra(EXTRA_INDEX, 2);
				startActivity(intent);
			}
		});

		image4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TestActivity.this, OrigamiViewActivity.class);
				intent.putExtra(EXTRA_INDEX, 3);
				startActivity(intent);
			}
		});

		image5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TestActivity.this, OrigamiViewActivity.class);
				intent.putExtra(EXTRA_INDEX, 4);
				startActivity(intent);
			}
		});

		image6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TestActivity.this, OrigamiViewActivity.class);
				intent.putExtra(EXTRA_INDEX, 5);
				startActivity(intent);
			}
		});

		OrigamiAdapter adapter = new OrigamiAdapter(this, new int[]{
				R.mipmap.batman,
				R.mipmap.jocker,
				R.mipmap.cat,
				R.mipmap.cobblepot,
				R.mipmap.bane,
				R.mipmap.jocker_dark
		});

		image1.setImageBitmap(adapter.getBitmap(150, 150, 0));
		image2.setImageBitmap(adapter.getBitmap(150, 150, 1));
		image3.setImageBitmap(adapter.getBitmap(150, 150, 2));
		image4.setImageBitmap(adapter.getBitmap(150, 150, 3));
		image5.setImageBitmap(adapter.getBitmap(150, 150, 4));
		image6.setImageBitmap(adapter.getBitmap(150, 150, 5));
	}
}
