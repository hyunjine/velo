package com.hyunjine.velo_android.loader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import com.hyunjine.velo_core.ImageBitmapLoader
import com.hyunjine.velo_core.request.ImageRequestImpl
import com.hyunjine.velo_core.request.ImageSize
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpStatusCode
import io.ktor.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.net.SocketException
import java.util.concurrent.CancellationException
import kotlin.math.roundToInt

object AndroidImageLoader: ImageBitmapLoader {
    private const val TAG: String = "AndroidImageLoader"

    private val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                socketTimeoutMillis = 30_000 // 송수신 대기 시간
                connectTimeoutMillis = 10_000 // TCP 연결 대기 시간
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(TAG, message)
                    }
                }
            }
            install(HttpRequestRetry) {
                maxRetries = 3
                retryIf { request, response ->
                    when (response.status) {
                        HttpStatusCode.RequestTimeout, // 요청 시간이 초과되었을 때
                        HttpStatusCode.TooManyRequests, // 과도한 요청이 발생했을 때
                        HttpStatusCode.BadGateway, // 게이트웨이에 문제가 생겼을 때
                        HttpStatusCode.ServiceUnavailable, // 서버가 일시적으로 사용 불가능할 때
                        HttpStatusCode.GatewayTimeout -> true // 서버 간 통신 시간이 초과되었을 때
                        else -> false
                    }
                }
                retryOnExceptionIf { request, cause ->
                    when (cause) {
                        is CancellationException, // java.util.concurrent, not coroutine.
                        is SocketTimeoutException,
                        is ConnectTimeoutException,
                        is SocketException -> true
                        else -> false
                    }
                }
                // 지수 백오프 사용
                exponentialDelay(
                    baseDelayMs = 100,
                    maxDelayMs = 60_000,
                )
                modifyRequest { request ->
                    request.headers.append("x-retry-count", retryCount.toString())
                }
            }
        }
    }

    override fun getCachedImageBitmap(url: String, imageRequest: ImageRequestImpl): ImageBitmap? {
        return getCachedBitmap(url, imageRequest)?.asImageBitmap()
    }

    override suspend fun get(
        url: String,
        size: IntSize,
        imageRequest: ImageRequestImpl
    ): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            fetchBitmap(url = url, size = size, imageRequest = imageRequest)?.asImageBitmap()
        }
    }

    private suspend fun fetchBitmap(
        url: String,
        size: IntSize,
        imageRequest: ImageRequestImpl
    ): Bitmap? {
        return coroutineScope {
            ensureActive()

            // 이미지를 서버에서 가져옵니다.
            val byteArray = httpClient.get(url).bodyAsBytes()

            ensureActive()

            /*
             * imageRequest의 width, height가 ImageSize.Origin이라면 다운 샘플링을 패스합니다.
             * 그 외에는 다운 샘플링 작업이 시작됩니다.
             */
            val bitmap = if (imageRequest.width is ImageSize.Origin && imageRequest.height is ImageSize.Origin) {
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            } else {
                samplingBitmap(
                    byteArray = byteArray,
                    targetWidth = imageRequest.width,
                    targetHeight = imageRequest.height,
                    size = size
                )
            }
            // imageRequest의 메모리 또는 디스크 캐시 옵션 여부에 따라 진행됩니다.
            if (bitmap != null) {
                if (imageRequest.useMemoryCache) {
                    MemoryCache[url] = bitmap
                }
                if (imageRequest.useDiskCache) {
                    DiskCache[url] = bitmap
                }
            }
            bitmap
        }
    }

    private suspend fun samplingBitmap(
        byteArray: ByteArray,
        targetWidth: ImageSize,
        targetHeight: ImageSize,
        size: IntSize
    ): Bitmap? = coroutineScope {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

        ensureActive()

        /*
         * imageRequest의 width, height가 ImageSize.Specific이면 해당 값을 사용하고,
         * ImageSize.Default라면 size로 들어온 컴포넌트 사이즈 미만으로 다운 샘플링합니다.
         */

        val (reqWidth, reqHeight) = if (targetWidth is ImageSize.Specific && targetHeight is ImageSize.Specific) {
            targetWidth.value to targetHeight.value
        } else {
            size.width to size.height
        }
        options.apply {
            inJustDecodeBounds = false
            inSampleSize = calculateInSampleSize(
                origWidth = options.outWidth,
                origHeight = options.outHeight,
                reqWidth = reqWidth,
                reqHeight = reqHeight
            )
        }

        ensureActive()

        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)
    }

    private fun getCachedBitmap(url: String, imageRequest: ImageRequestImpl): Bitmap? {
        // 메모리 캐시를 확인합니다.
        if (imageRequest.useMemoryCache) {
            MemoryCache[url]?.let { return it }
        }
        if (imageRequest.useDiskCache) {
            // 디스크 캐시를 확인합니다.
            DiskCache[url]?.let {
                // 캐싱되어 있다면 메모리 캐시에 적재합니다.
                MemoryCache[url] = it
                return it
            }
        }
        return null
    }

    private fun calculateInSampleSize(
        origWidth: Int,
        origHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (origHeight > reqHeight || origWidth > reqWidth) {
            val heightRatio = (origHeight.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (origWidth.toFloat() / reqWidth.toFloat()).roundToInt()
            inSampleSize = maxOf(heightRatio, widthRatio)
        }
        return inSampleSize
    }
}