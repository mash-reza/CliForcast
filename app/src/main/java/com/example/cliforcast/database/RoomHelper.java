package com.example.cliforcast.database;

import android.content.Context;

import androidx.room.Room;

public class RoomHelper {
    private static MyRoom instance;
    public static MyRoom getInstance(Context context) {
        if(instance == null){
            instance = Room.databaseBuilder(context,MyRoom.class,"Climate").build();
        }
        return instance;
    }
}
