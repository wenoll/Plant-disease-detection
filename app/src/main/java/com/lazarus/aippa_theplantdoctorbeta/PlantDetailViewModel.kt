package com.lazarus.aippa_theplantdoctorbeta

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class PlantDetailViewModel(application: Application, private val plantId: Long) : AndroidViewModel(application) {

    private val repository: PlantRepository
    
    val plant: LiveData<Plant>
    val timeline: MediatorLiveData<List<TimelineItem>> = MediatorLiveData()

    init {
        val database = AppDatabase.getDatabase(application)
        val plantDao = database.plantDao()
        val predictionHistoryDao = database.predictionHistoryDao()
        val gardenLogDao = database.gardenLogDao()
        repository = PlantRepository(plantDao, predictionHistoryDao, gardenLogDao)
        
        plant = liveData {
            emit(repository.getPlant(plantId)!!)
        }

        val logsSource = repository.getLogsForPlant(plantId)
        val predictionsSource = repository.getPredictionsForPlant(plantId)

        var logs: List<GardenLog>? = null
        var predictions: List<PredictionHistory>? = null

        fun updateTimeline() {
            if (logs != null && predictions != null) {
                val predictionMap = predictions!!.associateBy { it.id }
                val combinedList = logs!!.map { log ->
                    if (log.activityType == ActivityType.DIAGNOSIS && log.predictionId != null) {
                        predictionMap[log.predictionId]?.let { prediction ->
                            TimelineItem.Diagnosis(log, prediction)
                        } ?: TimelineItem.Log(log) // Fallback if prediction not found
                    } else {
                        TimelineItem.Log(log)
                    }
                }
                timeline.value = combinedList
            }
        }

        timeline.addSource(logsSource) {
            logs = it
            updateTimeline()
        }
        timeline.addSource(predictionsSource) {
            predictions = it
            updateTimeline()
        }
    }

    fun addLog(log: GardenLog) {
        viewModelScope.launch {
            repository.insertGardenLog(log)
        }
    }
    
    fun updateLog(log: GardenLog) {
        viewModelScope.launch {
            repository.updateGardenLog(log)
        }
    }
    
    fun deleteLog(log: GardenLog) {
        viewModelScope.launch {
            repository.deleteGardenLog(log)
        }
    }
    
    fun deleteDiagnosis(item: TimelineItem.Diagnosis) {
        viewModelScope.launch {
            // 先删除诊断记录
            repository.deletePrediction(item.predictionHistory)
            // 再删除对应的日志记录
            repository.deleteGardenLog(item.gardenLog)
        }
    }
    
    suspend fun getLogById(logId: Long): GardenLog? {
        return repository.getLogById(logId)
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