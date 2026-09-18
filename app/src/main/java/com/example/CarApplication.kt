package com.example

import android.app.Application
import android.util.Log

class CarApplication : Application() {
    companion object {
        private const val TAG = "CarApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Initializing CarApplication")

        // Catch unhandled background exceptions to log diagnostic info and protect from non-fatal crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception on thread ${thread.name}: ${throwable.message}", throwable)
            // Prevent non-critical background worker exceptions (I/O, OOM on thumbnail decoding, IllegalState) from abruptly killing the car UI
            if (thread.name != "main" && (
                throwable is OutOfMemoryError ||
                throwable is java.io.IOException ||
                throwable is IllegalStateException ||
                throwable is SecurityException ||
                throwable is ConcurrentModificationException ||
                throwable.message?.contains("MediaMetadataRetriever", ignoreCase = true) == true
            )) {
                Log.w(TAG, "Gracefully absorbed non-fatal background thread crash: ${throwable.message}")
                return@setDefaultUncaughtExceptionHandler
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
