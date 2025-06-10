package com.lazarus.aippa_theplantdoctorbeta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityPlantDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.lazarus.aippa_theplantdoctorbeta.databinding.DialogAddLogBinding

class PlantDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantDetailBinding
    private lateinit var viewModel: PlantDetailViewModel
    private lateinit var logAdapter: LogAdapter
    private var plantId: Long = -1

    companion object {
        const val EXTRA_PLANT_ID = "extra_plant_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        plantId = intent.getLongExtra(EXTRA_PLANT_ID, -1)
        if (plantId == -1L) {
            finish() // Invalid ID, close activity
            return
        }

        val viewModelFactory = PlantDetailViewModelFactory(application, plantId)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PlantDetailViewModel::class.java)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        logAdapter = LogAdapter(emptyList())
        binding.rvLogs.apply {
            adapter = logAdapter
            layoutManager = LinearLayoutManager(this@PlantDetailActivity)
        }
        
        binding.fabAddLog.setOnClickListener {
            showAddLogDialog()
        }
    }

    private fun observeViewModel() {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        viewModel.plant.observe(this) { plant ->
            plant?.let {
                binding.toolbar.title = it.name
                binding.tvPlantVarietyDetail.text = it.variety
                binding.tvPlantLocationDetail.text = it.location
                binding.tvPlantDateDetail.text = getString(R.string.plant_detail_planted_on, dateFormatter.format(Date(it.plantingDate)))
            }
        }

        viewModel.logs.observe(this) { logs ->
            logs?.let { logAdapter.updateData(it) }
        }
    }

    private fun showAddLogDialog() {
        val dialogBinding = DialogAddLogBinding.inflate(layoutInflater)
        val dialogView = dialogBinding.root

        val activityTypes = resources.getStringArray(R.array.activity_type_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerActivityType.adapter = adapter

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(R.string.dialog_title_log_activity)
            .setPositiveButton("Save") { dialog, _ ->
                val description = dialogBinding.etLogDescription.text.toString().trim()
                if (description.isNotEmpty()) {
                    val selectedPosition = dialogBinding.spinnerActivityType.selectedItemPosition
                    
                    // This mapping is based on the order in the string-array. It's not ideal but will work.
                    val activityType = when (selectedPosition) {
                        0 -> ActivityType.WATERING
                        1 -> ActivityType.FERTILIZING
                        2 -> ActivityType.PRUNING
                        3 -> ActivityType.TREATMENT
                        4 -> ActivityType.NOTE
                        else -> ActivityType.NOTE
                    }

                    val log = GardenLog(
                        plantId = plantId,
                        activityType = activityType,
                        date = System.currentTimeMillis(),
                        description = description
                    )
                    viewModel.addLog(log)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 