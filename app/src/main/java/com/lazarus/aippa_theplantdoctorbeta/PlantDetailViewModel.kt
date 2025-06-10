package com.lazarus.aippa_theplantdoctorbeta

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class PlantDetailViewModel(application: Application, private val plantId: Long) : AndroidViewModel(application) {

    private val plantDao: PlantDao = AppDatabase.getDatabase(application).plantDao()
    private val gardenLogDao: GardenLogDao = AppDatabase.getDatabase(application).gardenLogDao()

    val plant: LiveData<Plant> = liveData {
        emit(plantDao.getPlant(plantId)!!)
    }

    val logs: LiveData<List<GardenLog>> = gardenLogDao.getLogsForPlant(plantId)

    fun addLog(log: GardenLog) {
        viewModelScope.launch {
            gardenLogDao.insert(log)
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