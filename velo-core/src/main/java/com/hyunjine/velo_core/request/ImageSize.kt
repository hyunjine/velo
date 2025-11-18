package com.hyunjine.velo_core.request

sealed interface ImageSize {
    // 디폴트 값. Image 컴포저블 width, height에 따름
    data object Default: ImageSize

    // 원본 이미지에 따름
    data object Origin: ImageSize

    // 구체적인 수치에 따름
    data class Specific(val value: Int) : ImageSize
}