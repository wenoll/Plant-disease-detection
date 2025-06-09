package com.lazarus.aippa_theplantdoctorbeta

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class LibraryAdapter(
    private val context: Context,
    private var diseaseList: ArrayList<Disease>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var diseaseListFiltered: ArrayList<Disease> = diseaseList

    override fun getItemViewType(position: Int): Int {
        return diseaseListFiltered[position].viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == Disease.VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_disease, parent, false)
            DiseaseViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val disease = diseaseListFiltered[position]
        if (holder is HeaderViewHolder) {
            holder.headerTextView.text = disease.name
        } else if (holder is DiseaseViewHolder) {
            holder.diseaseNameTextView.text = disease.name.split("___").last().replace("_", " ")
            holder.causativeAgentTextView.text = disease.causativeAgent
            holder.itemView.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java).apply {
                    putExtra("diseaseName", disease.name)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return diseaseListFiltered.size
    }

    inner class DiseaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val diseaseNameTextView: TextView = itemView.findViewById(R.id.diseaseNameTextView)
        val causativeAgentTextView: TextView = itemView.findViewById(R.id.causativeAgentTextView)
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.headerTextView)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString()?.lowercase(Locale.ROOT) ?: ""
                diseaseListFiltered = if (charString.isEmpty()) {
                    diseaseList
                } else {
                    val filteredList = ArrayList<Disease>()
                    var currentHeader = ""
                    for (disease in diseaseList) {
                        if (disease.viewType == Disease.VIEW_TYPE_HEADER) {
                            currentHeader = disease.name
                        } else {
                            if (disease.name.lowercase(Locale.ROOT).contains(charString) || disease.causativeAgent.lowercase(Locale.ROOT).contains(charString)) {
                                val headerInList = filteredList.any { it.name == currentHeader && it.viewType == Disease.VIEW_TYPE_HEADER }
                                if (!headerInList) {
                                    filteredList.add(Disease(-1, currentHeader, "", Disease.VIEW_TYPE_HEADER))
                                }
                                filteredList.add(disease)
                            }
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = diseaseListFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                diseaseListFiltered = results?.values as ArrayList<Disease>
                notifyDataSetChanged()
            }
        }
    }
} 