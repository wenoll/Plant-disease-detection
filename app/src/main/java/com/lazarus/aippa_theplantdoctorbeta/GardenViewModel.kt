package com.lazarus.aippa_theplantdoctorbeta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GardenViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlantRepository
    val allPlants: LiveData<List<Plant>>
    val allHistory: LiveData<List<PredictionHistory>>

    init {
        val database = AppDatabase.getDatabase(application)
        val plantDao = database.plantDao()
        val predictionHistoryDao = database.predictionHistoryDao()
        val gardenLogDao = database.gardenLogDao()
        repository = PlantRepository(plantDao, predictionHistoryDao, gardenLogDao)
        allPlants = repository.allPlants
        allHistory = repository.allHistory
    }

    fun insert(plant: Plant) = viewModelScope.launch {
        repository.insert(plant)
    }

    suspend fun insertPrediction(history: PredictionHistory): Long {
        return repository.insertPrediction(history)
    }

    fun insertGardenLog(gardenLog: GardenLog) = viewModelScope.launch {
        repository.insertGardenLog(gardenLog)
    }

    fun getLogsForPlant(plantId: Long): LiveData<List<GardenLog>> {
        return repository.getLogsForPlant(plantId)
    }
} 