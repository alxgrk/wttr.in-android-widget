package de.alxgrk.wttrinwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.wttr_in_widget_configure.*

private const val LOG_TAG = "ConfigActivity"

/**
 * The configuration screen for the [WttrInWidget] AppWidget.
 */
class WttrInWidgetActivity : Activity() {

    private var appWidgetId = INVALID_APPWIDGET_ID

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        val wttrRepository = WttrRepository(this)

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        setContentView(R.layout.wttr_in_widget_configure)

        btn_save.setOnClickListener {

            et_config.text.toString().also { location ->
                wttrRepository.saveLocation(appWidgetId, location)
                Log.i(LOG_TAG, "new location is $location")
            }

            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(this)
            WttrInWidget.updateAppWidget(
                this,
                appWidgetManager,
                appWidgetId,
                wttrRepository,
                PendingIntentCreator(this, appWidgetId)
            )

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

        wttrRepository.loadLocation(appWidgetId).also { location ->
            et_config.setText(location)
            Log.i(LOG_TAG, "loading saved location $location")
        }
    }

}

