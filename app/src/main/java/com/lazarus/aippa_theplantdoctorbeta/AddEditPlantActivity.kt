package com.lazarus.aippa_theplantdoctorbeta

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityAddEditPlantBinding
import kotlinx.coroutines.launch

class AddEditPlantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditPlantBinding
    private lateinit var viewModel: GardenViewModel
    private var currentPlantId: Long = INVALID_PLANT_ID

    companion object {
        const val EXTRA_PLANT_ID = "extra_plant_id"
        private const val INVALID_PLANT_ID: Long = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditPlantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(GardenViewModel::class.java)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currentPlantId = intent.getLongExtra(EXTRA_PLANT_ID, INVALID_PLANT_ID)

        if (currentPlantId == INVALID_PLANT_ID) {
            title = getString(R.string.title_add_plant)
        } else {
            title = "Edit Plant" // Consider adding this to strings.xml
            lifecycleScope.launch {
                viewModel.getPlant(currentPlantId)?.let { plant ->
                    binding.etPlantName.setText(plant.name)
                    binding.etPlantVariety.setText(plant.variety)
                    binding.etPlantLocation.setText(plant.location)
                }
            }
        }

        binding.btnSavePlant.setOnClickListener {
            savePlant()
        }
    }

    private fun savePlant() {
        val name = binding.etPlantName.text.toString().trim()
        val variety = binding.etPlantVariety.text.toString().trim()
        val location = binding.etPlantLocation.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Plant name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        val plant = Plant(
            id = if (currentPlantId == INVALID_PLANT_ID) 0 else currentPlantId,
            name = name,
            variety = variety,
            plantingDate = System.currentTimeMillis(), // Note: This updates planting date on every edit
            location = location
        )

        viewModel.insert(plant) // Room's onConflict strategy will handle update
        Toast.makeText(this, "Plant saved!", Toast.LENGTH_SHORT).show()
        finish() 
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 