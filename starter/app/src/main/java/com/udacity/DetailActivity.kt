package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.databinding.ContentDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val fileName by lazy { intent?.extras?.getString(ARG_FILE_NAME, "") }
    private val fileStatus by lazy { intent?.extras?.getString(ARG_FILE_STATUS, "") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.contentDetail.setData()

    }

    private fun ContentDetailBinding.setData() {
        fileNameText.text = fileName
        downloadStatusText.text = fileStatus
        when (fileStatus) {
            DownloadStatus.SUCCESSFUL.status -> {
                downloadStatusImage.setImageResource(R.drawable.ic_check_circle)
                downloadStatusImage.imageTintList = getColorStateList(R.color.colorPrimary)
            }
            DownloadStatus.ERROR.status -> {
                downloadStatusImage.setImageResource(R.drawable.ic_error)
                downloadStatusImage.imageTintList = getColorStateList(R.color.design_default_color_error)
            }
        }
        okButton.setOnClickListener {
            val intent = Intent(this@DetailActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val ARG_FILE_NAME = "FILE_NAME"
        private const val ARG_FILE_STATUS = "FILE_STATUS"

        fun detailsArgs(fileName: String, fileStatus: String) =
            bundleOf(ARG_FILE_NAME to fileName, ARG_FILE_STATUS to fileStatus)
    }


}
