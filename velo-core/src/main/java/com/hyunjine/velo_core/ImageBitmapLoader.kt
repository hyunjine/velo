package com.hyunjine.velo_core

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import com.hyunjine.velo_core.request.ImageRequestImpl

interface ImageBitmapLoader {
    /**
     * [url]을 key로 사용하여 캐싱된 [ImageBitmap]을 반환합니다.
     * 없다면 null을 반환합니다.
     */
    fun getCachedImageBitmap(url: String, imageRequest: ImageRequestImpl): ImageBitmap?

    /**
     * [url]으로 이미지를 다운로드 하여 [ImageBitmap]을 반환합니다.
     * 받아온 이미지를 [size]미만으로 다운샘플링하여 메모리와 디스크에 캐싱합니다.
     * 이미지 다운로드 또는 캐싱 실패 시 null을 반환합니다.
     */
    suspend fun get(url: String, size: IntSize, imageRequest: ImageRequestImpl): ImageBitmap?
}