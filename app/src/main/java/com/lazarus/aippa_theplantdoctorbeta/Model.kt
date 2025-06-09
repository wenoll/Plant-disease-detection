package com.lazarus.aippa_theplantdoctorbeta

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Model(
    val name: String,
    val causativeAgent: String,
    val cause: String,
    val symptom1: String,
    val symptom2: String,
    val symptom3: String,
    val symptom4: String,
    val symptom5: String,
    val comment1: String,
    val comment2: String,
    val management1: String,
    val management2: String,
    val management3: String,
    val management4: String
) : Parcelable 