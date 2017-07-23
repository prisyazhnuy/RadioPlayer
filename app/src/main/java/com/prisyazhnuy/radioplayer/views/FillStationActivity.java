package com.prisyazhnuy.radioplayer.views;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.prisyazhnuy.radioplayer.R;
import com.prisyazhnuy.radioplayer.mvp.presenter.FillStationPresenter;
import com.prisyazhnuy.radioplayer.mvp.presenter.FillStationPresenterImpl;
import com.prisyazhnuy.radioplayer.mvp.view.FillDataView;

public class FillStationActivity extends MvpActivity<FillDataView, FillStationPresenter> implements FillDataView {

    private EditText mEtName;
    private EditText mEtUrl;
    private Switch mSwhFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_station);

        mEtName = (EditText) findViewById(R.id.etName);
        mEtUrl = (EditText) findViewById(R.id.etUrl);
        mSwhFavorite = (Switch) findViewById(R.id.swhFavorite);
        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().addStation(mEtName.getText().toString(),
                        mEtUrl.getText().toString(), mSwhFavorite.isChecked());
            }
        });
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
}
