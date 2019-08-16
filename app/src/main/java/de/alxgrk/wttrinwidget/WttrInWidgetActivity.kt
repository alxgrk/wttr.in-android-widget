package de.alxgrk.wttrinwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.wttr_in_widget_configure.*

/**
 * The configuration screen for the [WttrInWidget] AppWidget.
 */
class WttrInWidgetActivity : Activity() {

    private var appWidgetId = INVALID_APPWIDGET_ID

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        setContentView(R.layout.wttr_in_widget_configure)

        btn_save.setOnClickListener {

            et_config.text.toString().also { location ->
                save(appWidgetId, location)
                Log.i(LOG_TAG, "new location is $location")
            }

            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(this)
            WttrInWidget.updateAppWidget(this, appWidgetManager, appWidgetId, load(appWidgetId))

            // Make sure we pass back the original appWidgetId
            Intent().apply {
                putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(RESULT_OK, this)
            }
            finish()
        }


        // Find the widget id from the intent.
        appWidgetId = intent.extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
            ?: INVALID_APPWIDGET_ID

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        load(appWidgetId).also { location ->
            et_config.setText(location)
            Log.i(LOG_TAG, "loading saved location $location")
        }
    }

    companion object {

        private const val LOG_TAG = "ConfigActivity"

        private const val PREFS_NAME = "de.alxgrk.wttrinwidget.WttrInWidget"

        private fun WttrInWidgetActivity.save(appWidgetId: Int, text: String) =
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString("$appWidgetId", text)
                .apply()

        private fun WttrInWidgetActivity.load(appWidgetId: Int): String =
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString("$appWidgetId", null) ?: ""

        internal fun delete(context: Context, appWidgetId: Int) = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .remove("$appWidgetId")
            .apply()

    }
}

