package com.example.cliforcast.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(exportSchema = false,entities ={Weather.class},version = 1)
public abstract class MyRoom extends RoomDatabase {
    public abstract Dao getDao();
}
