package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding
import com.udacity.databinding.ContentMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivityMainBinding

    private var downloadID: Long = 0

    private lateinit var contentMainBinding: ContentMainBinding
    private lateinit var url: String

    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

        contentMainBinding = dataBinding.contentMain
        setSupportActionBar(dataBinding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        onDownloadOptionsClicked()

        contentMainBinding.btnDownload.setOnClickListener {
            if (::url.isInitialized) {
                contentMainBinding.btnDownload.buttonState = ButtonState.Loading
                download()
            } else Toast.makeText(
                this@MainActivity,
                getString(R.string.you_should_select_from_list),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onDownloadOptionsClicked() {
        dataBinding.contentMain.rgFileOptions.setOnCheckedChangeListener { _, index ->
            when (index) {
                R.id.btnGlide -> {
                    url = Constants.GLIDE_URL
                    notificationHelper =
                        NotificationHelper(this, getString(R.string.glide))
                }

                R.id.btnLoadApp -> {
                    url = Constants.UDACITY_URL
                    notificationHelper =
                        NotificationHelper(this, getString(R.string.load_app))
                }

                R.id.btnRetrofit -> {
                    url = Constants.RETROFIT_URL
                    notificationHelper =
                        NotificationHelper(this, getString(R.string.retrofit))
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val intentId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (intentId == -1L) return
            intentId?.let { id ->
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query()
                query.setFilterById(id)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val downloadStatus =
                        if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(index)) getString(R.string.success_status)
                        else getString(R.string.failed_status)
                    notificationHelper.sendNotification(downloadStatus)
                    contentMainBinding.btnDownload.buttonState = ButtonState.Completed
                }
            }
        }
    }

    private fun download() {
        val request = DownloadManager.Request(Uri.parse(url)).setTitle(getString(R.string.app_name))
            .setDescription(String.format(getString(R.string.app_description), url))
            .setRequiresCharging(false).setAllowedOverMetered(true).setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        // enqueue puts the download request in the queue.
        downloadID = downloadManager.enqueue(request)
    }

}