package com.lazarus.aippa_theplantdoctorbeta

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: Plant): Long

    @Update
    suspend fun update(plant: Plant)

    @Delete
    suspend fun delete(plant: Plant)

    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlant(plantId: Long): Plant?

    @Query("SELECT * FROM plants ORDER BY name ASC")
    fun getAllPlants(): LiveData<List<Plant>>
} 