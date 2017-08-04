package com.prisyazhnuy.radioplayer.db;

import android.util.Log;

import com.prisyazhnuy.radioplayer.db.migration.Migration;
import com.prisyazhnuy.radioplayer.db.models.StationRealmModel;
import com.prisyazhnuy.radioplayer.models.Station;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Dell on 23.07.2017.
 */

public class DBService {
    private RealmConfiguration mConfig = new RealmConfiguration.Builder()
            .schemaVersion(1)
            .migration(new Migration())
            .build();

    public <T extends RealmObject> Observable<Long> delete(long id, final Class<T> clazz) {
        final Realm realm = Realm.getInstance(mConfig);
        return Observable.just(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
//                .flatMap(new Function<Long, ObservableSource<Long>>() {
//                    @Override
//                    public ObservableSource<Long> apply(Long t) throws Exception {
//                        return Observable.just(t);
//                    }
//                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        realm.beginTransaction();

                    }
                })
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        realm.commitTransaction();
                        realm.close();
                    }
                })
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long o) throws Exception {
                        RealmResults<StationRealmModel> rows = realm.where(StationRealmModel.class).equalTo("id", o).findAll();
                        rows.deleteAllFromRealm();
                    }
                });
    }

    public Observable<Station> updateList(List<Station> items) {
//        final Realm realm = Realm.getInstance(mConfig);
        return Observable.just(items)
//                .subscribeOn(Schedulers.computation())
                .flatMapIterable(new Function<List<Station>, Iterable<? extends Station>>() {
                    @Override
                    public Iterable<? extends Station> apply(List<Station> stations) throws Exception {
                        return stations;
                    }

                })
                .flatMap(new Function<Station, ObservableSource<Station>>() {
                    @Override
                    public ObservableSource<Station> apply(Station station) throws Exception {
                        return Observable.just(station)
                                .map(new Function<Station, Station>() {
                                    @Override
                                    public Station apply(Station station) throws Exception {
                                        Log.d("DBService", "thread: " + Thread.currentThread());
                                        final Realm realm = Realm.getInstance(mConfig);
                                        realm.beginTransaction();
                                        StationRealmModel model = realm.where(StationRealmModel.class)
                                                .equalTo("id", station.getId())
                                                .findFirst();
                                        if (model != null) {
                                            model.setFavourite(station.isFavourite());
                                            model.setPosition(station.getPosition());
                                            model.setUrl(station.getUrl());
                                            model.setName(station.getName());
                                        }
                                        realm.commitTransaction();
                                        realm.close();
                                        return station;
                                    }
                                })
                                .subscribeOn(Schedulers.computation());
                    }
                })
//                .doOnSubscribe(new Consumer<Disposable>() {
//                    @Override
//                    public void accept(Disposable disposable) throws Exception {
//                        realm.beginTransaction();
//                    }
//                })
//                .doOnDispose(new Action() {
//                    @Override
//                    public void run() throws Exception {
//                        realm.commitTransaction();
//                        realm.close();
//                    }
//                })
//                .doOnNext(new Consumer<Station>() {
//                    @Override
//                    public void accept(Station station) throws Exception {
//                        long id = station.getId();
//                        Log.d("DBService", "thread: " + Thread.currentThread());
//                        int position = station.getPosition();
//                        final Realm realm = Realm.getInstance(mConfig);
//                        realm.beginTransaction();
//                        StationRealmModel model = realm.where(StationRealmModel.class)
//                                .equalTo("id", id)
//                                .findFirst();
//                        if (model != null) {
//                            model.setPosition(position);
//                        }
//                        realm.commitTransaction();
//                        realm.close();
//                    }
//                })
//                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                ;
    }

//    public Observable<Long> update(Long id, final int position) {
//        final Realm realm = Realm.getInstance(mConfig);
//        return Observable.just(id)
//                .flatMap(new Func1<Long, Observable<Long>>() {
//                    @Override
//                    public Observable<Long> call(Long t) {
//                        return Observable.just(t);
//                    }
//                })
//                .doOnSubscribe(new Action0() {
//                    @Override
//                    public void call() {
//                        realm.beginTransaction();
//                    }
//                })
//                .doOnUnsubscribe(new Action0() {
//                    @Override
//                    public void call() {
//                        realm.commitTransaction();
//                        realm.close();
//                    }
//                })
//                .doOnNext(new Action1<Long>() {
//                              @Override
//                              public void call(Long o) {
//                                  StationRealmModel model = realm.where(StationRealmModel.class).equalTo("id", o).findFirst();
//                                  model.setPosition(position);
//                                  realm.copyToRealmOrUpdate(model);
//                              }
//                          }
//                );
//    }

    public <T extends RealmObject> Observable<T> save(T object, Class<T> clazz) {
        final Realm realm = Realm.getInstance(mConfig);
        long id;
        int position;
        try {
            id = realm.where(clazz).max("id").longValue() + 1;
            position = realm.where(clazz).max("position").intValue() + 1;
        } catch (Exception e) {
            id = 0L;
            position = 0;
        }
        ((StationRealmModel) object).setId(id);
        ((StationRealmModel) object).setPosition(position);
        return Observable.just(object)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
//                .flatMap(new Function<T, ObservableSource<T>>() {
//                    @Override
//                    public ObservableSource<T> apply(T t) throws Exception {
//                        return Observable.just(t);
//                    }
//                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        realm.beginTransaction();

                    }
                })
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        realm.commitTransaction();
                        realm.close();
                    }
                })
                .doOnNext(new Consumer<T>() {
                    @Override
                    public void accept(T t) throws Exception {
                        realm.copyToRealm(t);
                    }
                });
    }

    public <T extends RealmObject> Observable<List<T>> getAll(Class<T> clazz) {
        final Realm realm = Realm.getInstance(mConfig);

        return Observable.just(clazz)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
//                .flatMap(new Function<Class<T>, ObservableSource<Class<T>>>() {
//                    @Override
//                    public ObservableSource<Class<T>> apply(Class<T> tClass) throws Exception {
//                        return Observable.just(tClass);
//                    }
//                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        realm.beginTransaction();
                    }
                }).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        realm.commitTransaction();
                        realm.close();
                    }
                }).map(new Function<Class<T>, List<T>>() {
                    @Override
                    public List<T> apply(Class<T> tClass) throws Exception {
                        return new ArrayList<>(realm.where(tClass).findAllSorted("position", Sort.DESCENDING));
                    }
                });
    }

    public <T extends RealmObject> Observable<List<T>> getFavourite(Class<T> clazz) {
        final Realm realm = Realm.getInstance(mConfig);

        return Observable.just(clazz)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
//                .flatMap(new Function<Class<T>, ObservableSource<Class<T>>>() {
//                    @Override
//                    public ObservableSource<Class<T>> apply(Class<T> tClass) throws Exception {
//                        return Observable.just(tClass);
//                    }
//                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        realm.beginTransaction();
                    }
                }).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        realm.commitTransaction();
                        realm.close();
                    }
                }).map(new Function<Class<T>, List<T>>() {
                    @Override
                    public List<T> apply(Class<T> tClass) throws Exception {
                        return new ArrayList<>(realm.where(tClass)
                                .equalTo("isFavourite", true)
                                .findAllSorted("position", Sort.DESCENDING));
                    }
                });
    }
}
