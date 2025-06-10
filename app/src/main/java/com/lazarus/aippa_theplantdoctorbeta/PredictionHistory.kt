package com.lazarus.aippa_theplantdoctorbeta

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "history_table",
    foreignKeys = [
        ForeignKey(
            entity = Plant::class,
            parentColumns = ["id"],
            childColumns = ["plant_id"],
            onDelete = ForeignKey.SET_NULL // 如果植物被删除，历史记录不会被删除，只是解除关联
        )
    ]
)
data class PredictionHistory(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "plant_id", index = true)
    var plantId: Long? = null,

    @ColumnInfo(name = "disease_name")
    val diseaseName: String,

    @ColumnInfo(name = "confidence")
    val confidence: Float,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "image_path")
    val imagePath: String,

    // Feedback can be "correct", "incorrect", or null if no feedback is given
    @ColumnInfo(name = "feedback")
    var feedback: String? = null
) 