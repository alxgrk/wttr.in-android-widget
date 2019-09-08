package de.alxgrk.wttrinwidget

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.util.Log
import coil.ImageLoader
import coil.api.get
import coil.transform.RoundedCornersTransformation
import java.util.concurrent.CancellationException

private const val LOG_TAG = "Repository"
private const val PREFS_NAME = "de.alxgrk.wttrinwidget.WttrInWidget"

@Suppress("DEPRECATION")
class WttrRepository(
    private val context: Context,
    private val imageLoader: ImageLoader = ImageLoader(context)
) {

    private val syncProblemImage: Drawable = context.resources.getDrawable(R.drawable.ic_sync_problem)

    suspend fun getWttrFor(location: String, forecastLevel: ForecastLevel = ForecastLevel.ZERO): Wttr {

        // check network state
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnected = connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
        if (!isConnected) {
            Log.e(LOG_TAG, "no internet connection, aborting wttr retrieval")
            return Wttr(syncProblemImage)
        }

        val url = "https://wttr.in/${location}_${forecastLevel.asNumber}tqp.png"

        Log.d(LOG_TAG, "requesting $url")

        val imageResult = runCatching {
            imageLoader.get(url) {
                transformations(RoundedCornersTransformation(4f))
            }
        }

        if (imageResult.isFailure) {
            val e = imageResult.exceptionOrNull()

            Log.e(LOG_TAG, "could not retrieve wttr image, $e")

            if (e != null && e is CancellationException)
                throw e
        }

        val image = imageResult.getOrDefault(syncProblemImage)

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

enum class ForecastLevel(val asNumber: Int) {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3)
}