package com.lazarus.aippa_theplantdoctorbeta

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.text.TextUtils
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.lazarus.aippa_theplantdoctorbeta.databinding.ItemTimelineDiagnosisBinding
import com.lazarus.aippa_theplantdoctorbeta.databinding.ItemTimelineLogBinding
import java.text.SimpleDateFormat
import java.util.*

class TimelineAdapter(
    private val onEditLog: (GardenLog) -> Unit,
    private val onDeleteLog: (GardenLog) -> Unit,
    private val onDeleteDiagnosis: (TimelineItem.Diagnosis) -> Unit
) : ListAdapter<TimelineItem, RecyclerView.ViewHolder>(TimelineDiffCallback()) {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    companion object {
        private const val VIEW_TYPE_LOG = 1
        private const val VIEW_TYPE_DIAGNOSIS = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimelineItem.Log -> VIEW_TYPE_LOG
            is TimelineItem.Diagnosis -> VIEW_TYPE_DIAGNOSIS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_LOG -> {
                val binding = ItemTimelineLogBinding.inflate(inflater, parent, false)
                LogViewHolder(binding)
            }
            VIEW_TYPE_DIAGNOSIS -> {
                val binding = ItemTimelineDiagnosisBinding.inflate(inflater, parent, false)
                DiagnosisViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TimelineItem.Log -> (holder as LogViewHolder).bind(item)
            is TimelineItem.Diagnosis -> (holder as DiagnosisViewHolder).bind(item)
        }
    }

    inner class LogViewHolder(private val binding: ItemTimelineLogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimelineItem.Log) {
            val log = item.gardenLog
            
            // 设置活动类型文本
            val activityTypeText = when (log.activityType) {
                ActivityType.DIAGNOSIS -> "诊断"
                ActivityType.WATERING -> "浇水"
                ActivityType.FERTILIZING -> "施肥"
                ActivityType.PRUNING -> "修剪"
                ActivityType.TREATMENT -> "治疗"
                ActivityType.NOTE -> "笔记"
            }
            binding.tvActivityType.text = activityTypeText
            
            // 设置描述文本
            binding.tvLogDescription.text = log.description
            
            // 处理描述文本的折叠/展开
            if (log.description.length > 100) {  // 文本较长时才显示展开/折叠按钮
                binding.tvExpandCollapse.visibility = View.VISIBLE
                
                var isExpanded = false
                binding.tvExpandCollapse.setOnClickListener {
                    isExpanded = !isExpanded
                    if (isExpanded) {
                        binding.tvLogDescription.maxLines = Integer.MAX_VALUE
                        binding.tvLogDescription.ellipsize = null
                        binding.tvExpandCollapse.text = "收起"
                    } else {
                        binding.tvLogDescription.maxLines = 2
                        binding.tvLogDescription.ellipsize = TextUtils.TruncateAt.END
                        binding.tvExpandCollapse.text = "展开"
                    }
                }
            } else {
                binding.tvExpandCollapse.visibility = View.GONE
            }
            
            // 加载图片（如果有）
            log.imagePath?.let { path ->
                binding.ivLogImage.visibility = View.VISIBLE
                binding.ivLogImage.load(path) {
                    crossfade(true)
                    error(R.drawable.ic_broken_image)
                }
            } ?: run {
                binding.ivLogImage.visibility = View.GONE
            }
            
            binding.tvLogDate.text = dateFormatter.format(Date(item.date))
            
            val iconRes = when (log.activityType) {
                ActivityType.WATERING -> R.drawable.ic_water_drop
                ActivityType.FERTILIZING -> R.drawable.ic_fertilizer
                ActivityType.PRUNING -> R.drawable.ic_content_cut
                ActivityType.TREATMENT -> R.drawable.ic_treatment
                else -> R.drawable.ic_notes
            }
            binding.timelineIcon.setImageResource(iconRes)
            
            // 设置更多选项按钮点击事件
            binding.btnMoreOptions.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.inflate(R.menu.menu_timeline_item)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            onEditLog(log)
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteLog(log)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
            
            // 添加点击事件，点击卡片时展开/折叠详细内容
            binding.logCard.setOnClickListener {
                if (binding.tvExpandCollapse.visibility == View.VISIBLE) {
                    binding.tvExpandCollapse.performClick()
                }
            }
        }
    }

    inner class DiagnosisViewHolder(private val binding: ItemTimelineDiagnosisBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimelineItem.Diagnosis) {
            val prediction = item.predictionHistory
            binding.tvDiseaseName.text = prediction.diseaseName
            binding.tvConfidence.text = "Confidence: ${String.format("%.1f%%", prediction.confidence * 100)}"
            binding.tvLogDescriptionDiagnosis.text = item.gardenLog.description
            binding.tvDiagnosisDate.text = dateFormatter.format(Date(item.date))

            prediction.imagePath?.let {
                binding.ivDiagnosisImage.load(it) {
                    crossfade(true)
                    error(R.drawable.ic_broken_image)
                }
            }
            
            // 设置更多选项按钮点击事件
            binding.btnMoreOptionsDiagnosis.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.inflate(R.menu.menu_timeline_item)
                // 诊断记录不能编辑，只能删除
                popup.menu.findItem(R.id.action_edit).isVisible = false
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_delete -> {
                            onDeleteDiagnosis(item)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
            
            // 添加点击事件，跳转到预测历史详情页面
            binding.diagnosisCard.setOnClickListener {
                val context = it.context
                val intent = Intent(context, DetailsActivity::class.java).apply {
                    putExtra(DetailsActivity.EXTRA_HISTORY_ID, prediction.id)
                }
                context.startActivity(intent)
            }
        }
    }

    class TimelineDiffCallback : DiffUtil.ItemCallback<TimelineItem>() {
        override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem.id == newItem.id && oldItem.javaClass == newItem.javaClass
        }

        override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem == newItem
        }
    }
}
