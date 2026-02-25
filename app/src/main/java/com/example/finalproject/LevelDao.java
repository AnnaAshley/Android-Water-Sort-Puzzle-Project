package com.example.finalproject;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.logging.Level;

//level data access object - base on developer site https://developer.android.com/training/data-storage/room#kts
@Dao
public interface LevelDao {
    @Query("SELECT * FROM levels")
    List<LevelEntity> getAllLevels();

    @Query("SELECT * FROM levels WHERE lid IN (:levelId)")
    LevelEntity getLevel(int levelId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLevel(LevelEntity level);

    @Update
    void updateLevel(LevelEntity level);

    @Query("UPDATE levels SET is_completed = :status WHERE lid = :levelId")
    void setLevelCompleted(int levelId, boolean status);

    @Query("UPDATE levels SET moveCount = :count WHERE lid = :levelId")
    void updateMoveCount(int levelId, int count);

}
