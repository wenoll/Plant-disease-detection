package com.lazarus.aippa_theplantdoctorbeta

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityLibraryBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class LibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibraryBinding
    private lateinit var adapter: LibraryAdapter
    private val diseaseList = ArrayList<Disease>()

    private val jsonFileName: String
        get() {
            val currentLocale = Locale.getDefault().language
            return if (currentLocale == "zh") "disease_description_zh.json" else "disease_description.json"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setSupportActionBar(binding.toolbar) // Temporarily commented out
        // supportActionBar?.setDisplayHomeAsUpEnabled(true) // Temporarily commented out

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        loadDiseases()
        
        adapter = LibraryAdapter(this, diseaseList)
        binding.recyclerView.adapter = adapter
    }

    private fun loadDiseases() {
        try {
            val obj = JSONObject(loadJSONFromAsset())
            val diseaseArray = obj.getJSONArray("disease")
            val rawList = ArrayList<Pair<String, String>>()

            for (i in 0 until diseaseArray.length()) {
                val diseaseDetail = diseaseArray.getJSONObject(i)
                val name = diseaseDetail.getString("name")
                val causativeAgent = diseaseDetail.getString("causative_agent")
                rawList.add(Pair(name, causativeAgent))
            }

            // Group by plant type
            val groupedDiseases = rawList.groupBy {
                it.first.split("___").first()
            }

            var idCounter = 0
            for ((plant, diseases) in groupedDiseases) {
                // Add header
                diseaseList.add(Disease(idCounter++, plant.replace("_", " "), "", Disease.VIEW_TYPE_HEADER))
                // Add items
                for (disease in diseases) {
                    diseaseList.add(Disease(idCounter++, disease.first, disease.second, Disease.VIEW_TYPE_ITEM))
                }
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun loadJSONFromAsset(): String {
        return try {
            val inputStream = assets.open(jsonFileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            val charset: Charset = Charsets.UTF_8
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, charset)
        } catch (ex: IOException) {
            ex.printStackTrace()
            ""
        }
    }

    /* // Temporarily commented out
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_library, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    */
} 