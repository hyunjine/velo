<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <img src="https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat" />
</p>


## What's Velo?
Velo is image loading library running on Compose. It serves many options with ImageRequest, that contains resize image, turn on memory and disk cache.
Also, it serves fallback when occur error in requesting network, so you can replace image on error.

## Download
![Static Badge](https://img.shields.io/badge/Maven%20Central-v0.2.0-blue)

### Gradle

Add the dependency below into your **module**'s `build.gradle` file:

```gradle
dependencies {
    implementation("io.github.hyunjine:velo-core:0.2.0")
    implementation("io.github.hyunjine:velo-android:0.2.0") // For Android
}
```

## Usage
### VeloImage
VeloImage is Composable funcion to render image through url. Think about [AsyncImage](https://github.com/coil-kt/coil).

```kotlin
@Composable
fun VeloImage(
    modifier: Modifier = Modifier,
    url: String,
    imageLoader: ImageBitmapLoader,
    imageRequest: ImageRequest = ImageRequestImpl.empty(),
    contentDescription: String?,
    onSuccess: () -> Unit = {},
    onError: (Throwable) -> Unit = {},
    error: Painter? = null,
    contentScale: ContentScale = ContentScale.Fit
)
```
### Parameter
- **modifier** : Yes. It is androidx.compose.ui.Modifier you know.
- **url** : The path that bring image download.
- **imageLoader** : It is a interface, but if you add velo-android module, the implementation will be imported. Why the loader is interface, because Velo will serve Kotlin Multiplatform. ImageLoader has been once type as AndroidImageLoader not yet.
- **imageRequest** : An option how to render, resize, memory or disk cache. 
- **contentDescription** : A description for accessibility.
- **onSuccess** : It will be called when the url is be transformed to a bitmap with successful.
- **onError** : It will be called when the error occur processing from url to bitmap.
- **error** : If the condition is onError called, you can replace empty with this.
- contentScale : Yes. It is androidx.compose.ui.layout.ContentScale you know.

### ImageRequest
- **componentSize()** : Default option. It automatically measure VeloImage size and resize the downloaded image to measured layout(widht, height).
```kotlin
VeloImage(
  imageRequest = ImageRequest
    .componentSize(),
  ...
)
```
- **originSize()** : Not downsampling, so display on UI real resolution.
```kotlin
VeloImage(
  imageRequest = ImageRequest
    .originSize(),
  ...
)
```

- **size(width: Int, height: Int)** : Resize as pixel Unit.
```kotlin
VeloImage(
  imageRequest = ImageRequest
    .size(width = 100, height = 100),
  ...
)
```

- @Composable **size(width: Dp, height: Dp)** : Resize as dp Unit.
```kotlin
VeloImage(
  imageRequest = ImageRequest
    .size(width = 100.dp, height = 100.dp),
  ...
)
```

- **useMemoryCache(enabled: Boolean)** : Whether turn on memory cache option. If enabled is true, use memory cache.
```kotlin
VeloImage(
  imageRequest = ImageRequest
    .useMemoryCache(true),
  ...
)
```

- **useDiskCache(enabled: Boolean)** : Whether turn on disk cache option. If enabled is true, use memory cache.
```kotlin
VeloImage(
  imageRequest = ImageRequest
    .useDiskCache(true),
  ...
)
```

## Welcome to register any issues.

# License
```xml
Copyright 2025 hyunjine (HyunJin Yang)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
