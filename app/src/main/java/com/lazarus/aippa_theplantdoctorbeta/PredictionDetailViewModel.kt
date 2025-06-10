package com.lazarus.aippa_theplantdoctorbeta

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class PredictionDetailViewModel(application: Application, private val historyId: Long) : AndroidViewModel(application) {

    private val repository: PredictionHistoryRepository

    private val _history = MutableLiveData<PredictionHistory?>()
    val history: LiveData<PredictionHistory?> = _history

    init {
        val historyDao = AppDatabase.getDatabase(application).predictionHistoryDao()
        repository = PredictionHistoryRepository(historyDao)
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = repository.getHistoryById(historyId)
        }
    }
}

class PredictionDetailViewModelFactory(private val application: Application, private val historyId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredictionDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PredictionDetailViewModel(application, historyId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 