package com.lazarus.aippa_theplantdoctorbeta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityDetailsBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.util.Locale

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    companion object {
        const val EXTRA_DISEASE_NAME = "diseaseName"
    }

//    JSON
    private val jsonFileName: String
        get() {
            // 根据当前语言选择不同的JSON文件
            val currentLocale = Locale.getDefault().language
            return if (currentLocale == "zh") "disease_description_zh.json" else "disease_description.json"
        }
    
    var confPerStr: String = String()
    var diseaseNameStr: String = String()
    var diseaseNameJSON: ArrayList<String> = ArrayList()
    var causativeAgentJSON: ArrayList<String> = ArrayList()
    var causeJSON: ArrayList<String> = ArrayList()
    var symptoms1JSON: ArrayList<String> = ArrayList()
    var symptoms2JSON: ArrayList<String> = ArrayList()
    var symptoms3JSON: ArrayList<String> = ArrayList()
    var symptoms4JSON: ArrayList<String> = ArrayList()
    var symptoms5JSON: ArrayList<String> = ArrayList()

    var comments1JSON: ArrayList<String> = ArrayList()
    var comments2JSON: ArrayList<String> = ArrayList()

    var management1JSON: ArrayList<String> = ArrayList()
    var management2JSON: ArrayList<String> = ArrayList()
    var management3JSON: ArrayList<String> = ArrayList()
    var management4JSON: ArrayList<String> = ArrayList()

    //    ML
    private var title = "df"
    private var diseaseName = "df"
    private var predictionConfidence = "10 %"
//    private var pictureCapture = byteArrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        title = bundle?.getString("titleN", "Title") ?: ""
        diseaseName = bundle?.getString(EXTRA_DISEASE_NAME, "Disease Name") ?: ""
        predictionConfidence = bundle?.getString("prediction_confidence") ?: ""
//        pictureCapture = bundle?.getByteArray("pictureCapture")!!

        confPerStr = predictionConfidence
        diseaseNameStr = diseaseName

        val historyId = intent.getLongExtra("historyId", -1L)

        val modelList = ArrayList<Model>()
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        try {
            val obj = JSONObject(loadJSONFromAsset())
            val diseaseArray = obj.getJSONArray("disease")
            var found = false
            for (i in 0 until diseaseArray.length()){
                val diseaseDetail = diseaseArray.getJSONObject(i)
                if(diseaseDetail.getString("name").trim().equals(diseaseName?.trim(), ignoreCase = true)){
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
                    diseaseName ?: getString(R.string.unknown_disease),
                    "", "", getString(R.string.healthy_plant_description), "", "", "", "", "", "", "", "", "", ""
                )
                modelList.add(m)
            }

        } catch (e: JSONException){
            e.printStackTrace()
        }

        val adapter = CustomAdapter(
            this,
            modelList,
            diseaseName ?: "Unknown",
            predictionConfidence,
            historyId
        )
        recyclerView.adapter = adapter
    }

    private fun loadJSONFromAsset(): String{
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

    fun goHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
        }
        startActivity(intent)
    }
} 