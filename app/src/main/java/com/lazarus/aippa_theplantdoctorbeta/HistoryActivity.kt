package com.lazarus.aippa_theplantdoctorbeta

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        val adapter = HistoryAdapter { history ->
            showDeleteConfirmationDialog(history)
        }
        
        binding.historyRecyclerView.adapter = adapter
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.allHistory.observe(this) { history ->
            adapter.setData(history)
        }
    }

    private fun showDeleteConfirmationDialog(history: PredictionHistory) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_history_title)
            .setMessage(R.string.dialog_delete_history_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.delete(history)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
} 