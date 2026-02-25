package com.example.finalproject;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
//used Developer site to underestand formatting https://developer.android.com/training/data-storage/room#kts

@Entity(tableName ="levels")
public class LevelEntity {
    @PrimaryKey(autoGenerate = true)
    public int lid;

    public String name;
    @ColumnInfo(name ="initial_layers") //JSON String of original puzzle
    public String initialLayers;

    @ColumnInfo(name = "current_layers") // JSON String of current state of level
    public String currentLayers;

    @ColumnInfo(name = "maxLayers")
    public int maxLayers;

    @ColumnInfo(name = "nOfTubes")
    public int nOfTubes;

    @ColumnInfo(name ="is_completed")
    public boolean isCompleted;

    @ColumnInfo(name = "moveCount")
    public int moveCount;

    public LevelEntity(int i, String initialLayers, String notStarted, int maxLayers, int nOfTubes) {
        lid = i;
        this.initialLayers = initialLayers;
        this.maxLayers = maxLayers;
        this.nOfTubes = nOfTubes;
        isCompleted = false;
    }

    public LevelEntity() {

    }
}
