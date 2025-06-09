package com.lazarus.aippa_theplantdoctorbeta

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LibraryAdapter(
    private val context: Context,
    private val diseaseList: List<String>,
    private val onItemClicked: (String) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_disease, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val diseaseName = diseaseList[position]
        holder.textView.text = diseaseName
        holder.itemView.setOnClickListener {
            onItemClicked(diseaseName)
        }
    }

    override fun getItemCount(): Int {
        return diseaseList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.diseaseNameTextView)
    }
} 