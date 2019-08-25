package de.alxgrk.wttrinwidget

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import coil.ImageLoader
import coil.api.get
import coil.transform.RoundedCornersTransformation

private const val LOG_TAG = "Repository"
private const val PREFS_NAME = "de.alxgrk.wttrinwidget.WttrInWidget"

class WttrRepository(
    private val context: Context,
    private val imageLoader: ImageLoader = ImageLoader(context)
) {

    suspend fun getWttrFor(location: String): Wttr {
        val url = "https://wttr.in/${location}_0tqp.png"

        Log.d(LOG_TAG, "requesting $url")

        // TODO test for exceptions
        val image = imageLoader.get(url) {
            transformations(RoundedCornersTransformation(4f))
        }

        Log.d(LOG_TAG, "got image")

        return Wttr(image)
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

data class Wttr(val wttrImage: Drawable)