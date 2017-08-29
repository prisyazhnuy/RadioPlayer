package com.prisyazhnuy.radioplayer.mvp.presenter;

import android.content.Context;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.prisyazhnuy.radioplayer.db.DBService;
import com.prisyazhnuy.radioplayer.models.Station;
import com.prisyazhnuy.radioplayer.mvp.view.FillDataView;

import java.util.Collections;

import io.reactivex.functions.Consumer;
import io.realm.Realm;

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
    public void addStation(String name, String subname, String url, boolean isFavorite) {
        Station station = new Station(0, name, subname, url, 0, isFavorite);
//        StationRealmModel model = new StationRealmModel();
//        model.setName(name);
//        model.setUrl(url);
//        model.setFavourite(isFavorite);
        mDBService.saveStation(station)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long id) throws Exception {
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
