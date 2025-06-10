package com.lazarus.aippa_theplantdoctorbeta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityGardenBinding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.content.Intent
import android.view.Menu
import android.view.MenuItem

class GardenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGardenBinding
    private lateinit var viewModel: GardenViewModel
    private lateinit var plantAdapter: PlantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGardenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.my_garden)

        viewModel = ViewModelProvider(this).get(GardenViewModel::class.java)

        setupRecyclerView()

        viewModel.allPlants.observe(this) { plants ->
            plantAdapter.updateData(plants)
            if (plants.isEmpty()) {
                binding.rvPlants.visibility = View.GONE
                binding.tvEmptyGarden.visibility = View.VISIBLE
            } else {
                binding.rvPlants.visibility = View.VISIBLE
                binding.tvEmptyGarden.visibility = View.GONE
            }
        }

        binding.fabAddPlant.setOnClickListener {
            val intent = Intent(this, AddEditPlantActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.garden_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_generate_report -> {
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        plantAdapter = PlantAdapter(emptyList())
        binding.rvPlants.apply {
            adapter = plantAdapter
            layoutManager = LinearLayoutManager(this@GardenActivity)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 