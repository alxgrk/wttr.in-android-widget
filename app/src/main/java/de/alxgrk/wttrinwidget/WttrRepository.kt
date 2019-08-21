package de.alxgrk.wttrinwidget

import android.app.Activity
import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request

private const val LOG_TAG = "WttrRepository"
private const val PREFS_NAME = "de.alxgrk.wttrinwidget.WttrInWidget"

class WttrRepository(private val context: Context) {

    val ok = OkHttpClient()

    fun getWttrFor(location: String): Wttr {
        val url = "https://wttr.in/$location?format=3"

        Log.d(LOG_TAG, "requesting $url")

        val request = Request.Builder()
            .url(url)
            .build()
        // TODO catch SocketTimeoutException
        val response = ok.newCall(request).execute().body()?.string() ?: ""

        Log.d(LOG_TAG, "got response: $response")

        return Wttr(response)
    }

    fun saveLocation(appWidgetId: Int, text: String) =
        context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
            .edit()
            .putString("$appWidgetId", text)
            .apply()

    fun loadLocation(appWidgetId: Int): String =
        context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE).getString("$appWidgetId", null) ?: ""

    fun deleteLocation(appWidgetId: Int) =
        context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE)
            .edit()
            .remove("$appWidgetId")
            .apply()

}

data class Wttr(val wttrData: String)