package com.example.cliforcast.database;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@androidx.room.Dao
public interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCity(Weather weather);

    @Query("select * from cities where lat = :lat and lon = :lon")
    Weather getCity(double lat,double lon);


}
