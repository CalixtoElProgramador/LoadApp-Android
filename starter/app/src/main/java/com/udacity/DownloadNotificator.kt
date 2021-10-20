package com.udacity

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver

class DownloadNotificator(private val context: Context, private val lifecycle: Lifecycle) :
    LifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun notify(fileName: String, downloadStatus: DownloadStatus) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Toast.makeText(
                context,
                "Download completed",
                Toast.LENGTH_SHORT
            ).show();
        }
        with(context.applicationContext) {
            val notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.createStatusChannel(this)
            notificationManager.setNotification(fileName, downloadStatus, this.applicationContext)
        }
    }
}