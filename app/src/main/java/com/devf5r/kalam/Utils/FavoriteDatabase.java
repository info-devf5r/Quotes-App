package com.devf5r.kalam.Utils;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.devf5r.kalam.Model.FavoriteList;

@Database(entities={FavoriteList.class},version = 1)
public abstract class FavoriteDatabase extends RoomDatabase {

    public abstract FavoriteDao favoriteDao();


}
