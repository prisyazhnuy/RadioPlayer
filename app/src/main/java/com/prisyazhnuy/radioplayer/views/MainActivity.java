package com.prisyazhnuy.radioplayer.views;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.prisyazhnuy.radioplayer.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void addStation(View view) {
        startActivity(new Intent(this, FillStationActivity.class));
    }

    public void allStations(View view) {
        startActivity(new Intent(this, StationExplorerActivity.class));
    }
}
