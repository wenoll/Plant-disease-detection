package com.lazarus.aippa_theplantdoctorbeta

data class Disease(
    val id: Int,
    val name: String,
    val causativeAgent: String,
    val viewType: Int // 0 for header, 1 for disease item
) {
    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
    }
} 