package com.hyunjine.velo_core

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.hyunjine.velo_core.request.ImageRequest
import com.hyunjine.velo_core.request.ImageRequestImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.text.get

/**
 * @param url 이미지를 요청할 url 주소입니다.
 * @param imageLoader 플랫폼 별 사용할 이미지 로딩 엔진입니다.
 * @param onSuccess 이미지 로딩이 성공하면 호출됩니다.
 * @param onError 이미지 로딩 과정 중 실패하면 호출됩니다.
 * @param error [onError]호출 시 표시할 에러 이미지입니다.
 * @param imageRequest 이미지 디코딩 사이즈, 메모리 및 디스크 캐시 사용 여부와 같은 요청 파라미터입니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun VeloImage(
    modifier: Modifier = Modifier,
    url: String,
    imageLoader: ImageBitmapLoader,
    contentDescription: String?,
    onSuccess: () -> Unit = {},
    onError: (Throwable) -> Unit = {},
    error: Painter? = null,
    imageRequest: ImageRequest = ImageRequestImpl.empty(),
    contentScale: ContentScale = ContentScale.Fit
) {
    // 이미지 로딩 성공 시 반영됩니다.
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    // VeloImage의 레이아웃 측정 결과가 반영됩니다. 다운 샘플링을 위함입니다.
    var layoutSize by remember { mutableStateOf<IntSize?>(null) }
    // 이미지 로딩 실패 시 반영됩니다.
    var errorState by remember { mutableStateOf(false) }

    require(imageRequest is ImageRequestImpl)

    val handler = CoroutineExceptionHandler { _, e ->
        onError(e)
        errorState = true
    }

    LaunchedEffect(url) {
        errorState = false

        launch(SupervisorJob() + Dispatchers.Main + handler) {
            snapshotFlow { url }
                .flatMapLatest { url ->
                    val cache = imageLoader.getCachedImageBitmap(url = url, imageRequest = imageRequest)
                    // 캐싱된 이미지가 있으면 반환합니다.
                    if (cache != null) {
                        flowOf(cache)
                    } else {
                        // 측정이 완료되고나면 imageLoader에 요청을 보냅니다.
                        snapshotFlow { layoutSize }
                            .filterNotNull()
                            .mapLatest { size ->
                                imageLoader.get(url = url, size = size, imageRequest = imageRequest)
                            }
                    }
                }.collectLatest {
                    imageBitmap = it
                    onSuccess()
                }
        }
    }
    Column(
        modifier = modifier
            .then(Modifier.onSizeChanged { layoutSize = it })
    ) {
        if (errorState && error != null) {
            Image(
                painter = error,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }
    }
}