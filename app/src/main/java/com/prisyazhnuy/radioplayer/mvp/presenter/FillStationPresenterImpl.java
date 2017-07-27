package com.prisyazhnuy.radioplayer.mvp.presenter;

import android.content.Context;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.prisyazhnuy.radioplayer.db.DBService;
import com.prisyazhnuy.radioplayer.db.models.StationRealmModel;
import com.prisyazhnuy.radioplayer.mvp.view.FillDataView;

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
}
