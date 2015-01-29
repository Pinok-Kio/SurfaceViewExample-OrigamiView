package com.serega.origamicontrol.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.serega.origamicontrol.R;

public class TestActivity extends Activity {
    public static final String EXTRA_INDEX = "INDEX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery);

        findViewById(R.id.image_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_INDEX, 0);
                startActivity(intent);
            }
        });

        findViewById(R.id.image_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_INDEX, 1);
                startActivity(intent);
            }
        });

        findViewById(R.id.image_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_INDEX, 2);
                startActivity(intent);
            }
        });

        findViewById(R.id.image_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_INDEX, 3);
                startActivity(intent);
            }
        });

        findViewById(R.id.image_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_INDEX, 4);
                startActivity(intent);
            }
        });

        findViewById(R.id.image_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_INDEX, 5);
                startActivity(intent);
            }
        });
    }
}
