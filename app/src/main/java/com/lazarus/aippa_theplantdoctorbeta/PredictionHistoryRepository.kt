package com.lazarus.aippa_theplantdoctorbeta

import androidx.lifecycle.LiveData

class PredictionHistoryRepository(private val predictionHistoryDao: PredictionHistoryDao) {

    val allHistory: LiveData<List<PredictionHistory>> = predictionHistoryDao.getAllHistory()

    suspend fun getHistoryById(id: Long): PredictionHistory? {
        return predictionHistoryDao.getHistoryById(id)
    }

    suspend fun deleteHistory(history: PredictionHistory) {
        predictionHistoryDao.delete(history)
    }
} 