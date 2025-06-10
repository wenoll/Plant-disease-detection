package com.lazarus.aippa_theplantdoctorbeta

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface PredictionHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PredictionHistory): Long

    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    fun getAllHistory(): LiveData<List<PredictionHistory>>
    
    @Query("SELECT * FROM history_table WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getHistorySince(sinceTimestamp: Long): List<PredictionHistory>

    @Query("UPDATE history_table SET feedback = :feedback WHERE id = :id")
    suspend fun updateFeedback(id: Long, feedback: String)

    @Delete
    suspend fun delete(history: PredictionHistory)

    @Query("SELECT * from history_table WHERE id = :id")
    suspend fun getHistoryById(id: Long): PredictionHistory?

    @Query("DELETE FROM history_table")
    suspend fun clear()
} 