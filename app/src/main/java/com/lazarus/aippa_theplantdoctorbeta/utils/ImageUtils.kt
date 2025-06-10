package com.lazarus.aippa_theplantdoctorbeta.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

/**
 * 图片处理工具类，用于将临时URI转换为永久存储路径
 */
object ImageUtils {

    private const val TAG = "ImageUtils"
    private const val IMAGES_FOLDER = "plant_images"

    /**
     * 将URI指向的图片保存到应用私有存储，返回永久URI
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): Uri? {
        try {
            // 创建应用私有存储目录
            val imagesDir = File(context.filesDir, IMAGES_FOLDER).apply {
                if (!exists()) mkdirs()
            }

            // 生成唯一文件名
            val imageFile = File(imagesDir, "IMG_${UUID.randomUUID()}.jpg")
            
            // 复制图片内容
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(imageFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // 返回私有存储URI
            return Uri.fromFile(imageFile)
        } catch (e: IOException) {
            Log.e(TAG, "Error saving image: ${e.message}")
            return null
        }
    }

    /**
     * 获取URI的永久版本（如果是临时URI则复制到私有存储）
     */
    fun getPermamentUri(context: Context, uri: Uri?): Uri? {
        if (uri == null) return null
        
        // 检查是否已经是私有存储的URI
        val path = uri.path ?: return uri
        if (path.contains(context.filesDir.path) && path.contains(IMAGES_FOLDER)) {
            return uri
        }
        
        // 否则复制到私有存储
        return saveImageToInternalStorage(context, uri)
    }
} 