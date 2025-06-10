package com.lazarus.aippa_theplantdoctorbeta

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
        notifyDataSetChanged() // For a real app, use DiffUtil
    }

    class LogViewHolder(private val binding: ItemLogEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        fun bind(log: GardenLog) {
            binding.tvLogDescription.text = log.description
            binding.tvLogDate.text = dateFormatter.format(Date(log.date))

            val iconRes = when (log.activityType) {
                ActivityType.DIAGNOSIS -> R.drawable.ic_diagnosis
                ActivityType.WATERING -> R.drawable.ic_watering
                ActivityType.FERTILIZING -> R.drawable.ic_fertilizing
                ActivityType.PRUNING -> R.drawable.ic_pruning
                ActivityType.TREATMENT -> R.drawable.ic_treatment
                ActivityType.NOTE -> R.drawable.ic_note
            }
            binding.ivLogIcon.setImageResource(iconRes)
        }
    }
} 