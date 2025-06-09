package com.lazarus.aippa_theplantdoctorbeta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // 如果有 ActionBar，可以设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // 处理返回按钮的点击事件
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 