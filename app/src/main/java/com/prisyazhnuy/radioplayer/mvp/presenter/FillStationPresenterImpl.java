package com.prisyazhnuy.radioplayer.mvp.presenter;

import android.content.Context;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.prisyazhnuy.radioplayer.db.DBService;
import com.prisyazhnuy.radioplayer.db.models.StationRealmModel;
import com.prisyazhnuy.radioplayer.models.Station;
import com.prisyazhnuy.radioplayer.mvp.view.FillDataView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import io.reactivex.functions.Consumer;
import io.realm.Realm;
import io.realm.internal.Collection;

/**
 * Created by Dell on 23.07.2017.
 *
 */

public class FillStationPresenterImpl extends MvpBasePresenter<FillDataView> implements FillStationPresenter {

    private DBService mDBService;

    public FillStationPresenterImpl(Context context) {
        Realm.init(context);
        mDBService = new DBService();
    }

    @Override
    public void addStation(String name, String url, boolean isFavorite) {
        StationRealmModel model = new StationRealmModel();
        model.setName(name);
        model.setUrl(url);
        model.setFavourite(isFavorite);
        mDBService.save(model, StationRealmModel.class)
                .subscribe(new Consumer<StationRealmModel>() {
                    @Override
                    public void accept(StationRealmModel stationRealmModel) throws Exception {
                        getView().successData();
                    }
                });
    }

    @Override
    public void fillStation(Station station) {
        if (station == null) {
            getView().createStation();
        } else {
            getView().showStation(station);
        }
    }

    @Override
    public void updateStation(Station station) {
        mDBService.updateList(Collections.singletonList(station))
        .subscribe(new Consumer<Station>() {
            @Override
            public void accept(Station station) throws Exception {
                getView().successUpdate();
            }
        });
    }
}
