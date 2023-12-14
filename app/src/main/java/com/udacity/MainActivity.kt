package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.udacity.databinding.ActivityMainBinding
import com.udacity.databinding.ContentMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivityMainBinding
    private var downloadID: Long = 0

    private lateinit var contentMainBinding: ContentMainBinding
    private lateinit var url: String

    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasNotificationPermission()) {
            // Permission already granted
            showToast(getString(R.string.message_permission_granted))
        } else {
            requestNotificationPermission()
        }

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
            } else showToast(getString(R.string.you_should_select_from_list))
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

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (hasNotificationPermission()) {
                showToast(getString(R.string.message_permission_granted))
            } else {
                showToast(getString(R.string.message_permission_denied))
            }
        }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android Ore (API 26) and higher versions
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.areNotificationsEnabled()
        } else {
            // For versions prior to Oreo
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android Ore (API 26) and higher versions
            val intent = Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            notificationPermissionLauncher.launch(intent)
        } else {
            // For versions prior to Oreo
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            notificationPermissionLauncher.launch(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            this@MainActivity,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

}