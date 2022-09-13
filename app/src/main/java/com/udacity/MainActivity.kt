package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.renderscript.RenderScript
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.DataInput
import java.io.File
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    /*private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action*/
    private lateinit var URL: String
    private lateinit var downloadStatus: String
    private lateinit var filename: String

    private val storagePermission = 113

    lateinit var loadingButton: LoadingButton
    private var complete = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, storagePermission)
        createChannel(CHANNEL_ID, CHANNEL_NAME)

        loadingButton = findViewById(R.id.custom_button)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.radio1) {
                URL = GLIDE_URL
                filename = getString(R.string.glide_image_loading_library_by_bump_tech)
            }
            if (checkedId == R.id.radio2) {
                URL = LOAD_APP_URL
                filename = getString(R.string.loadapp_current_repsitory_by_udacity)
            }
            if (checkedId == R.id.radio3) {
                URL = RETROFIT_URL
                filename =
                    getString(R.string.retrofit_type_safe_http_client_for_for_android_and_java_by_square_inc)
            }

        }

        loadingButton.setOnClickListener {
            if (radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Please select the file to download", Toast.LENGTH_LONG).show()
                loadingButton.hasCompletedDownload()
            } else {
                download(URL, filename)
            }
            complete = true
        }
    }

    private fun download(URL: String, fileName: String) {

        try {
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val fileLink = Uri.parse(URL)
            val request = DownloadManager.Request(fileLink)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "file")
            downloadManager.enqueue(request)
            downloadID = downloadManager.enqueue(request)
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    var id: Long? = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadID) {
                        val cursor =
                            downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
                        if (cursor.moveToFirst()) {
                            val status =
                                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                downloadStatus = "Success"
                            } else {
                                downloadStatus = "Fail"
                            }
                        }
                        createNotification(
                            "Udacity: Android Kotlin Nanodegree",
                            "The Project 3 repository is downloaded",
                            CHANNEL_ID,
                            NotificationCompat.PRIORITY_DEFAULT,
                            100,
                            fileName,
                            downloadStatus
                        )
                        if (complete) {
                            loadingButton.hasCompletedDownload()
                        }
                    }
                }

            }
            registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))


        } catch (e: Exception) {
            Toast.makeText(this, "Failed Download File", Toast.LENGTH_LONG).show()
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(false)
            }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download Notification"

            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
//            Toast.makeText(this , "Permission Granted", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == storagePermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this , "Storage Permission Granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun createNotification(
        title: String,
        content: String,
        channelId: String,
        priority: Int,
        notificationID: Int,
        filename: String,
        downloadStatus: String

    ) {
        val intent = Intent(applicationContext, DetailActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val extras = Bundle()
        extras.putString(DetailActivity.file_name, filename)
        extras.putString(DetailActivity.download_status, downloadStatus)
        intent.putExtras(extras)
        intent.action = Intent.ACTION_VIEW

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                notificationID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources, R.drawable.ic_cloud_download
                )
            )
            .setSmallIcon(R.drawable.ic_cloud_download)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_cloud_download, getString(R.string.check_status),
                pendingIntent
            )

        with(NotificationManagerCompat.from(applicationContext)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelId,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                createChannel(CHANNEL_ID, CHANNEL_NAME)
            }
            notify(notificationID, builder.build())
        }

    }

    fun playNotificationSound(context: Context) {
        try {
            val defaultSoundUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, defaultSoundUri)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val GLIDE_URL =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val LOAD_APP_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/refs/heads/master.zip"
        private const val RETROFIT_URL =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "channelName"
    }

}
