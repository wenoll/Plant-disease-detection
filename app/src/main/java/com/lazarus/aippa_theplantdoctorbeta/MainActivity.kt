package com.lazarus.aippa_theplantdoctorbeta

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityMainBinding
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mClassifier: Classifier
    private lateinit var mBitmap: Bitmap

    // Voice Recognition
    private val RECORD_AUDIO_PERMISSION_CODE = 101
    private lateinit var speechRecognizer: SpeechRecognizer

    private val mCameraRequestCode = 0
    private val mGalleryRequestCode = 2

    private val mInputSize = 224
    private val mModelPath = "plant_disease_model.tflite"
    private val mLabelPath = "plant_labels.txt"
    private val mSamplePath = "test_leaf.jpg"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 加载保存的语言设置
        loadLocale()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        mClassifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)
        setupSpeechRecognizer()

        resources.assets.open(mSamplePath).use {
            mBitmap = BitmapFactory.decodeStream(it)
            mBitmap = Bitmap.createScaledBitmap(mBitmap, mInputSize, mInputSize, true)
            binding.leafImageView.setImageBitmap(mBitmap)
        }
//        functional buttons
        binding.fabVoice.setOnClickListener {
            checkAndRequestPermissions()
        }
        binding.fabLibrary.setOnClickListener {
            val intent = Intent(this, LibraryActivity::class.java)
            startActivity(intent)
        }
        binding.fabHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        binding.detectBtn.setOnClickListener{
            val results = mClassifier.recognizeImage(mBitmap).firstOrNull()
            if (results != null) {
                // 保存图片并获取路径
                val imagePath = saveImageToInternalStorage(mBitmap)
                
                // 启动一个协程来执行数据库插入
                GlobalScope.launch(Dispatchers.IO) {
                    val historyDao = AppDatabase.getDatabase(applicationContext).predictionHistoryDao()
                    val newHistoryRecord = PredictionHistory(
                        diseaseName = results.title,
                        confidence = results.confidence,
                        timestamp = System.currentTimeMillis(),
                        imagePath = imagePath
                    )
                    val historyId = historyDao.insert(newHistoryRecord)

                    // 在主线程启动Activity
                    launch(Dispatchers.Main) {
                        val confidencePer = 100 * results.confidence
                        val intentDetailsActivity = Intent(this@MainActivity, DetailsActivity::class.java).apply {
                            putExtra("historyId", historyId) // 传递历史记录ID
                            putExtra("diseaseName", results.title)
                            putExtra("prediction_confidence", "(${getString(R.string.prediction_conf)} $confidencePer %)")
                        }
                        Toast.makeText(this@MainActivity, getString(R.string.predict_btn_val), Toast.LENGTH_SHORT).show()
                        startActivity(intentDetailsActivity)
                    }
                }
            } else {
                val toast = Toast.makeText(this, getString(R.string.leaf_not_found), Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
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

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val spokenText = matches[0].lowercase(Locale.getDefault())
                    Log.d("SpokenText", spokenText)
                    if (spokenText.contains(getString(R.string.keyword_take_photo))) {
                        openCamera()
                    } else if (spokenText.contains(getString(R.string.keyword_recognize))) {
                        openGallery()
                    } else if (spokenText.contains(getString(R.string.keyword_predict))) {
                        binding.detectBtn.performClick()
                    } else {
                        Toast.makeText(this@MainActivity, getString(R.string.command_not_recognized, spokenText), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
        } else {
            startSpeechToText()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startSpeechToText()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.listening))
        speechRecognizer.startListening(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    // 创建选项菜单，添加语言切换选项
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // 处理菜单项选择
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_language_en -> {
                setLocale("en")
                recreate() // 重新创建活动以应用新语言
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

    // 设置应用程序语言
    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)

        // 保存语言设置
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("Language", lang)
        editor.apply()
    }

    // 加载保存的语言设置
    private fun loadLocale() {
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("Language", "")
        if (language != null && language.isNotEmpty()) {
            setLocale(language)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == mCameraRequestCode){
            // Consider the case of the canceled camera
            if(resultCode == Activity.RESULT_OK && data != null) {
                mBitmap = data.extras!!.get("data") as Bitmap
                mBitmap = scaleImage(mBitmap)
                val toast = Toast.makeText(
                    this,
                    getString(R.string.image_crop_info, mBitmap.width, mBitmap.height),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.BOTTOM, 0, 20)
                toast.show()
                binding.leafImageView.setImageBitmap(mBitmap)
                binding.predictedTextView.text= getString(R.string.photo_set)
            } else {
                Toast.makeText(this, getString(R.string.camera_cancel), Toast.LENGTH_LONG).show()
            }
        } else if(requestCode == mGalleryRequestCode) {
            if (data != null) {
                val uri = data.data

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                println("Success!!!")
                mBitmap = scaleImage(mBitmap)
                binding.leafImageView.setImageBitmap(mBitmap)
            }
        } else {
            Toast.makeText(this, getString(R.string.unrecognized_request), Toast.LENGTH_LONG).show()
        }
    }

    fun scaleImage(bitmap: Bitmap?): Bitmap {
        val orignalWidth = bitmap!!.width
        val originalHeight = bitmap.height
        val scaleWidth = mInputSize.toFloat() / orignalWidth
        val scaleHeight = mInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, orignalWidth, originalHeight, matrix, true)
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PlantDoc_$timeStamp.jpg"
        val file = File(filesDir, fileName)
        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    private fun openCamera() {
        val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(callCameraIntent, mCameraRequestCode)
    }

    private fun openGallery() {
        val callGalleryIntent = Intent(Intent.ACTION_PICK)
        callGalleryIntent.type = "image/*"
        startActivityForResult(callGalleryIntent, mGalleryRequestCode)
    }
} 