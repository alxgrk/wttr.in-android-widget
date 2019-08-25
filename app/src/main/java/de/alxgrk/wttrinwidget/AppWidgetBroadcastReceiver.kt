package de.alxgrk.wttrinwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

const val ICONS_SHOWN_KEY = "areIconsShown"
const val ACTION_TOGGLE_ICONS = "de.alxgrk.wttrinwidget.action.TOGGLE_ICONS"

private const val LOG_TAG = "AppWidgetReceiver"

abstract class AppWidgetBroadcastReceiver : BroadcastReceiver() {

    open fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(LOG_TAG, "updating ${appWidgetIds.toList()}")
    }

    open fun onToggleIcons(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        showIcons: Boolean
    ) {
        Log.d(LOG_TAG, "toggling icons for ${appWidgetIds.toList()}")
    }

    open fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(LOG_TAG, "deleting ${appWidgetIds.toList()}")
    }

    open fun onAppWidgetOptionsChanged(
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

    open fun onEnabled(context: Context) {
        Log.d(LOG_TAG, "enabled app widget")
    }

    open fun onDisabled(context: Context) {
        Log.d(LOG_TAG, "disabled app widget")
    }

    open fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray?) {
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