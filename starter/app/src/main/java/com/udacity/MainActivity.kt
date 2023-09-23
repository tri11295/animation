package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.Notification.Action
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var downloadID: Long? = null
    var fileName = ""
    var description = ""
    var status = ""
    var url = ""

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            handleNotificationPermissionResult()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.btnLoading.setOnClickListener {
            downloadFile()
        }
        binding.rgDownload.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbGlide -> {
                    fileName = getString(R.string.file_glide)
                    description = GLIDE_DES
                    status = "Success"
                    url = URL_GLIDE
                }

                R.id.rbLoadApp -> {
                    fileName = getString(R.string.file_load_app)
                    description = LOAD_APP_DES
                    status = "Fail"
                    url = URL_LOAD_APP
                }

                R.id.rbRetrofit -> {
                    fileName = getString(R.string.file_retrofit)
                    description = RETROFIT_DES
                    status = "Fail"
                    url = URL_RETROFIT
                }
            }
        }
        setContentView(binding.root)
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            downloadID = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            downloadID?.let {
                sendNotification(it.toInt())
                binding.btnLoading.finishDownloading {
                    Toast.makeText(this@MainActivity, "Finish Download", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleNotificationPermissionResult() {

    }

    private fun downloadFile() {
        binding.run {
            if (rbGlide.isChecked || rbLoadApp.isChecked || rbRetrofit.isChecked) {
                download(url)
                binding.btnLoading.startDownload()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Please select the file to download",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "description"

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(
        notificationId: Int
    ) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("key_file", fileName)
            putExtra("key_status", status)
            putExtra("key_description", description)
        }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.download)
            .setContentTitle("Project: Design an App with Application Loading Status")
            .setContentText("Finish Downloaded")
            .addAction(R.drawable.detail2, "Click here to go to detail screen", pendingIntent)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(
            notificationId,
            builder.build()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    companion object {
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_LOAD_APP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "channelName"

        private const val LOAD_APP_DES =
            "In this project students will create an app to download a file from Internet by clicking on a custom-built button where:width of the button gets animated from left to right;text gets changed based on different states of the button;circle gets be animated from 0 to 360 degreesA notification will be sent once the download is complete. When a user clicks on notification, the user lands on detail activity and the notification gets dismissed. In detail activity, the status of the download will be displayed and animated via MotionLayout upon opening the activity."
        private const val RETROFIT_DES =
            "RETROFIT - Copyright 2013 Square, Inc.Licensed under the Apache License, Version 2.0 (the \"License\");you may not use this file except in compliance with the License.You may obtain a copy of the License athttp://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, softwaredistributed under the License is distributed on an \"AS IS\" BASIS,WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.See the License for the specific language governing permissions andlimitations under the License."
        private const val GLIDE_DES =
            "Glide is a fast and efficient open source media management and image loading framework for Android that wraps media decoding, memory and disk caching, and resource pooling into a simple and easy to use interfaceGlide supports fetching, decoding, and displaying video stills, images, and animated GIFs. Glide includes a flexible API that allows developers to plug in to almost any network stack. By default Glide uses a custom HttpUrlConnection based stack, but also includes utility libraries plug in to Google's Volley project or Square's OkHttp library instead.\n" + "\n" + "Glide's primary focus is on making scrolling any kind of a list of images as smooth and fast as possible, but Glide is also effective for almost any case where you need to fetch, resize, and display a remote image."
    }


}