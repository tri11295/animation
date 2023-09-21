package com.udacity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        val intent = intent
        val fileName = intent.getStringExtra("key_file")
        val status = intent.getStringExtra("key_status")
        val description = intent.getStringExtra("key_description")
        binding.tvFileName.text = fileName
        binding.tvStatus.text = status
        binding.tvDes.text = description
        binding.btnOK.setOnClickListener {
            onBackPressed()
        }
        val listener = AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val seekPosition = -verticalOffset / binding.appbarLayout.totalScrollRange.toFloat()
            binding.motionLayout.progress = seekPosition
        }
        binding.appbarLayout.addOnOffsetChangedListener(listener)

        setContentView(binding.root)
    }
}
