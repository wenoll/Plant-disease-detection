package com.lazarus.aippa_theplantdoctorbeta

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.lazarus.aippa_theplantdoctorbeta.databinding.ItemLogEntryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogAdapter(private var logs: List<GardenLog>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount() = logs.size

    fun updateData(newLogs: List<GardenLog>) {
        logs = newLogs
        notifyDataSetChanged()
    }

    class LogViewHolder(private val binding: ItemLogEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy, HH:mm", Locale.getDefault())
        private var isExpanded = false

        fun bind(log: GardenLog) {
            binding.tvLogDescription.text = log.description
            binding.tvLogDate.text = dateFormatter.format(Date(log.date))

            // Default state for description text
            binding.tvLogDescription.maxLines = 2
            isExpanded = false
            binding.tvLogDescription.setOnClickListener {
                isExpanded = !isExpanded
                binding.tvLogDescription.maxLines = if (isExpanded) Int.MAX_VALUE else 2
            }

            if (log.activityType == ActivityType.DIAGNOSIS) {
                // Load image using Coil if path is not null
                log.imagePath?.let {
                    binding.ivLogIcon.load(it) {
                        crossfade(true)
                        placeholder(R.drawable.ic_plant_placeholder) // Optional placeholder
                        error(R.drawable.ic_broken_image) // Optional error image
                    }
                } ?: binding.ivLogIcon.setImageResource(R.drawable.ic_diagnosis) // Fallback icon

                // Set click listener to open prediction details
                itemView.setOnClickListener {
                    log.predictionId?.let { id ->
                        val context = itemView.context
                        val intent = Intent(context, DetailsActivity::class.java).apply {
                            putExtra(DetailsActivity.EXTRA_HISTORY_ID, id)
                        }
                        context.startActivity(intent)
                    }
                }
            } else {
                // Handle other log types
                val iconRes = when (log.activityType) {
                    ActivityType.WATERING -> R.drawable.ic_watering
                    ActivityType.FERTILIZING -> R.drawable.ic_fertilizing
                    ActivityType.PRUNING -> R.drawable.ic_pruning
                    ActivityType.TREATMENT -> R.drawable.ic_treatment
                    ActivityType.NOTE -> R.drawable.ic_note
                    else -> R.drawable.ic_note // Should not happen for DIAGNOSIS here
                }
                binding.ivLogIcon.setImageResource(iconRes)
                itemView.setOnClickListener(null) // Remove click listener for non-diagnosis logs
            }
        }
    }
} 