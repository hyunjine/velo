package com.hyunjine.velo_core.request

import androidx.annotation.Px
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

data class ImageRequestImpl(
    var width: ImageSize,
    var height: ImageSize,
    var useMemoryCache: Boolean,
    var useDiskCache: Boolean,
) : ImageRequest {
    companion object {
        fun empty() = ImageRequestImpl(
            width = ImageSize.Default,
            height = ImageSize.Default,
            useMemoryCache = true,
            useDiskCache = true
        )
    }

    override fun size(@Px width: Int, @Px height: Int) = apply {
        this.width = ImageSize.Specific(width)
        this.height = ImageSize.Specific(height)
    }

    @Composable
    override fun size(width: Dp, height: Dp) = apply {
        this.width = ImageSize.Specific(width.toPx)
        this.height = ImageSize.Specific(height.toPx)
    }

    override fun originSize(): ImageRequest = apply {
        this.width = ImageSize.Origin
        this.height = ImageSize.Origin
    }

    override fun componentSize(): ImageRequest = apply {
        this.width = ImageSize.Default
        this.height = ImageSize.Default
    }

    override fun useMemoryCache(enabled: Boolean): ImageRequest = apply {
        this.useMemoryCache = enabled
    }

    override fun useDiskCache(enabled: Boolean): ImageRequest = apply {
        this.useDiskCache = enabled
    }

    private val Dp.toPx: Int
        @Composable
        get() = with(LocalDensity.current) {
            this@toPx.toPx()
        }.toInt()
}