package de.alxgrk.wttrinwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

class PendingIntentCreator(private val context: Context, private val appWidgetId: Int) {

    fun toggleIconsIntent(showIcons: Boolean): PendingIntent? {
        val iconIntent = Intent(context, WttrInWidget::class.java).apply {
            action = ACTION_TOGGLE_ICONS
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            putExtra(ICONS_SHOWN_KEY, showIcons)
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            iconIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun updateWidgetIntent(): PendingIntent? {
        val wttrIntent = Intent(context, WttrInWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            wttrIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun showSettingsIntent(): PendingIntent? {
        val settingsIntent = Intent(context, WttrInWidgetActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getActivity(
            context,
            appWidgetId,
            settingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

}