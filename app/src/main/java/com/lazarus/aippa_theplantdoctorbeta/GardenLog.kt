package com.lazarus.aippa_theplantdoctorbeta

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * 代表与特定植物相关的一次养护活动记录
 *
 * @param id 唯一标识符，主键
 * @param plantId 外键，关联到 plants 表中的植物 id
 * @param activityType 活动类型 (使用 ActivityType 枚举)
 * @param date 活动发生的日期，以毫秒为单位的时间戳
 * @param description 活动的详细描述或备注
 * @param predictionId 可选的外键，仅当活动类型为 DIAGNOSIS 时，关联到 prediction_history 表
 */
@Entity(
    tableName = "garden_logs",
    indices = [androidx.room.Index(value = ["plantId"]), androidx.room.Index(value = ["predictionId"])],
    foreignKeys = [
        ForeignKey(
            entity = Plant::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE // 如果植物被删除，其所有日志也应被删除
        ),
        ForeignKey(
            entity = PredictionHistory::class,
            parentColumns = ["id"],
            childColumns = ["predictionId"],
            onDelete = ForeignKey.SET_NULL // 如果诊断历史被删除，日志中的关联ID设为NULL
        )
    ]
)
data class GardenLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantId: Long,
    val activityType: ActivityType,
    val date: Long,
    val description: String,
    val predictionId: Long? = null,
    val imagePath: String? = null // To store image path for DIAGNOSIS type logs
) 