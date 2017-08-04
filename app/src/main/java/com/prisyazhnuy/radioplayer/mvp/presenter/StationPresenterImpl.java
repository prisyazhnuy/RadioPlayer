package com.prisyazhnuy.radioplayer.mvp.presenter;

import android.content.Context;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.prisyazhnuy.radioplayer.db.DBService;
import com.prisyazhnuy.radioplayer.db.models.StationRealmModel;
import com.prisyazhnuy.radioplayer.models.Station;
import com.prisyazhnuy.radioplayer.mvp.view.StationExplorerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.realm.Realm;

/**
 * Created by Dell on 23.07.2017.
 *
 */

public class StationPresenterImpl extends MvpBasePresenter<StationExplorerView> implements StationPresenter {

    private DBService mDBService;
    private CompositeDisposable disposable = new CompositeDisposable();

    public StationPresenterImpl(Context context) {
        Realm.init(context);
        mDBService = new DBService();
    }

    @Override
    public void loadStations() {
        Disposable subscribe = mDBService.getAll(StationRealmModel.class)
                .subscribe(new Consumer<List<StationRealmModel>>() {
                    @Override
                    public void accept(List<StationRealmModel> stationRealmModels) throws Exception {
                        List<Station> stations = new ArrayList<>(stationRealmModels.size());
                        for (StationRealmModel model : stationRealmModels) {
                            stations.add(new Station(model.getId(), model.getName(),
                                    model.getUrl(), model.getPosition(), model.isFavourite()));
                        }
                        if (stations.isEmpty()) {
                            getView().showEmptyList();
                        } else {
                            getView().showStations(stations);
                        }
                    }
                });
        disposable.add(subscribe);
    }

    @Override
    public void removeStation(long id) {
        Disposable subscribe = mDBService.delete(id, StationRealmModel.class).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                getView().showDeleteResult();
            }
        });
        disposable.add(subscribe);
    }

    @Override
    public void updatePosition(List<Station> items) {
//        mDBService.update(id, position).subscribe(new Action1<Long>() {
        Disposable subscribe = mDBService.updateList(items).subscribe(new Consumer<Station>() {
            @Override
            public void accept(Station station) throws Exception {
                getView().showUpdateResult();
            }
        });
        disposable.add(subscribe);
    }

    @Override
    public void dispose() {
        disposable.dispose();
    }

    @Override
    public void stationClicked(Station station) {
        getView().showEditStationDialog(station);
    }
}
