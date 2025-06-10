package com.lazarus.aippa_theplantdoctorbeta

import android.app.Application
import androidx.lifecycle.*
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val historyDao: PredictionHistoryDao = AppDatabase.getDatabase(application).predictionHistoryDao()

    private val _barChartData = MutableLiveData<BarData>()
    val barChartData: LiveData<BarData> = _barChartData

    private val _diseaseLabels = MutableLiveData<List<String>>()
    val diseaseLabels: LiveData<List<String>> = _diseaseLabels

    private val _reportSummary = MutableLiveData<String>()
    val reportSummary: LiveData<String> = _reportSummary

    init {
        generateReport()
    }

    private fun generateReport() {
        viewModelScope.launch {
            // Get data from the last year
            val oneYearAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }.timeInMillis
            val history = historyDao.getHistorySince(oneYearAgo)

            if (history.isEmpty()) {
                _reportSummary.postValue("No diagnostic data from the past year to generate a report.")
                return@launch
            }

            // --- 1. Process data for Bar Chart ---
            val frequencyMap = history.groupingBy { it.diseaseName }.eachCount()
            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()
            
            var i = 0
            for ((diseaseName, count) in frequencyMap) {
                entries.add(BarEntry(i.toFloat(), count.toFloat()))
                labels.add(diseaseName)
                i++
            }
            
            val dataSet = BarDataSet(entries, "Disease Count")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            dataSet.setDrawValues(true)

            _barChartData.postValue(BarData(dataSet))
            _diseaseLabels.postValue(labels)

            // --- 2. Generate Textual Summary ---
            val summaryBuilder = StringBuilder()
            val mostCommonDisease = frequencyMap.maxByOrNull { it.value }
            
            summaryBuilder.append("In the last year, you've recorded ${history.size} diagnoses across ${frequencyMap.size} different diseases.\n\n")
            
            mostCommonDisease?.let {
                summaryBuilder.append("• The most frequently diagnosed issue was **${it.key}**, recorded **${it.value}** times. Keep an eye out for its symptoms, especially during humid weather.\n")
            }

            summaryBuilder.append("• Regular monitoring and early detection are key. Keep up the great work in tracking your garden's health!")

            _reportSummary.postValue(summaryBuilder.toString())
        }
    }
} 