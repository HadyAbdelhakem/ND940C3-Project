package com.udacity


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import kotlinx.android.synthetic.main.content_detail.view.*

class DetailActivity : AppCompatActivity() {

    companion object {
        const val file_name: String = "file_name"
        const val download_status: String = "download_status"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        updateUI(intent)
        button.setOnClickListener {
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        updateUI(intent)
    }

    private fun updateUI(intent: Intent?): Unit{
        val fileName = (intent?.extras?.get(file_name)) as String?
        val downloadStatus = (intent?.extras?.get(download_status)) as String?
        textViewFileName.text = fileName
        textViewDownloadStatus.text = downloadStatus
    }

}
