package com.example.finalproject;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LevelEntity.class}, version =1, exportSchema = false)
public abstract class LevelDatabase extends RoomDatabase {
    public abstract LevelDao levelDao();

    private  static LevelDatabase INSTANCE;

    public static synchronized LevelDatabase getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            LevelDatabase.class, "game-db")
                    .fallbackToDestructiveMigration(true)
                    .build();

        }

        return INSTANCE;

    }


}

