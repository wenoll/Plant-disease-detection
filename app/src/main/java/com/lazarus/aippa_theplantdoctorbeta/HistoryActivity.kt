package com.lazarus.aippa_theplantdoctorbeta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyDao: PredictionHistoryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyDao = AppDatabase.getDatabase(applicationContext).predictionHistoryDao()
        
        val adapter = HistoryAdapter()
        val recyclerView = binding.historyRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        historyDao.getAllHistory().observe(this, Observer { history ->
            adapter.setData(history)
        })
    }
} 