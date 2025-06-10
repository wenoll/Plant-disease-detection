package com.lazarus.aippa_theplantdoctorbeta

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityAddEditPlantBinding

class AddEditPlantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditPlantBinding
    private lateinit var viewModel: GardenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditPlantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(GardenViewModel::class.java)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.title_add_plant)

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

        val newPlant = Plant(
            name = name,
            variety = variety,
            plantingDate = System.currentTimeMillis(),
            location = location
        )

        viewModel.insert(newPlant)
        Toast.makeText(this, "Plant saved!", Toast.LENGTH_SHORT).show()
        finish() // Close the activity after saving
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 