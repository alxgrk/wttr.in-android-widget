package de.alxgrk.wttrinwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val LOG_TAG = "Widget"

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [WttrInWidgetActivity]
 */
class WttrInWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(LOG_TAG, "updating ${appWidgetIds.toList()}")
        val wttrRepository = WttrRepository(context)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, wttrRepository)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(LOG_TAG, "deleting ${appWidgetIds.toList()}")
        val wttrRepository = WttrRepository(context)
        for (appWidgetId in appWidgetIds) {
            wttrRepository.deleteLocation(appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            wttrRepository: WttrRepository
        ) {

            val location = wttrRepository.loadLocation(appWidgetId)
            if (location.isEmpty())
                return

            // Construct the RemoteViews object
            Log.i(LOG_TAG, "loading wttr.in information for location $location")
            val views = RemoteViews(context.packageName, R.layout.wttr_in_widget)
            views.setTextViewText(R.id.tv_wttr, "waiting for wttr data...")

            // connect to view model
            CoroutineScope(Dispatchers.IO).launch {
                val wttr = wttrRepository.getWttrFor(location)
                views.setTextViewText(R.id.tv_wttr, wttr.wttrData)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }

            // update
            val intent = Intent(context, WttrInWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.root_wttr, pendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

