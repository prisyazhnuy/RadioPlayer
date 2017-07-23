package com.prisyazhnuy.radioplayer.db;

import com.prisyazhnuy.radioplayer.db.migration.Migration;
import com.prisyazhnuy.radioplayer.db.models.StationRealmModel;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmModel;
import io.realm.RealmObject;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Dell on 23.07.2017.
 *
 */

public class DBService {
    private RealmConfiguration mConfig = new RealmConfiguration.Builder()
            .schemaVersion(1)
            .migration(new Migration())
            .build();

    public <T extends RealmObject> Observable<T> save(T object, Class<T> clazz){
        final Realm realm = Realm.getInstance(mConfig);
        long id;
        try {
            id = realm.where(clazz).max("id").longValue() + 1;
        } catch (Exception e) {
            id = 0L;
        }
        ((StationRealmModel)object).setId(id);
        return Observable.just(object)
                .flatMap(new Func1<T, Observable<T>>() {
                    @Override
                    public Observable<T> call(T t) {
                        return Observable.just(t);
                    }
                })
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        realm.beginTransaction();
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        realm.commitTransaction();
                        realm.close();
                    }
                })
                .doOnNext(new Action1<RealmModel>() {
                              @Override
                              public void call(RealmModel o) {
                                  realm.copyToRealm(o);
                              }
                          }
                );
    }

    public <T extends RealmObject> Observable<List<T>> getAll(Class<T> clazz) {
        final Realm realm = Realm.getInstance(mConfig);

        return Observable.just(clazz)
                .flatMap(new Func1<Class<T>, Observable<Class<T>>>() {
                    @Override
                    public Observable<Class<T>> call(Class<T> tClass) {
                        return Observable.just(tClass);
                    }
                }).doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        realm.beginTransaction();
                    }
                }).doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        realm.commitTransaction();
                        realm.close();
                    }
                }).map(new Func1<Class<T>, List<T>>() {
                    @Override
                    public List<T> call(Class<T> o) {
                        return new ArrayList<>(realm.where(o).findAll());
                    }
                });
    }
}
