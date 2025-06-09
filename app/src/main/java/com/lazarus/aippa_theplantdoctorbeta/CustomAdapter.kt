package com.lazarus.aippa_theplantdoctorbeta

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CustomAdapter(
    private val context: Context,
    private val modelList: ArrayList<Model>,
    private val diseaseNameString: String,
    private val confPerString: String?,
    private val historyId: Long
) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = modelList[position]

        holder.diseaseNameTV.text = diseaseNameString.toUpperCase()
        if (!confPerString.isNullOrEmpty()) {
            holder.confPer.text = confPerString
            holder.confPer.visibility = View.VISIBLE
        } else {
            holder.confPer.visibility = View.GONE
        }

        fun setTextOrHide(textView: TextView, text: String?) {
            if (!text.isNullOrEmpty()) {
                textView.text = text
                textView.visibility = View.VISIBLE
            } else {
                textView.visibility = View.GONE
            }
        }

        setTextOrHide(holder.causativeAgentTV, model.causativeAgent)
        setTextOrHide(holder.causeTV, model.cause)
        setTextOrHide(holder.symptoms1TV, model.symptom1)
        setTextOrHide(holder.symptoms2TV, model.symptom2)
        setTextOrHide(holder.symptoms3TV, model.symptom3)
        setTextOrHide(holder.symptoms4TV, model.symptom4)
        setTextOrHide(holder.symptoms5TV, model.symptom5)
        setTextOrHide(holder.comments1TV, model.comment1)
        setTextOrHide(holder.comments2TV, model.comment2)
        setTextOrHide(holder.management1TV, model.management1)
        setTextOrHide(holder.management2TV, model.management2)
        setTextOrHide(holder.management3TV, model.management3)
        setTextOrHide(holder.management4TV, model.management4)

        if (historyId != -1L) {
            holder.feedbackSection.visibility = View.VISIBLE
            
            val feedbackHandler = { feedback: String ->
                GlobalScope.launch(Dispatchers.IO) {
                    val historyDao = AppDatabase.getDatabase(context).predictionHistoryDao()
                    historyDao.updateFeedback(historyId, feedback)
                }
                holder.buttonCorrect.isEnabled = false
                holder.buttonIncorrect.isEnabled = false
                holder.buttonCorrect.alpha = 0.5f
                holder.buttonIncorrect.alpha = 0.5f
                Toast.makeText(context, context.getString(R.string.feedback_thanks), Toast.LENGTH_SHORT).show()
            }

            holder.buttonCorrect.setOnClickListener {
                feedbackHandler("correct")
            }
            holder.buttonIncorrect.setOnClickListener {
                feedbackHandler("incorrect")
            }
        } else {
            holder.feedbackSection.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val diseaseNameTV: TextView = itemView.findViewById(R.id.tvDiseaseName)
        val confPer: TextView = itemView.findViewById(R.id.tvConfidencePer)
        val causativeAgentTV: TextView = itemView.findViewById(R.id.tvCausativeAgent)
        val causeTV: TextView = itemView.findViewById(R.id.tvCause)
        val symptoms1TV: TextView = itemView.findViewById(R.id.tvSymptoms1)
        val symptoms2TV: TextView = itemView.findViewById(R.id.tvSymptoms2)
        val symptoms3TV: TextView = itemView.findViewById(R.id.tvSymptoms3)
        val symptoms4TV: TextView = itemView.findViewById(R.id.tvSymptoms4)
        val symptoms5TV: TextView = itemView.findViewById(R.id.tvSymptoms5)
        val comments1TV: TextView = itemView.findViewById(R.id.tvComments1)
        val comments2TV: TextView = itemView.findViewById(R.id.tvComments2)
        val management1TV: TextView = itemView.findViewById(R.id.tvManagement1)
        val management2TV: TextView = itemView.findViewById(R.id.tvManagement2)
        val management3TV: TextView = itemView.findViewById(R.id.tvManagement3)
        val management4TV: TextView = itemView.findViewById(R.id.tvManagement4)
        val feedbackSection: LinearLayout = itemView.findViewById(R.id.feedback_section)
        val buttonCorrect: Button = itemView.findViewById(R.id.button_correct)
        val buttonIncorrect: Button = itemView.findViewById(R.id.button_incorrect)
    }
} 