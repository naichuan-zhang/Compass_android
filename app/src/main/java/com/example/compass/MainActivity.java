package com.example.compass;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CompassView cv = (CompassView)this.findViewById(R.id.compassView);
        cv.setBearing(45);
    }
}
