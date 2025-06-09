package com.lazarus.aippa_theplantdoctorbeta

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class PredictionHistory(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

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