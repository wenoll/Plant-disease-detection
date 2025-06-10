package com.lazarus.aippa_theplantdoctorbeta

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 代表用户花园中的一株植物
 *
 * @param id 唯一标识符，主键
 * @param name 用户为植物取的名字 (例如 "客厅窗台的绿萝")
 * @param variety 植物的品种 (例如 "绿萝")
 * @param plantingDate 种植日期，以毫秒为单位的时间戳
 * @param location 种植位置 (例如 "客厅窗台")
 * @param notes 备注信息
 */
@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val variety: String,
    val plantingDate: Long,
    val location: String,
    val notes: String? = null
) 