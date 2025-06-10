package com.lazarus.aippa_theplantdoctorbeta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityPlantDetailBinding
import com.lazarus.aippa_theplantdoctorbeta.databinding.DialogAddLogBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlantDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantDetailBinding
    private lateinit var viewModel: PlantDetailViewModel
    private lateinit var logAdapter: LogAdapter
    private var plantId: Long = -1

    // For adding images to logs
    private var tempLogImageUri: Uri? = null
    private var logImageDialogCallback: ((Uri) -> Unit)? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureForLogLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageForLogLauncher: ActivityResultLauncher<String>

    companion object {
        const val EXTRA_PLANT_ID = "extra_plant_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        plantId = intent.getLongExtra(EXTRA_PLANT_ID, -1)
        if (plantId == -1L) {
            finish()
            return
        }

        val viewModelFactory = PlantDetailViewModelFactory(application, plantId)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PlantDetailViewModel::class.java)

        registerLaunchers()
        setupUI()
        observeViewModel()
    }
    
    private fun registerLaunchers() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) showImageSourceDialog() else Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }

        takePictureForLogLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) tempLogImageUri?.let { logImageDialogCallback?.invoke(it) }
        }

        pickImageForLogLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { logImageDialogCallback?.invoke(it) }
        }
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

                it.imagePath?.let { path ->
                    binding.ivPlantHeaderImage.load(path) {
                        crossfade(true)
                        error(R.drawable.ic_broken_image)
                    }
                } ?: binding.ivPlantHeaderImage.setImageResource(R.drawable.ic_plant_placeholder)
            }
        }

        viewModel.logs.observe(this) { logs ->
            logs?.let { logAdapter.updateData(it) }
        }
    }

    private fun showAddLogDialog() {
        val dialogBinding = DialogAddLogBinding.inflate(layoutInflater)
        var selectedImageUri: Uri? = null

        val activityTypes = resources.getStringArray(R.array.activity_type_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityTypes)
        dialogBinding.spinnerActivityType.adapter = adapter

        logImageDialogCallback = { uri ->
            selectedImageUri = uri
            dialogBinding.ivLogImagePreview.visibility = View.VISIBLE
            dialogBinding.ivLogImagePreview.load(uri)
        }

        dialogBinding.btnAddLogImage.setOnClickListener {
            checkPermissionAndShowDialog()
        }

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle(R.string.dialog_title_log_activity)
            .setPositiveButton("Save") { _, _ ->
                val description = dialogBinding.etLogDescription.text.toString().trim()
                if (description.isNotEmpty()) {
                    val activityType = ActivityType.values()[dialogBinding.spinnerActivityType.selectedItemPosition]
                    val log = GardenLog(
                        plantId = plantId,
                        activityType = activityType,
                        date = System.currentTimeMillis(),
                        description = description,
                        imagePath = selectedImageUri?.toString()
                    )
                    viewModel.addLog(log)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                logImageDialogCallback = null // Clean up
                dialog.cancel()
            }
            .show()
    }

    private fun checkPermissionAndShowDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showImageSourceDialog()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showImageSourceDialog() {
        val items = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_select_image_title))
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        val photoFile = File.createTempFile("LOG_IMG_", ".jpg", getExternalFilesDir(null))
                        tempLogImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
                        takePictureForLogLauncher.launch(tempLogImageUri)
                    }
                    1 -> pickImageForLogLauncher.launch("image/*")
                }
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_plant_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_plant -> {
                val intent = Intent(this, AddEditPlantActivity::class.java)
                intent.putExtra(AddEditPlantActivity.EXTRA_PLANT_ID, plantId)
                startActivity(intent)
                true
            }
            R.id.action_delete_plant -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_plant_title)
            .setMessage(R.string.dialog_delete_plant_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deletePlant()
                finish() // Close the detail view after deletion
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
} 