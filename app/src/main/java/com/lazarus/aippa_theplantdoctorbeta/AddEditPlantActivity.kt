package com.lazarus.aippa_theplantdoctorbeta

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityAddEditPlantBinding
import kotlinx.coroutines.launch
import java.io.File
import com.lazarus.aippa_theplantdoctorbeta.utils.ImageUtils

class AddEditPlantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditPlantBinding
    private lateinit var viewModel: GardenViewModel
    private var currentPlantId: Long = INVALID_PLANT_ID
    private var currentImageUri: Uri? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                showImageSourceDialog()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentImageUri?.let {
                binding.ivPlantImage.load(it)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentImageUri = it
            binding.ivPlantImage.load(it)
        }
    }

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
            title = "Edit Plant"
            lifecycleScope.launch {
                viewModel.getPlant(currentPlantId)?.let { plant ->
                    binding.etPlantName.setText(plant.name)
                    binding.etPlantVariety.setText(plant.variety)
                    binding.etPlantLocation.setText(plant.location)
                    plant.imagePath?.let {
                        currentImageUri = Uri.parse(it)
                        binding.ivPlantImage.load(currentImageUri)
                    }
                }
            }
        }

        binding.btnSelectImage.setOnClickListener {
            checkPermissionAndShowDialog()
        }

        binding.btnSavePlant.setOnClickListener {
            savePlant()
        }
    }

    private fun checkPermissionAndShowDialog() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                showImageSourceDialog()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // You can show a custom dialog explaining why you need the permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showImageSourceDialog() {
        val items = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_select_image_title))
            .setItems(items) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("IMG_", ".jpg", getExternalFilesDir(null))
        currentImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        takePictureLauncher.launch(currentImageUri)
    }

    private fun savePlant() {
        val name = binding.etPlantName.text.toString().trim()
        val variety = binding.etPlantVariety.text.toString().trim()
        val location = binding.etPlantLocation.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Plant name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        // 持久化保存图片URI
        val permanentUri = ImageUtils.getPermamentUri(this, currentImageUri)
        
        // 保存植物信息
        val plant = Plant(
            id = if (currentPlantId == INVALID_PLANT_ID) 0 else currentPlantId,
            name = name,
            variety = variety,
            plantingDate = System.currentTimeMillis(),
            location = location,
            imagePath = permanentUri?.toString()
        )

        viewModel.insert(plant)
        Toast.makeText(this, "Plant saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 