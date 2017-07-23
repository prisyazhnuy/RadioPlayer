package com.prisyazhnuy.radioplayer.views;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.prisyazhnuy.radioplayer.R;
import com.prisyazhnuy.radioplayer.models.Station;
import com.prisyazhnuy.radioplayer.mvp.presenter.StationPresenter;
import com.prisyazhnuy.radioplayer.mvp.presenter.StationPresenterImpl;
import com.prisyazhnuy.radioplayer.mvp.view.StationExplorerView;

import java.util.List;

public class StationExplorerActivity extends MvpActivity<StationExplorerView, StationPresenter> implements StationExplorerView {

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_explorer);

        mRecyclerView = (RecyclerView) findViewById(R.id.rvStations);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        getPresenter().loadStations();
    }

    @NonNull
    @Override
    public StationPresenter createPresenter() {
        return new StationPresenterImpl(this);
    }

    @Override
    public void showStations(List<Station> stations) {
        mRecyclerView.setAdapter(new StationAdapter(this, stations));
    }

    @Override
    public void showEmptyList() {
        Toast.makeText(this, "There are no stations", Toast.LENGTH_SHORT).show();
    }
}