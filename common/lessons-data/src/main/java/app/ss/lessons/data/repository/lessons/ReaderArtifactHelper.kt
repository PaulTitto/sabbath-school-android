/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.lessons.data.repository.lessons

import android.content.Context
import app.ss.models.config.AppConfig
import app.ss.network.NetworkResource
import app.ss.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import ss.foundation.android.connectivity.ConnectivityHelper
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.ioScopable
import ss.lessons.api.SSLessonsApi
import ss.misc.SSConstants
import ss.prefs.api.SSPrefs
import timber.log.Timber
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@Singleton
internal class ReaderArtifactHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lessonsApi: SSLessonsApi,
    private val ssPrefs: SSPrefs,
    private val okHttpClient: OkHttpClient,
    private val dispatcherProvider: DispatcherProvider,
    private val connectivityHelper: ConnectivityHelper,
    appConfig: AppConfig
) : Scopable by ioScopable(dispatcherProvider) {

    private val apiBaseUrl = if (appConfig.isDebug)
        SSConstants.SS_STAGE_API_BASE_URL else SSConstants.SS_API_BASE_URL
    private val readerUrl: String = "${apiBaseUrl}reader/$SS_READER_ARTIFACT_NAME"

    fun sync() = try {
        scope.launch {
            val response = safeApiCall(connectivityHelper) {
                lessonsApi.readerArtifact(readerUrl)
            }
            when (response) {
                is NetworkResource.Success -> {
                    val lastModified = response.value.headers()[LAST_MODIFIED] ?: return@launch

                    if (ssPrefs.getReaderArtifactLastModified() != lastModified) {
                        if (downloadFile()) {
                            ssPrefs.setReaderArtifactLastModified(lastModified)
                        }
                    } else if (!File(context.filesDir, SSConstants.SS_READER_ARTIFACT_FOLDER_NAME).exists()) {
                        unzip()
                    }
                }
                is NetworkResource.Failure -> {}
            }
        }
    } catch (error: Throwable) {
        Timber.e(error)
    }

    private suspend fun downloadFile(): Boolean {
        val destination = File(context.filesDir, SS_READER_ARTIFACT_NAME)
        val request = Request.Builder().url(readerUrl).build()
        return try {
          val response = okHttpClient.newCall(request).execute()
          val body =
              response.body
                  ?: run {
                    Timber.e("Failed to download reader")
                    return false
                  }
          destination.sink().buffer().use { sink -> sink.writeAll(body.source()) }
          unzip()
          true
        } catch (error: Throwable) {
          Timber.e(error)
          false
        }
    }

    private suspend fun unzip() =
      withContext(dispatcherProvider.io) {
        val file = File(context.filesDir, SS_READER_ARTIFACT_NAME)
        if (file.exists()) {
          file.unzip()
        }
      }

    companion object {
        private const val LAST_MODIFIED = "last-modified"
        private const val SS_READER_ARTIFACT_NAME = "${SSConstants.SS_READER_ARTIFACT_FOLDER_NAME}.zip"
    }
}

private data class ZipIO(val entry: ZipEntry, val output: File)

/** Unzips this file contents. */
private fun File.unzip() {
    val rootFolder = File(parentFile?.absolutePath + File.separator + nameWithoutExtension)
    if (!rootFolder.exists()) {
        rootFolder.mkdirs()
    }

    ZipFile(this).use { zip ->
        zip.entries()
            .asSequence()
            .map {
                val outputFile = File(rootFolder.absolutePath + File.separator + it.name)
                ZipIO(it, outputFile)
            }
            .map {
                it.output.parentFile?.run { if (!exists()) mkdirs() }
                it
            }
            .filter { !it.entry.isDirectory }
            .forEach { (entry, output) ->
                zip.getInputStream(entry).use { input ->
                    output.outputStream().use { output -> input.copyTo(output) }
                }
            }
    }
}
