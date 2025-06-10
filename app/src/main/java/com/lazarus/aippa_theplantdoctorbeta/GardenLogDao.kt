package com.lazarus.aippa_theplantdoctorbeta

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GardenLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gardenLog: GardenLog)

    @Query("SELECT * FROM garden_logs WHERE plantId = :plantId ORDER BY date DESC")
    fun getLogsForPlant(plantId: Long): LiveData<List<GardenLog>>
} 