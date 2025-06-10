package com.lazarus.aippa_theplantdoctorbeta

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.lazarus.aippa_theplantdoctorbeta.Plant
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mClassifier: Classifier
    private var mBitmap: Bitmap? = null
    private lateinit var viewModel: GardenViewModel
    private var plantList: List<Plant> = emptyList()

    // Speed Dial FABs
    private var isAllFabsVisible: Boolean = false
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabCamera: FloatingActionButton
    private lateinit var fabGallery: FloatingActionButton
    private lateinit var fabVoice: FloatingActionButton
    private lateinit var fabLibrary: FloatingActionButton
    private lateinit var fabHistory: FloatingActionButton
    private lateinit var fabGarden: FloatingActionButton
    private lateinit var labelCamera: TextView
    private lateinit var labelGallery: TextView
    private lateinit var labelVoice: TextView
    private lateinit var labelLibrary: TextView
    private lateinit var labelHistory: TextView
    private lateinit var labelGarden: TextView

    // Voice Recognition
    private var speechRecognizer: SpeechRecognizer? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
    }

    private val mGalleryRequestCode = 3
    private val mInputSize = 224
    private val mModelPath = "plant_disease_model.tflite"
    private val mLabelPath = "plant_labels.txt"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocale()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize components
        mClassifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)
        viewModel = ViewModelProvider(this).get(GardenViewModel::class.java)

        // Observe the plant list
        viewModel.allPlants.observe(this) { plants ->
            this.plantList = plants ?: emptyList()
        }

        // Setup UI
        initializeUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeUI() {
        setupFabs()
        binding.detectBtn.isEnabled = false
        binding.detectBtn.setOnClickListener {
            mBitmap?.let { bitmap ->
                val results = mClassifier.recognizeImage(bitmap).firstOrNull()
                if (results != null) {
                    binding.predictedTextView.text = " ${results.title}\n Confidence: ${results.confidence}"
                    showLinkPlantDialog(results)
                } else {
                    val toast = Toast.makeText(this, getString(R.string.leaf_not_found), Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
            } ?: run {
                Toast.makeText(this, getString(R.string.no_image_to_predict), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFabs() {
        fabMain = binding.fabMain
        fabCamera = binding.fabCamera
        fabGallery = binding.fabGallery
        fabVoice = binding.fabVoice
        fabLibrary = binding.fabLibrary
        fabHistory = binding.fabHistory
        fabGarden = binding.fabGarden
        labelCamera = binding.labelCamera
        labelGallery = binding.labelGallery
        labelVoice = binding.labelVoice
        labelLibrary = binding.labelLibrary
        labelHistory = binding.labelHistory
        labelGarden = binding.labelGarden

        setFabVisibility(View.GONE)

        fabMain.setOnClickListener {
            isAllFabsVisible = if (!isAllFabsVisible) {
                setFabVisibility(View.VISIBLE)
                fabMain.animate().rotation(135f)
                true
            } else {
                setFabVisibility(View.GONE)
                fabMain.animate().rotation(0f)
                false
            }
        }

        fabCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            }
        }
        fabGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, mGalleryRequestCode)
        }
        fabVoice.setOnClickListener { startSpeechRecognition() }
        fabLibrary.setOnClickListener {
            val intent = Intent(this, LibraryActivity::class.java)
            startActivity(intent)
        }
        fabHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        fabGarden.setOnClickListener {
            val intent = Intent(this, GardenActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setFabVisibility(visibility: Int) {
        if (visibility == View.VISIBLE) {
            fabCamera.show()
            fabGallery.show()
            fabVoice.show()
            fabLibrary.show()
            fabHistory.show()
            fabGarden.show()
        } else {
            fabCamera.hide()
            fabGallery.hide()
            fabVoice.hide()
            fabLibrary.hide()
            fabHistory.hide()
            fabGarden.hide()
        }
        labelCamera.visibility = visibility
        labelGallery.visibility = visibility
        labelVoice.visibility = visibility
        labelLibrary.visibility = visibility
        labelHistory.visibility = visibility
        labelGarden.visibility = visibility
    }

    private fun startSpeechRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1234)
            return
        }

        speechRecognizer?.destroy()
        if (!SpeechRecognizer.isRecognitionAvailable(applicationContext)) {
            Toast.makeText(this, getString(R.string.speech_recognition_not_available), Toast.LENGTH_LONG).show()
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {
                    Toast.makeText(this@MainActivity, getString(R.string.listening), Toast.LENGTH_SHORT).show()
                }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    Log.e("SpeechRecognizer", "Error: $error")
                    Toast.makeText(this@MainActivity, getString(R.string.speech_error), Toast.LENGTH_SHORT).show()
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        val spokenText = matches[0].lowercase(Locale.getDefault())
                        if (spokenText.contains(getString(R.string.keyword_predict).lowercase(Locale.getDefault()))) {
                            binding.detectBtn.performClick()
                        } else if (spokenText.contains(getString(R.string.keyword_take_photo).lowercase(Locale.getDefault()))) {
                            fabCamera.performClick()
                        } else {
                            Toast.makeText(this@MainActivity, spokenText, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.listening))
        speechRecognizer?.startListening(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_language_en -> {
                setLocale("en")
                recreate()
                return true
            }
            R.id.menu_language_zh -> {
                setLocale("zh")
                recreate()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("Language", lang)
        editor.apply()
    }

    private fun loadLocale() {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("Language", "")
        if (language != null && language.isNotEmpty()) {
            setLocale(language)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                mGalleryRequestCode -> {
                    val uri = data?.data
                    try {
                        uri?.let {
                            val `is` = contentResolver.openInputStream(it)
                            mBitmap = BitmapFactory.decodeStream(`is`)
                            mBitmap = mBitmap?.let { bmp -> scaleImage(bmp) }
                            binding.mPhotoImageView.setImageBitmap(mBitmap)
                            binding.detectBtn.isEnabled = true
                            binding.predictedTextView.text = getString(R.string.photo_set)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        mBitmap = scaleImage(it)
                        binding.mPhotoImageView.setImageBitmap(mBitmap)
                        binding.detectBtn.isEnabled = true
                        binding.predictedTextView.text = getString(R.string.photo_set)
                    }
                }
            }
        }
    }

    private fun scaleImage(bitmap: Bitmap): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val scaleWidth = mInputSize.toFloat() / originalWidth
        val scaleHeight = mInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, originalWidth, originalHeight, matrix, true)
    }

    fun saveImageToInternalStorage(bitmap: Bitmap, diseaseName: String): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PlantDoc_${timeStamp}_${diseaseName.replace(" ", "_")}.jpg"
        val directory = getDir("predictions", Context.MODE_PRIVATE)
        val file = File(directory, fileName)

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun savePrediction(results: Classifier.Recognition, plantId: Long?) {
        val imagePath = saveImageToInternalStorage(mBitmap!!, results.title)
        lifecycleScope.launch(Dispatchers.IO) {
            val prediction = PredictionHistory(
                diseaseName = results.title,
                confidence = results.confidence,
                timestamp = System.currentTimeMillis(),
                imagePath = imagePath,
                plantId = plantId
            )
            val newPredictionId = viewModel.insertPrediction(prediction)

            if (plantId != null) {
                val gardenLog = GardenLog(
                    plantId = plantId,
                    activityType = ActivityType.DIAGNOSIS,
                    date = System.currentTimeMillis(),
                    description = "诊断结果: ${results.title}, 置信度: ${String.format("%.1f%%", results.confidence * 100)}",
                    predictionId = newPredictionId
                )
                viewModel.insertGardenLog(gardenLog)
            }
        }
        val toast = Toast.makeText(this, "诊断记录已保存", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 20)
        toast.show()
    }

    private fun showLinkPlantDialog(results: Classifier.Recognition) {
        viewModel.allPlants.observe(this, Observer { plants ->
            if (plants.isNullOrEmpty()) {
                // 如果没有植物，直接保存记录即可
                savePrediction(results, null)
                Toast.makeText(this, "花园中还没有植物，已直接保存记录", Toast.LENGTH_LONG).show()
                return@Observer
            }

            val plantNames = plants.map { it.name }.toTypedArray()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("要将此诊断关联到哪个植物？")
                .setItems(plantNames) { dialog, which ->
                    val selectedPlant = plants[which]
                    savePrediction(results, selectedPlant.id)
                    dialog.dismiss()
                }
                .setNeutralButton("仅保存记录") { dialog, _ ->
                    savePrediction(results, null)
                    dialog.dismiss()
                }
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }

            builder.create().show()
        })
    }
} 