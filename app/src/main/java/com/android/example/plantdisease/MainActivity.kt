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
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityMainBinding
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mClassifier: Classifier
    private lateinit var mBitmap: Bitmap

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

        resources.assets.open(mSamplePath).use {
            mBitmap = BitmapFactory.decodeStream(it)
            mBitmap = Bitmap.createScaledBitmap(mBitmap, mInputSize, mInputSize, true)
            binding.leafImageView.setImageBitmap(mBitmap)
        }
//        functional buttons
        binding.fabCamera.setOnClickListener{
//            Log.d("\n camera!", "Camera button was clicked!\n")
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(callCameraIntent, mCameraRequestCode)
        }
        binding.fabGallery.setOnClickListener{
//            Log.d("\n clicked!", "Gallery button was clicked!\n")
            val callGalleryIntent = Intent(Intent.ACTION_PICK)
            callGalleryIntent.type = "image/*"
            startActivityForResult(callGalleryIntent, mGalleryRequestCode)
        }
        binding.detectBtn.setOnClickListener{
//            Log.d("\n clicked!", "Detect button was clicked!\n")
            val results = mClassifier.recognizeImage(mBitmap).firstOrNull()
//            predictedTextView.text= "Name: "+results?.title+"\n Confidence: "+confidencePer
//            Log.d("predicted Result", "\n Disease Name: " + results.title + "\n Confidence: " + confidencePer + " %")

//            if prediction result is empty process  could not be processed
            if (results != null) {
                val confidencePer = 100* results?.confidence!!
                val intentDetailsActivity = Intent(this, DetailsActivity::class.java).apply{
                    putExtra("titleN", getString(R.string.predicted_value))
                    putExtra("diseaseName", results.title)
                    putExtra("prediction_confidence", "(${getString(R.string.prediction_conf)} $confidencePer %)")
                //  putExtra("pictureCapture", bitmapData)
                }
                Toast.makeText(this, getString(R.string.predict_btn_val), Toast.LENGTH_SHORT).show()
                startActivity(intentDetailsActivity)
            } else{
                val toast = Toast.makeText(this, getString(R.string.leaf_not_found), Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }

        binding.fabHelp.setOnClickListener(){
            val intent = Intent(this, AboutActivity::class.java).apply {
            }
            startActivity(intent)
        }
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
}