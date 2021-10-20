package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.DownloadManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View.NO_ID
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.udacity.databinding.ActivityMainBinding
import com.udacity.databinding.ContentMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var downloadID: Long = 0
    private var fileSelected = ""

    private var downloadNotificator: DownloadNotificator? = null
    private var downloadContentObserver: ContentObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        registerReceiver(receiver, IntentFilter(ACTION_DOWNLOAD_COMPLETE))
        binding.contentMain.onCustomViewClicked()
    }

    @SuppressLint("ResourceAsColor")
    private fun ContentMainBinding.onCustomViewClicked() {
        customButton.setOnClickListener {
            when (radioGroup.checkedRadioButtonId) {
                NO_ID -> {
                    Toast.makeText(
                        this@MainActivity,
                        "Select an option, please",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    fileSelected =
                        findViewById<RadioButton>(radioGroup.checkedRadioButtonId).text.toString()
                    customButton.changeButtonState(ButtonState.Loading)
                    download()
                }
            }
        }
    }


    private val receiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val downloadStatus = downloadManager.queryStatus(it)
                downloadStatus.takeIf { status -> status != DownloadStatus.UNKNOWN }?.run {
                    getDownloadNotificator().notify(fileSelected, downloadStatus)
                }
            }
        }
    }

    private fun getDownloadNotificator(): DownloadNotificator = when (downloadNotificator) {
        null -> DownloadNotificator(this, lifecycle).also { downloadNotificator = it }
        else -> downloadNotificator!!
    }

    @SuppressLint("Range")
    private fun DownloadManager.queryStatus(id: Long): DownloadStatus {
        query(Query().setFilterById(id)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    return when (getInt(getColumnIndex(COLUMN_STATUS))) {
                        STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                        STATUS_FAILED -> DownloadStatus.ERROR
                        else -> DownloadStatus.UNKNOWN
                    }
                }
                return DownloadStatus.UNKNOWN
            }
        }
    }

    private fun download() {
        val request = Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)
        downloadManager.observeContentDownloading()
    }

    private fun DownloadManager.observeContentDownloading() {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadContentObserver?.run { getProgress() }
            }
        }.also {
            downloadContentObserver = it
            contentResolver.registerContentObserver(
                "content://downloads/my_downloads".toUri(),
                true,
                downloadContentObserver!!
            )
        }
    }

    @SuppressLint("Range")
    private fun DownloadManager.getProgress() {
        query(Query().setFilterById(downloadID)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    when (getInt(getColumnIndex(COLUMN_STATUS))) {
                        STATUS_FAILED -> {
                            binding.contentMain.customButton.changeButtonState(ButtonState.Completed)
                        }
                        STATUS_RUNNING -> {
                            binding.contentMain.customButton.changeButtonState(ButtonState.Loading)
                        }
                        STATUS_SUCCESSFUL -> {
                            binding.contentMain.customButton.changeButtonState(ButtonState.Completed)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
    }

}
