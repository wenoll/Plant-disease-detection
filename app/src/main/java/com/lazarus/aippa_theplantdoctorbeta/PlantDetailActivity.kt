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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.lazarus.aippa_theplantdoctorbeta.utils.ImageUtils
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityPlantDetailBinding
import com.lazarus.aippa_theplantdoctorbeta.databinding.DialogAddLogBinding
import com.lazarus.aippa_theplantdoctorbeta.databinding.DialogEditLogBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlantDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantDetailBinding
    private lateinit var viewModel: PlantDetailViewModel
    private lateinit var timelineAdapter: TimelineAdapter
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

        binding.rvLogs.layoutManager = LinearLayoutManager(this@PlantDetailActivity)
        
        binding.fabAddLog.setOnClickListener {
            showAddLogDialog()
        }
    }

    private fun observeViewModel() {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        viewModel.plant.observe(this) { plant ->
            plant?.let {
                binding.collapsingToolbar.title = it.name
                binding.tvPlantVarietyDetail.text = it.variety
                binding.tvPlantLocationDetail.text = it.location
                binding.tvPlantDateDetail.text = getString(R.string.plant_detail_planted_on, dateFormatter.format(Date(it.plantingDate)))

                it.imagePath?.let { path ->
                    // 添加日志输出路径信息
                    android.util.Log.d("PlantDetail", "Loading image from path: $path")
                    try {
                        // 判断路径是否是文件路径
                        val file = File(path)
                        if (file.exists()) {
                            // 如果文件存在，直接加载文件
                            binding.ivPlantHeaderImage.load(file) {
                                crossfade(true)
                                error(R.drawable.ic_broken_image)
                            }
                        } else {
                            // 否则尝试将其解析为URI
                            val uri = android.net.Uri.parse(path)
                            binding.ivPlantHeaderImage.load(uri) {
                                crossfade(true)
                                error(R.drawable.ic_broken_image)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PlantDetail", "Error loading image: ${e.message}")
                        binding.ivPlantHeaderImage.setImageResource(R.drawable.ic_broken_image)
                    }
                } ?: binding.ivPlantHeaderImage.setImageResource(R.drawable.ic_plant_placeholder)
            }
        }

        viewModel.timeline.observe(this) { timelineItems ->
            val adapter = TimelineAdapter(
                onEditLog = { log -> showEditLogDialog(log) },
                onDeleteLog = { log -> showDeleteLogConfirmation(log) },
                onDeleteDiagnosis = { diagnosis -> showDeleteDiagnosisConfirmation(diagnosis) }
            )
            binding.rvLogs.adapter = adapter
            timelineItems?.let { 
                adapter.submitList(it)
                
                if (it.isEmpty()) {
                    binding.tvNoTimelineItems.visibility = View.VISIBLE
                } else {
                    binding.tvNoTimelineItems.visibility = View.GONE
                }
            }
        }
    }

    private fun showAddLogDialog() {
        val dialogBinding = DialogAddLogBinding.inflate(layoutInflater)
        var selectedImageUri: Uri? = null

        val activityTypes = resources.getStringArray(R.array.activity_type_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerActivityType.adapter = adapter
        dialogBinding.spinnerActivityType.dropDownWidth = resources.displayMetrics.widthPixels / 2
        dialogBinding.spinnerActivityType.dropDownVerticalOffset = 60

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
                    val spinnerPosition = dialogBinding.spinnerActivityType.selectedItemPosition
                    val activityType = when(spinnerPosition) {
                        0 -> ActivityType.WATERING    // 浇水
                        1 -> ActivityType.FERTILIZING // 施肥
                        2 -> ActivityType.PRUNING     // 修剪
                        3 -> ActivityType.TREATMENT   // 治疗
                        4 -> ActivityType.NOTE        // 笔记
                        else -> ActivityType.NOTE     // 默认
                    }
                    
                    // 持久化保存图片URI
                    val permanentUri = ImageUtils.getPermamentUri(this, selectedImageUri)
                    
                    val log = GardenLog(
                        plantId = plantId,
                        activityType = activityType,
                        date = System.currentTimeMillis(),
                        description = description,
                        imagePath = permanentUri?.toString()
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

    private fun showEditLogDialog(log: GardenLog) {
        val dialogBinding = DialogEditLogBinding.inflate(layoutInflater)
        var selectedImageUri: Uri? = log.imagePath?.let { Uri.parse(it) }

        val activityTypes = resources.getStringArray(R.array.activity_type_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerActivityType.adapter = adapter
        
        // 设置当前值
        dialogBinding.etLogDescription.setText(log.description)
        val position = when (log.activityType) {
            ActivityType.WATERING -> 0
            ActivityType.FERTILIZING -> 1
            ActivityType.PRUNING -> 2
            ActivityType.TREATMENT -> 3
            ActivityType.NOTE -> 4
            else -> 4 // 默认笔记
        }
        dialogBinding.spinnerActivityType.setSelection(position)
        
        // 加载图片预览（如果有）
        log.imagePath?.let { path ->
            dialogBinding.ivLogImagePreview.visibility = View.VISIBLE
            dialogBinding.ivLogImagePreview.load(Uri.parse(path))
        }

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
            .setTitle("编辑记录")
            .setPositiveButton("保存") { _, _ ->
                val description = dialogBinding.etLogDescription.text.toString().trim()
                if (description.isNotEmpty()) {
                    val spinnerPosition = dialogBinding.spinnerActivityType.selectedItemPosition
                    val activityType = when(spinnerPosition) {
                        0 -> ActivityType.WATERING    // 浇水
                        1 -> ActivityType.FERTILIZING // 施肥
                        2 -> ActivityType.PRUNING     // 修剪
                        3 -> ActivityType.TREATMENT   // 治疗
                        4 -> ActivityType.NOTE        // 笔记
                        else -> ActivityType.NOTE     // 默认
                    }
                    
                    // 持久化保存图片URI
                    val permanentUri = ImageUtils.getPermamentUri(this, selectedImageUri)
                    
                    val updatedLog = log.copy(
                        activityType = activityType,
                        description = description,
                        imagePath = permanentUri?.toString()
                    )
                    viewModel.updateLog(updatedLog)
                }
            }
            .setNegativeButton("取消") { dialog, _ ->
                logImageDialogCallback = null // Clean up
                dialog.cancel()
            }
            .show()
    }
    
    private fun showDeleteLogConfirmation(log: GardenLog) {
        AlertDialog.Builder(this)
            .setTitle("删除记录")
            .setMessage("确定要删除这条记录吗？此操作不可撤销。")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteLog(log)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showDeleteDiagnosisConfirmation(diagnosis: TimelineItem.Diagnosis) {
        AlertDialog.Builder(this)
            .setTitle("删除诊断记录")
            .setMessage("确定要删除这条诊断记录吗？此操作不可撤销，同时会删除对应的诊断历史。")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteDiagnosis(diagnosis)
            }
            .setNegativeButton("取消", null)
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