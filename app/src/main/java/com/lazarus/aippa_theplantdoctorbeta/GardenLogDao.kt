package com.lazarus.aippa_theplantdoctorbeta

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface GardenLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gardenLog: GardenLog)

    @Update
    suspend fun update(gardenLog: GardenLog)

    @Delete
    suspend fun delete(gardenLog: GardenLog)

    @Query("SELECT * FROM garden_logs WHERE plantId = :plantId ORDER BY date DESC")
    fun getLogsForPlant(plantId: Long): LiveData<List<GardenLog>>
    
    @Query("SELECT * FROM garden_logs WHERE id = :logId")
    suspend fun getLogById(logId: Long): GardenLog?
} 