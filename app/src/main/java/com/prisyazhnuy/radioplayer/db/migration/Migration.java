package com.prisyazhnuy.radioplayer.db.migration;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Dell on 23.07.2017.
 */

public class Migration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            schema.create("StationRealmModel")
                    .addField("name", String.class)
                    .addField("url", String.class)
                    .addField("isFavourite", Boolean.class)
                    .addField("position", Integer.class);
        }
        if (oldVersion == 1) {
            schema.get("StationRealmModel")
                    .addField("subname", String.class);
        }
        if (oldVersion == 2) {
            schema.get("StationRealmModel")
                    .addField("time", Long.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.setLong("time", 0);
                        }
                    });
        }
        if (oldVersion == 3) {
            schema.get("StationRealmModel")
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.setLong("time", 0);
                        }
                    });
        }
        oldVersion++;
    }
}
