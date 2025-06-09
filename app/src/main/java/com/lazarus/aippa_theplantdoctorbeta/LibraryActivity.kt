package com.lazarus.aippa_theplantdoctorbeta

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityLibraryBinding
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.util.Locale

class LibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibraryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val diseaseNames = getDiseaseNamesFromJSON()

        binding.libraryRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = LibraryAdapter(this, diseaseNames) { diseaseName ->
            val intent = Intent(this, DetailsActivity::class.java).apply {
                putExtra("diseaseName", diseaseName)
            }
            startActivity(intent)
        }
        binding.libraryRecyclerView.adapter = adapter
    }

    private fun getDiseaseNamesFromJSON(): List<String> {
        val diseaseNames = mutableListOf<String>()
        try {
            val jsonString = loadJSONFromAsset()
            if (jsonString.isNotBlank()) {
                val obj = JSONObject(jsonString)
                val diseaseArray = obj.getJSONArray("disease")
                for (i in 0 until diseaseArray.length()) {
                    val diseaseDetail = diseaseArray.getJSONObject(i)
                    diseaseNames.add(diseaseDetail.getString("name"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return diseaseNames.sorted()
    }

    private fun loadJSONFromAsset(): String {
        val jsonFileName = if (Locale.getDefault().language == "zh")
            "disease_description_zh.json"
        else
            "disease_description.json"

        return try {
            val inputStream = assets.open(jsonFileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            ""
        }
    }
} 