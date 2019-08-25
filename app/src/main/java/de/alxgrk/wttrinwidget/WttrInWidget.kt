package de.alxgrk.wttrinwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val LOG_TAG = "Widget"
private const val ICONS_SHOWN_KEY = "areIconsShown"
private const val ACTION_TOGGLE_ICONS = "de.alxgrk.wttrinwidget.action.TOGGLE_ICONS"

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [WttrInWidgetActivity]
 */
class WttrInWidget : BroadcastReceiver() {

    private fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        Log.d(LOG_TAG, "updating ${appWidgetIds.toList()}")
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

    private fun onToggleIcons(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        showIcons: Boolean
    ) {
        Log.d(LOG_TAG, "toggling icons for ${appWidgetIds.toList()}")
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

    private fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(LOG_TAG, "deleting ${appWidgetIds.toList()}")
        val wttrRepository = WttrRepository(context)
        for (appWidgetId in appWidgetIds) {
            wttrRepository.deleteLocation(appWidgetId)
        }
    }

    private fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        Log.d(
            LOG_TAG,
            "widget resized to minWidth=${newOptions.getFloat(OPTION_APPWIDGET_MIN_WIDTH)}<->maxWidth=${newOptions
                .getFloat(OPTION_APPWIDGET_MAX_WIDTH)} & minHeight=${newOptions
                .getFloat(OPTION_APPWIDGET_MIN_HEIGHT)}<->maxHeight=${newOptions.getFloat(OPTION_APPWIDGET_MAX_HEIGHT)}"
        )
    }

    private fun onEnabled(context: Context) {
        Log.d(LOG_TAG, "enabled app widget")
    }

    private fun onDisabled(context: Context) {
        Log.d(LOG_TAG, "disabled app widget")
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

            // request and display wttr
            views.setImageViewResource(R.id.iv_wttr, R.drawable.ic_waiting)
            CoroutineScope(Dispatchers.IO).launch {
                val wttr = wttrRepository.getWttrFor(location)
                views.setImageViewBitmap(R.id.iv_wttr, wttr.wttrImage.toBitmap())
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }

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

    }

    private fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray?) {
        Log.d(LOG_TAG, "restored app widgets: ${oldWidgetIds.toList()} -> ${newWidgetIds?.toList()}")
    }


    /**
     * copied from {@link AppWidgetProvider}
     */
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val extras = intent.extras

        when (action) {
            ACTION_APPWIDGET_ENABLED -> {
                this.onEnabled(context)
            }
            ACTION_APPWIDGET_DISABLED -> {
                this.onDisabled(context)
            }
        }
        if (extras != null)
            when (action) {
                ACTION_TOGGLE_ICONS -> {
                    val appWidgetIds = extras.getIntArray(EXTRA_APPWIDGET_IDS)
                    if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                        Log.d(LOG_TAG, "extras: ${extras.getBoolean(ICONS_SHOWN_KEY)}")
                        this.onToggleIcons(
                            context,
                            getInstance(context),
                            appWidgetIds,
                            extras.getBoolean(ICONS_SHOWN_KEY)
                        )
                    }
                }
                ACTION_APPWIDGET_UPDATE -> {
                    val appWidgetIds = extras.getIntArray(EXTRA_APPWIDGET_IDS)
                    if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                        this.onUpdate(context, getInstance(context), appWidgetIds)
                    }
                }
                ACTION_APPWIDGET_DELETED -> {
                    if (extras.containsKey(EXTRA_APPWIDGET_ID)) {
                        val appWidgetId = extras.getInt(EXTRA_APPWIDGET_ID)
                        this.onDeleted(context, intArrayOf(appWidgetId))
                    }
                }
                ACTION_APPWIDGET_OPTIONS_CHANGED -> {
                    if (extras.containsKey(EXTRA_APPWIDGET_ID) && extras.containsKey(EXTRA_APPWIDGET_OPTIONS)) {
                        val appWidgetId = extras.getInt(EXTRA_APPWIDGET_ID)
                        val widgetExtras = extras.getBundle(EXTRA_APPWIDGET_OPTIONS)
                        this.onAppWidgetOptionsChanged(
                            context, getInstance(context),
                            appWidgetId, widgetExtras ?: Bundle.EMPTY
                        )
                    }
                }
                // copied value from constant to fix API version issues
                "android.appwidget.action.APPWIDGET_RESTORED" -> {
                    val oldIds = extras.getIntArray("appWidgetOldIds")
                    val newIds = extras.getIntArray(EXTRA_APPWIDGET_IDS)
                    if (oldIds != null && oldIds.isNotEmpty()) {
                        this.onRestored(context, oldIds, newIds)
                        this.onUpdate(context, getInstance(context), newIds ?: intArrayOf())
                    }
                }
            }
    }
}

class PendingIntentCreator(private val context: Context, private val appWidgetId: Int) {

    fun toggleIconsIntent(showIcons: Boolean): PendingIntent? {
        val iconIntent = Intent(context, WttrInWidget::class.java).apply {
            action = ACTION_TOGGLE_ICONS
            putExtra(EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            putExtra(ICONS_SHOWN_KEY, showIcons)
        }
        return PendingIntent.getBroadcast(context, appWidgetId, iconIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun updateWidgetIntent(): PendingIntent? {
        val wttrIntent = Intent(context, WttrInWidget::class.java).apply {
            action = ACTION_APPWIDGET_UPDATE
            putExtra(EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        return PendingIntent.getBroadcast(context, appWidgetId, wttrIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun showSettingsIntent(): PendingIntent? {
        val settingsIntent = Intent(context, WttrInWidgetActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getActivity(context, appWidgetId, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}