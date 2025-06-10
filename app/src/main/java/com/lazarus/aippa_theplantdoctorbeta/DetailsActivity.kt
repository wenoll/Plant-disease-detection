package com.lazarus.aippa_theplantdoctorbeta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityDetailsBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.util.Locale

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var viewModel: PredictionDetailViewModel
    private var historyId: Long = -1

    companion object {
        const val EXTRA_HISTORY_ID = "extra_history_id"
        const val EXTRA_DISEASE_NAME = "diseaseName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyId = intent.getLongExtra(EXTRA_HISTORY_ID, -1L)
        
        if (historyId != -1L) {
            val factory = PredictionDetailViewModelFactory(application, historyId)
            viewModel = ViewModelProvider(this, factory).get(PredictionDetailViewModel::class.java)
            observeViewModel()
        } else {
            val diseaseName = intent.getStringExtra(EXTRA_DISEASE_NAME)
            if (diseaseName != null) {
                setupRecyclerView(diseaseName, 0f)
            } else {
                finish()
                return
            }
        }
    }

    private fun observeViewModel() {
        viewModel.history.observe(this) { history ->
            history?.let {
                val diseaseName = it.diseaseName
                val confidence = it.confidence
                
                setupRecyclerView(diseaseName, confidence)
            }
        }
    }
    
    private fun setupRecyclerView(diseaseName: String, confidence: Float) {
        val modelList = loadDiseaseDetails(diseaseName)
        val adapter = CustomAdapter(this, modelList, diseaseName, "${(confidence * 100).toInt()}%", historyId)
        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@DetailsActivity)
        }
    }

    private fun loadDiseaseDetails(diseaseName: String): ArrayList<Model> {
        val modelList = ArrayList<Model>()
        try {
            val obj = JSONObject(loadJSONFromAsset())
            val diseaseArray = obj.getJSONArray("disease")
            var found = false
            for (i in 0 until diseaseArray.length()){
                val diseaseDetail = diseaseArray.getJSONObject(i)
                if(diseaseDetail.getString("name").trim().equals(diseaseName.trim(), ignoreCase = true)){
                    val symptomsDetails = diseaseDetail.getJSONObject("symptoms")
                    val commentsDetails = diseaseDetail.getJSONObject("comments")
                    val managementDetails = diseaseDetail.getJSONObject("management")

                    val m = Model(
                        diseaseDetail.getString("name"),
                        diseaseDetail.getString("causative_agent"),
                        diseaseDetail.getString("cause"),
                        symptomsDetails.optString("1", ""),
                        symptomsDetails.optString("2", ""),
                        symptomsDetails.optString("3", ""),
                        symptomsDetails.optString("4", ""),
                        symptomsDetails.optString("5", ""),
                        commentsDetails.optString("1", ""),
                        commentsDetails.optString("2", ""),
                        managementDetails.optString("1", ""),
                        managementDetails.optString("2", ""),
                        managementDetails.optString("3", ""),
                        managementDetails.optString("4", "")
                    )
                    modelList.add(m)
                    found = true
                    break
                }
            }
            if (!found) {
                 val m = Model(
                    diseaseName,
                    "", "", if (diseaseName.equals("Healthy", ignoreCase = true)) getString(R.string.healthy_plant_description) else "", "", "", "", "", "", "", "", "", "", ""
                )
                modelList.add(m)
            }

        } catch (e: JSONException){
            e.printStackTrace()
        }
        return modelList
    }

    private fun loadJSONFromAsset(): String {
        val currentLocale = Locale.getDefault().language
        val jsonFileName = if (currentLocale == "zh") "disease_description_zh.json" else "disease_description.json"
        
        val json: String?
        try {
            val inputStream = assets.open(jsonFileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            val charset: Charset = Charsets.UTF_8
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, charset)
        } catch (ex: IOException){
            ex.printStackTrace()
            return ""
        }
        return json
    }
}