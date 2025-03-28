/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.resources.impl.ext

import androidx.core.net.toUri
import app.ss.models.AudioAux
import app.ss.models.VideoAux
import app.ss.models.media.SSVideo
import ss.libraries.storage.api.entity.AudioFileEntity
import ss.libraries.storage.api.entity.VideoInfoEntity
import ss.misc.SSConstants.SS_IMAGES_BASE_URL
import timber.log.Timber

fun AudioAux.toEntity(): AudioFileEntity = AudioFileEntity(
    id = this.id,
    title = this.title,
    artist = this.artist,
    src = this.src,
    image = this.image.takeIf { it.isFullUrl() } ?: "$SS_IMAGES_BASE_URL$image",
    imageRatio = this.imageRatio,
    target = this.target,
    targetIndex = this.targetIndex,
    duration = 0
)

internal fun VideoAux.toEntity(
    id: String,
    lessonIndex: String
): VideoInfoEntity = VideoInfoEntity(
    id = id,
    lessonIndex = lessonIndex,
    artist = artist,
    clips = clips.mapIndexed { index, clip ->
        SSVideo(
            id = clip.id,
            artist = clip.artist,
            title = clip.title,
            target = clip.target,
            targetIndex = clip.targetIndex,
            src = clip.src,
            thumbnail = clip.thumbnail.takeIf { it.isFullUrl() } ?: "$SS_IMAGES_BASE_URL${clip.thumbnail}",
        )
    }
)


private fun String.isFullUrl(): Boolean {
    return try {
        val uri = toUri()
        uri.scheme != null && uri.host != null
    } catch (e: Exception) {
        Timber.e(e)
        false
    }
}
