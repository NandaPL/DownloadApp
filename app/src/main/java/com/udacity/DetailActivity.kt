package com.udacity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.databinding.ContentDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivityDetailBinding
    private lateinit var dataBindingContent: ContentDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivityDetailBinding.inflate(layoutInflater)
        dataBindingContent = dataBinding.contentDetail

        val fileNameKey = intent.getStringExtra(Constants.FILE_NAME_KEY)
        val statusKey = intent.getStringExtra(Constants.STATUS_KEY)

        dataBindingContent.tvFileName.text = fileNameKey
        dataBindingContent.tvStatus.text = statusKey

        setContentView(dataBinding.root)
        setSupportActionBar(dataBinding.toolbar)

        dataBindingContent.tvStatus.setTextColor(
            when (statusKey) {
                getString(R.string.failed_status) -> Color.RED
                else -> Color.GREEN
            }
        )

        dataBindingContent.btnOk.setOnClickListener {
            finish()
        }
    }
}
