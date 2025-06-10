package com.lazarus.aippa_theplantdoctorbeta

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.lazarus.aippa_theplantdoctorbeta.databinding.ActivityReportBinding

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private lateinit var viewModel: ReportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ReportViewModel::class.java)

        setupToolbar()
        setupChart()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.report_title)
    }

    private fun setupChart() {
        binding.chartDiseaseFrequency.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)
            animateY(1500)
            legend.isEnabled = false
            
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e != null) {
                        val diseaseName = viewModel.diseaseLabels.value?.getOrNull(e.x.toInt())
                        if (diseaseName != null) {
                            val intent = Intent(this@ReportActivity, DetailsActivity::class.java).apply {
                                putExtra(DetailsActivity.EXTRA_DISEASE_NAME, diseaseName)
                            }
                            startActivity(intent)
                        }
                    }
                }

                override fun onNothingSelected() {
                    // Do nothing
                }
            })
        }
    }

    private fun observeViewModel() {
        viewModel.barChartData.observe(this) { barData ->
            binding.chartDiseaseFrequency.data = barData
            binding.chartDiseaseFrequency.invalidate()
        }

        viewModel.diseaseLabels.observe(this) { labels ->
            binding.chartDiseaseFrequency.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            binding.chartDiseaseFrequency.invalidate()
        }
        
        viewModel.reportSummary.observe(this) { summary ->
            binding.tvReportSummary.text = summary
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 