package com.lazarus.aippa_theplantdoctorbeta

import java.util.Date

sealed class TimelineItem {
    abstract val id: Long
    abstract val date: Long

    data class Log(
        val gardenLog: GardenLog
    ) : TimelineItem() {
        override val id: Long = gardenLog.id
        override val date: Long = gardenLog.date
    }

    data class Diagnosis(
        val gardenLog: GardenLog,
        val predictionHistory: PredictionHistory
    ) : TimelineItem() {
        override val id: Long = gardenLog.id
        override val date: Long = gardenLog.date
    }
}
