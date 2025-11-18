package com.hyunjine.velo_core.request

import androidx.annotation.Px
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

sealed interface ImageRequest {
    companion object: ImageRequest by ImageRequestImpl.empty()

    /**
     * 디코딩 size를 지정합니다.
     * @param width 디코딩 너비입니다. pixel 단위입니다.
     * @param height 디코딩 높이입니다. pixel 단위입니다.
     */
    fun size(@Px width: Int, @Px height: Int): ImageRequest

    /**
     * 디코딩 size를 지정합니다.
     * @param width 디코딩 너비입니다.
     * @param height 디코딩 높이입니다.
     */
    @Composable
    fun size(width: Dp, @Px height: Dp): ImageRequest

    /**
     * 다운샘플링 없이 원본을 유지합니다.
     */
    fun originSize(): ImageRequest

    /**
     * [com.hyunjine.velo_core.VeloImage]의 크기에 맞게 디코딩 size를 지정합니다.
     */
    fun componentSize(): ImageRequest

    /**
     * 메모리 캐시 사용 여부를 결정합니다.
     */
    fun useMemoryCache(enabled: Boolean): ImageRequest

    /**
     * 디시크 캐시 사용 여부를 결정합니다.
     */
    fun useDiskCache(enabled: Boolean): ImageRequest
}
