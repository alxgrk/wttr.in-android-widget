package de.alxgrk.wttrinwidget

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.util.Log
import coil.ImageLoader
import coil.api.get
import coil.transform.RoundedCornersTransformation

private const val LOG_TAG = "Repository"
private const val PREFS_NAME = "de.alxgrk.wttrinwidget.WttrInWidget"

@Suppress("DEPRECATION")
class WttrRepository(
    private val context: Context,
    private val imageLoader: ImageLoader = ImageLoader(context)
) {

    private val syncProblemImage: Drawable = context.resources.getDrawable(R.drawable.ic_sync_problem)

    suspend fun getWttrFor(location: String): Wttr {

        // check network state
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnected = connectivityManager.activeNetworkInfo?.isConnectedOrConnecting ?: false
        if (!isConnected) {
            Log.e(LOG_TAG, "no internet connection, aborting wttr retrieval")
            return Wttr(syncProblemImage)
        }


        val url = "https://wttr.in/${location}_0tqp.png"

        Log.d(LOG_TAG, "requesting $url")

        val imageResult = runCatching {
            imageLoader.get(url) {
                transformations(RoundedCornersTransformation(4f))
            }
        }

        if (imageResult.isFailure)
            Log.e(LOG_TAG, "could not retrieve wttr image, ${imageResult.exceptionOrNull()}")

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