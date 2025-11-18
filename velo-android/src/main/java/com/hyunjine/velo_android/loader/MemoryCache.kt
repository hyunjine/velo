package com.hyunjine.velo_android.loader

import android.graphics.Bitmap
import android.util.LruCache

internal object MemoryCache {
    /*
     * 갤럭시 24+ 기준 할당된 힙 사이즈 256MB
     * WEBP 변환 시 이미지 개당 약 15KB
     * cacheSize에 대략 2,184개 캐싱 가능
     */
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // Glide 참고

    private val bitmapCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }


    operator fun set(key: String, bitmap: Bitmap) {
        bitmapCache.put(key, bitmap)
    }

    operator fun get(key: String): Bitmap? {
        return bitmapCache.get(key)
    }
}
