package com.livebarn.android.sree

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.livebarn.android.sreelibrary.Constants

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        handler = Handler(Looper.getMainLooper())

        imageView = findViewById(R.id.image_view_gif)
        textViewMessage = findViewById(R.id.text_view_message)
        confettiView = findViewById(R.id.confetti_view)

        val imageViewLiveBarn: ImageView = findViewById(R.id.image_view_livebarn)
        imageViewLiveBarn.post {
            Log.d("MainActivity", "screen width and height -> ${resources.displayMetrics.widthPixels} / ${resources.displayMetrics.heightPixels}")
            Log.d("MainActivity", "imageView width and height -> ${imageViewLiveBarn.width} / ${imageViewLiveBarn.height}")
        }
    }

    override fun setGif(url: String?) {
        imageView?.let {
            url?.let { targetUrl ->
                Glide.with(this)
                    .asGif()
                    .load(targetUrl)
                    .into(it)
            }
        }
    }
}

