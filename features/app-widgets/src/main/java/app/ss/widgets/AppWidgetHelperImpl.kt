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

package app.ss.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import app.ss.widgets.glance.today.TodayAppWidget
import app.ss.widgets.glance.today.TodayImageAppWidget
import app.ss.widgets.glance.week.LessonInfoWidget
import app.ss.widgets.today.TodayAppWidgetProvider
import app.ss.widgets.today.TodayImgAppWidgetProvider
import app.ss.widgets.week.WeekLessonWidget
import app.ss.widgets.work.WidgetUpdateWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.foundation.coroutines.Scopable
import ss.foundation.coroutines.defaultScopable
import ss.libraries.appwidget.api.AppWidgetHelper
import timber.log.Timber
import javax.inject.Inject

class AppWidgetHelperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider,
) : AppWidgetHelper, Scopable by defaultScopable(dispatcherProvider) {

    private val exceptionLogger = CoroutineExceptionHandler { _, exception -> Timber.e(exception) }

    private val widgets = listOf(
        TodayAppWidgetProvider::class.java,
        TodayImgAppWidgetProvider::class.java,
        WeekLessonWidget::class.java,
    )

    override fun refreshAll() {
        scope.launch(exceptionLogger) {
            if (isAdded()) {
                updateWidgets()

                WidgetUpdateWorker.enqueuePeriodic(context)
            } else {
                WidgetUpdateWorker.cancel(context)
            }
        }
    }

    private suspend fun updateWidgets() {
        with(context) {
            widgets.forEach { clazz ->
                sendBroadcast(
                    Intent(BaseWidgetProvider.REFRESH_ACTION)
                        .setComponent(ComponentName(context, clazz))
                )
            }
        }

        LessonInfoWidget().updateAll(context)
        TodayAppWidget().updateAll(context)
        TodayImageAppWidget().updateAll(context)
    }

    override fun languageChanged() {
        scope.launch {
            if (isAdded()) {
                WidgetUpdateWorker.enqueue(context)
            }
        }
    }

    private suspend fun isAdded(): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val glanceAppWidgetManager = GlanceAppWidgetManager(context)

        val glanceWidgetIds = withContext(dispatcherProvider.default) {
            glanceAppWidgetManager.getGlanceIds(LessonInfoWidget().javaClass) +
                glanceAppWidgetManager.getGlanceIds(TodayAppWidget().javaClass) +
                glanceAppWidgetManager.getGlanceIds(TodayImageAppWidget().javaClass)

        }
        val legacyWidgetIds = widgets.map { appWidgetManager.getAppWidgetIds(ComponentName(context, it)) }

        return (glanceWidgetIds + legacyWidgetIds).isNotEmpty()
    }
}
