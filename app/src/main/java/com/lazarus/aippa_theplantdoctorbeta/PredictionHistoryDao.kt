package com.lazarus.aippa_theplantdoctorbeta

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PredictionHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PredictionHistory): Long

    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    fun getAllHistory(): LiveData<List<PredictionHistory>>
    
    @Query("UPDATE history_table SET feedback = :feedback WHERE id = :id")
    suspend fun updateFeedback(id: Long, feedback: String)

    @Query("DELETE FROM history_table WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * from history_table WHERE id = :id")
    suspend fun get(id: Long): PredictionHistory?

    @Query("DELETE FROM history_table")
    suspend fun clear()
} 