package com.lazarus.aippa_theplantdoctorbeta

import androidx.lifecycle.LiveData

class PlantRepository(
    private val plantDao: PlantDao,
    private val predictionHistoryDao: PredictionHistoryDao,
    private val gardenLogDao: GardenLogDao
) {

    val allPlants: LiveData<List<Plant>> = plantDao.getAllPlants()
    val allHistory: LiveData<List<PredictionHistory>> = predictionHistoryDao.getAllHistory()

    suspend fun insert(plant: Plant) {
        plantDao.insert(plant)
    }

    suspend fun delete(plant: Plant) {
        plantDao.delete(plant)
    }

    suspend fun insertPrediction(history: PredictionHistory): Long {
        return predictionHistoryDao.insert(history)
    }

    suspend fun insertGardenLog(gardenLog: GardenLog) {
        gardenLogDao.insert(gardenLog)
    }
    
    suspend fun updateGardenLog(gardenLog: GardenLog) {
        gardenLogDao.update(gardenLog)
    }
    
    suspend fun deleteGardenLog(gardenLog: GardenLog) {
        gardenLogDao.delete(gardenLog)
    }
    
    suspend fun getLogById(logId: Long): GardenLog? {
        return gardenLogDao.getLogById(logId)
    }

    suspend fun getPlant(id: Long): Plant? {
        return plantDao.getPlant(id)
    }

    fun getLogsForPlant(plantId: Long): LiveData<List<GardenLog>> {
        return gardenLogDao.getLogsForPlant(plantId)
    }
    
    fun getPredictionsForPlant(plantId: Long): LiveData<List<PredictionHistory>> {
        return predictionHistoryDao.getPredictionsForPlant(plantId)
    }
    
    suspend fun deletePrediction(prediction: PredictionHistory) {
        predictionHistoryDao.delete(prediction)
    }
} 