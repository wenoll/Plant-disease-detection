package com.lazarus.aippa_theplantdoctorbeta

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.lazarus.aippa_theplantdoctorbeta.databinding.ListItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onDeleteClick: (PredictionHistory) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var historyList = emptyList<PredictionHistory>()

    inner class HistoryViewHolder(private val binding: ListItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: PredictionHistory) {
            binding.historyDiseaseNameTextView.text = currentItem.diseaseName
            val confidencePercent = "%.2f".format(currentItem.confidence * 100)
            binding.historyConfidenceTextView.text = itemView.context.getString(R.string.prediction_conf) + " $confidencePercent %"
            
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            binding.historyTimestampTextView.text = sdf.format(Date(currentItem.timestamp))

            binding.historyImageView.load(currentItem.imagePath) {
                crossfade(true)
                placeholder(R.drawable.ic_plant_placeholder)
                error(R.drawable.ic_broken_image)
            }

            when (currentItem.feedback) {
                "correct" -> {
                    binding.feedbackIcon.setImageResource(R.drawable.ic_thumb_up)
                    binding.feedbackIcon.visibility = View.VISIBLE
                }
                "incorrect" -> {
                    binding.feedbackIcon.setImageResource(R.drawable.ic_thumb_down)
                    binding.feedbackIcon.visibility = View.VISIBLE
                }
                else -> {
                    binding.feedbackIcon.visibility = View.GONE
                }
            }

            binding.deleteButton.setOnClickListener {
                onDeleteClick(currentItem)
            }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetailsActivity::class.java).apply {
                    putExtra(DetailsActivity.EXTRA_HISTORY_ID, currentItem.id)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ListItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount() = historyList.size

    fun setData(history: List<PredictionHistory>) {
        this.historyList = history
        notifyDataSetChanged()
    }
}
