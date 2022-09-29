package com.livebarn.android.sree

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.livebarn.android.sreelibrary.Constants

class MainActivity : FragmentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var imageView: ImageView? = null
    private var textViewMessage: TextView? = null
    private var databaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.image_view_gif)
        textViewMessage = findViewById(R.id.text_view_message)
    }

    override fun onStart() {
        super.onStart()
        databaseReference = Firebase.database.getReference(Constants.DB_TABLE)
        databaseReference?.child(Constants.DB_PATH_MESSAGE)
            ?.addValueEventListener(messageValueListener)
        databaseReference?.child(Constants.DB_PATH_TARGET_GIF)
            ?.addValueEventListener(targetGifValueListener)
    }

    override fun onStop() {
        super.onStop()
        databaseReference?.child(Constants.DB_PATH_MESSAGE)
            ?.removeEventListener(messageValueListener)
        databaseReference?.child(Constants.DB_PATH_TARGET_GIF)
            ?.removeEventListener(targetGifValueListener)
        databaseReference = null
    }

    override fun onDestroy() {
        super.onDestroy()
        imageView = null
        textViewMessage = null
    }

    private val messageValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val message = snapshot.getValue(String::class.java) ?: "Congrats!"
            textViewMessage?.text = message
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() messageValueListener -> ${error.message}")
        }
    }

    private val targetGifValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val targetGif = snapshot.getValue(String::class.java) ?: Constants.claps["Joker"]
            imageView?.let {
                Glide.with(this@MainActivity)
                    .asGif()
                    .load(targetGif)
                    .into(it)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() targetGifValueListener -> ${error.message}")
        }
    }
}

