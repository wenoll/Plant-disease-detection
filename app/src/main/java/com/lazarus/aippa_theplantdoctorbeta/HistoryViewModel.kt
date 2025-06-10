package com.lazarus.aippa_theplantdoctorbeta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PredictionHistoryRepository
    val allHistory: LiveData<List<PredictionHistory>>

    init {
        val historyDao = AppDatabase.getDatabase(application).predictionHistoryDao()
        repository = PredictionHistoryRepository(historyDao)
        allHistory = repository.allHistory
    }

    fun delete(history: PredictionHistory) {
        viewModelScope.launch {
            repository.deleteHistory(history)
        }
    }
} 