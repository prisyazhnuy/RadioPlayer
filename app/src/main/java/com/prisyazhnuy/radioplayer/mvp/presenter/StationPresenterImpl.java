package com.prisyazhnuy.radioplayer.mvp.presenter;

import android.content.Context;
import android.util.Log;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.prisyazhnuy.radioplayer.db.DBService;
import com.prisyazhnuy.radioplayer.models.Station;
import com.prisyazhnuy.radioplayer.mvp.view.StationExplorerView;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.realm.Realm;

/**
 * Dell on 23.07.2017.
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
        Disposable subscribe = mDBService.getAll()
                .subscribe(new Consumer<List<Station>>() {
                    @Override
                    public void accept(List<Station> stations) throws Exception {
                        if (stations.isEmpty()) {
                            getView().showEmptyList();
                        } else {
                            getView().showStations(stations);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("DBService", "onError", throwable);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("DBService", "onComplete");
                    }
                });
        Log.d("DBService", "isDesposed " + subscribe.isDisposed());
        disposable.add(subscribe);
        Log.d("DBService", "isDesposed " + subscribe.isDisposed());

    }

    @Override
    public void removeStation(long id) {
        Disposable subscribe = mDBService.deleteStation(id).subscribe(new Consumer<Long>() {
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
       // disposable.dispose();
        disposable.clear();
    }

    @Override
    public void stationClicked(Station station) {
        getView().showEditStationDialog(station);
    }
}
