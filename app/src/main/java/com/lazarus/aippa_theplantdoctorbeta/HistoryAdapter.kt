package com.lazarus.aippa_theplantdoctorbeta

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var historyList = emptyList<PredictionHistory>()

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.historyImageView)
        val diseaseNameView: TextView = itemView.findViewById(R.id.historyDiseaseNameTextView)
        val confidenceView: TextView = itemView.findViewById(R.id.historyConfidenceTextView)
        val timestampView: TextView = itemView.findViewById(R.id.historyTimestampTextView)
        val feedbackIcon: ImageView = itemView.findViewById(R.id.feedbackIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_history, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = historyList[position]

        holder.diseaseNameView.text = currentItem.diseaseName
        val confidencePercent = "%.2f".format(currentItem.confidence * 100)
        holder.confidenceView.text = holder.itemView.context.getString(R.string.prediction_conf) + " $confidencePercent %"
        
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.timestampView.text = sdf.format(Date(currentItem.timestamp))

        val imgFile = File(currentItem.imagePath)
        if (imgFile.exists()) {
            holder.imageView.setImageURI(Uri.fromFile(imgFile))
        }

        when (currentItem.feedback) {
            "correct" -> {
                holder.feedbackIcon.setImageResource(R.drawable.ic_thumb_up)
                holder.feedbackIcon.visibility = View.VISIBLE
            }
            "incorrect" -> {
                holder.feedbackIcon.setImageResource(R.drawable.ic_thumb_down)
                holder.feedbackIcon.visibility = View.VISIBLE
            }
            else -> {
                holder.feedbackIcon.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailsActivity::class.java).apply {
                putExtra("historyId", currentItem.id)
                putExtra("diseaseName", currentItem.diseaseName)
                putExtra("prediction_confidence", "${context.getString(R.string.prediction_conf)} $confidencePercent %")
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = historyList.size

    fun setData(history: List<PredictionHistory>) {
        this.historyList = history
        notifyDataSetChanged()
    }
}
