package de.alxgrk.wttrinwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.*

private const val LOG_TAG = "Widget"

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [WttrInWidgetActivity]
 */
class WttrInWidget : AppWidgetBroadcastReceiver() {

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val wttrRepository = WttrRepository(context)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId,
                wttrRepository,
                PendingIntentCreator(context, appWidgetId)
            )
        }
    }

    override fun onToggleIcons(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        showIcons: Boolean
    ) {
        super.onToggleIcons(context, appWidgetManager, appWidgetIds, showIcons)

        for (appWidgetId in appWidgetIds) {
            val pendingIntentCreator = PendingIntentCreator(context, appWidgetId)
            val views = RemoteViews(context.packageName, R.layout.wttr_in_widget)

            Log.d(LOG_TAG, "additional icons shown: $showIcons")
            views.setViewVisibility(R.id.iv_settings, if (showIcons) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.iv_sync, if (showIcons) View.VISIBLE else View.GONE)

            views.setOnClickPendingIntent(R.id.root_wttr, pendingIntentCreator.toggleIconsIntent(!showIcons))

            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)

        val wttrRepository = WttrRepository(context)
        for (appWidgetId in appWidgetIds) {
            wttrRepository.deleteLocation(appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        minWidth: Int,
        maxWidth: Int,
        minHeight: Int,
        maxHeight: Int
    ) {
        super.onAppWidgetOptionsChanged(
            context, appWidgetManager, appWidgetId, minWidth, maxWidth, minHeight, maxHeight
        )

        val views = RemoteViews(context.packageName, R.layout.wttr_in_widget)
        views.setWttrToImageView(appWidgetManager, appWidgetId, context, minWidth, minHeight)

    }

    companion object {

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            wttrRepository: WttrRepository,
            pendingIntentCreator: PendingIntentCreator
        ) {

            val location = wttrRepository.loadLocation(appWidgetId)
            if (location.isEmpty())
                return

            Log.i(LOG_TAG, "loading wttr.in information for location $location")

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.wttr_in_widget)
            views.setImageViewResource(R.id.iv_wttr, R.drawable.ic_waiting)

            // request and display wttr
            val minWidth = appWidgetManager.getAppWidgetOptions(appWidgetId)
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val minHeight = appWidgetManager.getAppWidgetOptions(appWidgetId)
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            views.setWttrToImageView(
                appWidgetManager, appWidgetId, context,
                minWidth, minHeight, wttrRepository, location
            )

            // ICONS
            views.setViewVisibility(R.id.iv_settings, View.GONE)
            views.setViewVisibility(R.id.iv_sync, View.GONE)

            // update icon views on click
            views.setOnClickPendingIntent(R.id.root_wttr, pendingIntentCreator.toggleIconsIntent(true))

            // update wttr
            views.setOnClickPendingIntent(R.id.iv_sync, pendingIntentCreator.updateWidgetIntent())

            // show settings activity
            views.setOnClickPendingIntent(R.id.iv_settings, pendingIntentCreator.showSettingsIntent())

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun RemoteViews.setWttrToImageView(
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            context: Context,
            minWidth: Int,
            minHeight: Int,
            wttrRepository: WttrRepository = WttrRepository(context),
            location: String = wttrRepository.loadLocation(appWidgetId)
        ) {
            wttrJob?.cancel("cancel running job before launching new one")
            wttrJob = CoroutineScope(Dispatchers.IO).launch {
                val forecastLevel = when {
                    minWidth in 0..200 -> ForecastLevel.ZERO
                    minHeight in 0..100 -> ForecastLevel.ONE
                    minHeight in 101..150 -> ForecastLevel.TWO
                    else -> ForecastLevel.THREE
                }
                val wttr = wttrRepository.getWttrFor(location, forecastLevel)
                setImageViewBitmap(R.id.iv_wttr, wttr.wttrImage.toBitmap())
                appWidgetManager.partiallyUpdateAppWidget(appWidgetId, this@setWttrToImageView)
            }
        }

        private var wttrJob: Job? = null

    }

}

