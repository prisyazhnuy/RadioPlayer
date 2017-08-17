package com.prisyazhnuy.radioplayer.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.prisyazhnuy.radioplayer.R;
import com.prisyazhnuy.radioplayer.adapters.SimpleTouchHelperCallback;
import com.prisyazhnuy.radioplayer.adapters.StationAdapter;
import com.prisyazhnuy.radioplayer.models.Station;
import com.prisyazhnuy.radioplayer.mvp.presenter.StationPresenter;
import com.prisyazhnuy.radioplayer.mvp.presenter.StationPresenterImpl;
import com.prisyazhnuy.radioplayer.mvp.view.StationExplorerView;

import java.util.List;

public class StationExplorerActivity extends MvpActivity<StationExplorerView, StationPresenter> implements StationExplorerView {

    private static final int UPDATE_STATION_CODE = 1;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_explorer);

        mRecyclerView = (RecyclerView) findViewById(R.id.rvStations);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        getPresenter().loadStations();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.station_explorer_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_station) {
            startActivityForResult(new Intent(this, FillStationActivity.class), UPDATE_STATION_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public StationPresenter createPresenter() {
        return new StationPresenterImpl(this);
    }

    @Override
    public void showStations(List<Station> stations) {
        StationAdapter stationAdapter = new StationAdapter(this, getPresenter(), stations);
        mRecyclerView.setAdapter(stationAdapter);
        ItemTouchHelper.Callback callback = new SimpleTouchHelperCallback(stationAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void showEmptyList() {
        Toast.makeText(this, "There are no stations", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showDeleteResult() {
        Toast.makeText(this, "Item was deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showUpdateResult() {
//        Toast.makeText(this, "Item was updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showEditStationDialog(Station station) {
        Intent editStation = new Intent(this, FillStationActivity.class);
        editStation.putExtra("station", station);
        startActivityForResult(editStation, UPDATE_STATION_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case UPDATE_STATION_CODE:
                    getPresenter().loadStations();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        getPresenter().dispose();
        super.onDestroy();
    }
}
