package com.livebarn.android.sree

import android.os.Bundle
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.livebarn.android.sreelibrary.Constants

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        imageView = findViewById(R.id.image_view_gif)
        textViewMessage = findViewById(R.id.text_view_message)
    }

    override fun onStart() {
        super.onStart()
        databaseReference = Firebase.database.getReference(Constants.DB_NAME)
        databaseReference?.child(Constants.DB_PATH_MESSAGE)
            ?.addValueEventListener(messageValueListener)
        databaseReference?.child(Constants.DB_PATH_TARGET_GIF)
            ?.addValueEventListener(targetGifValueListener)
        databaseReference?.child(Constants.DB_PATH_LOCATION)
            ?.addValueEventListener(locationValueListener)
    }

    override fun onStop() {
        super.onStop()
        databaseReference?.child(Constants.DB_PATH_MESSAGE)
            ?.removeEventListener(messageValueListener)
        databaseReference?.child(Constants.DB_PATH_TARGET_GIF)
            ?.removeEventListener(targetGifValueListener)
        databaseReference?.child(Constants.DB_PATH_LOCATION)
            ?.removeEventListener(locationValueListener)
        databaseReference = null
    }

    override fun onDestroy() {
        super.onDestroy()
        imageView = null
        textViewMessage = null
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

