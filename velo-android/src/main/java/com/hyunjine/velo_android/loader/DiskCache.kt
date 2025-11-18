package com.hyunjine.velo_android.loader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.hyunjine.velo_android.applicationContext
import com.jakewharton.disklrucache.DiskLruCache
import java.io.File
import java.io.IOException

internal object DiskCache {
    // 캐시 구조 변경 시 increase 필요
    private const val APP_VERSION = 1

    private const val VALUE_COUNT = 1
    /*
     * 50MB
     * WEBP 변환 시 이미지 개당 약 15KB
     * 총 약 3,231개 캐싱 가능
     * 기본 이미지 기준 originalSize: 66,660,000B (63.6MB)
     * 갤럭시 24+ 기준 sampledSize 1,042,500B (1MB)
     */
    private const val MAX_SIZE = 50L * 1024L * 1024L

    /*
     * WEBP 변환 시 압축 률
     * 80%면 사람 눈으로 구별 어려움
     */
    private const val FORMAT_QUALITY: Int = 80

    private val cacheDir: File = File(applicationContext.cacheDir, "images")

    private val diskLruCache: DiskLruCache by lazy {
        DiskLruCache.open(cacheDir, APP_VERSION, VALUE_COUNT, MAX_SIZE)
    }

    operator fun set(key: String, bitmap: Bitmap) {
        val hashedKey = keyToFileName(key)
        try {
            val editor = diskLruCache.edit(hashedKey) ?: return
            editor.newOutputStream(0).use { out ->
                bitmap.compress(Bitmap.CompressFormat.WEBP, FORMAT_QUALITY, out)
            }
            editor.commit()
        } catch (_: IOException) { }
    }

    operator fun get(key: String): Bitmap? {
        val hashedKey = keyToFileName(key)
        return runCatching {
            val snapshot = diskLruCache.get(hashedKey) ?: return null
            snapshot.getInputStream(0).use { input ->
                BitmapFactory.decodeStream(input)
            }
        }.getOrNull()
    }

    private fun keyToFileName(key: String): String {
        return key.hashCode().toString()
    }
}
