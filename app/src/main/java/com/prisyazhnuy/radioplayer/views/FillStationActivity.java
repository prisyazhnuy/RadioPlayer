package com.prisyazhnuy.radioplayer.views;

import android.app.ActionBar;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.prisyazhnuy.radioplayer.R;
import com.prisyazhnuy.radioplayer.models.Station;
import com.prisyazhnuy.radioplayer.mvp.presenter.FillStationPresenter;
import com.prisyazhnuy.radioplayer.mvp.presenter.FillStationPresenterImpl;
import com.prisyazhnuy.radioplayer.mvp.view.FillDataView;

public class FillStationActivity extends MvpActivity<FillDataView, FillStationPresenter> implements FillDataView {

    private EditText mEtName;
    private EditText mEtUrl;
    private Switch mSwhFavorite;
    private Button mBtnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_station);

        mEtName = (EditText) findViewById(R.id.etName);
        mEtUrl = (EditText) findViewById(R.id.etUrl);
        mSwhFavorite = (Switch) findViewById(R.id.swhFavorite);
        mBtnAdd = (Button) findViewById(R.id.btnAdd);
        getStation();
    }

    private void getStation() {
        Intent intent = getIntent();
        if (intent != null) {
            Station station = intent.getParcelableExtra("station");
            getPresenter().fillStation(station);
        }
    }

    @NonNull
    @Override
    public FillStationPresenter createPresenter() {
        return new FillStationPresenterImpl(getApplicationContext());
    }

    @Override
    public void successData() {
        mSwhFavorite.setChecked(false);
        mEtName.setText("");
        mEtUrl.setText("");
        Toast.makeText(this, "Station was added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void failedData() {

    }

    @Override
    public void showStation(final Station station) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Edit station");
        }
        if (mBtnAdd != null) {
            mBtnAdd.setText("Update");
            mBtnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    station.setName(mEtName.getText().toString());
                    station.setUrl(mEtUrl.getText().toString());
                    station.setFavourite(mSwhFavorite.isChecked());
                    getPresenter().updateStation(station);
                }
            });
        }
        if (mEtName != null) {
            mEtName.setText(station.getName());
        }
        if (mEtUrl != null) {
            mEtUrl.setText(station.getUrl());
        }
        if (mSwhFavorite != null) {
            mSwhFavorite.setChecked(station.isFavourite());
        }
    }

    @Override
    public void createStation() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Create station");
        }
        if (mBtnAdd != null) {
            mBtnAdd.setText("Add");
            mBtnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPresenter().addStation(mEtName.getText().toString(),
                            mEtUrl.getText().toString(), mSwhFavorite.isChecked());
                }
            });
        }
    }

    @Override
    public void successUpdate() {
        Toast.makeText(this, "Station was updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}
