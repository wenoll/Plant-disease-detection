package com.lazarus.aippa_theplantdoctorbeta

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class PlantDetailViewModel(application: Application, private val plantId: Long) : AndroidViewModel(application) {

    private val repository: PlantRepository
    
    val plant: LiveData<Plant>
    val logs: LiveData<List<GardenLog>>

    init {
        val database = AppDatabase.getDatabase(application)
        val plantDao = database.plantDao()
        val predictionHistoryDao = database.predictionHistoryDao()
        val gardenLogDao = database.gardenLogDao()
        repository = PlantRepository(plantDao, predictionHistoryDao, gardenLogDao)
        
        plant = liveData {
            emit(repository.getPlant(plantId)!!)
        }
        logs = repository.getLogsForPlant(plantId)
    }

    fun addLog(log: GardenLog) {
        viewModelScope.launch {
            repository.insertGardenLog(log)
        }
    }

    fun deletePlant() {
        viewModelScope.launch {
            plant.value?.let {
                repository.delete(it)
            }
        }
    }
}

class PlantDetailViewModelFactory(private val application: Application, private val plantId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailViewModel(application, plantId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 