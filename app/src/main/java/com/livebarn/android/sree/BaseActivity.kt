package com.livebarn.android.sree

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.livebarn.android.sreelibrary.Constants

abstract class BaseActivity : FragmentActivity() {

    companion object {
        private const val TAG = "BaseActivity"
        private val DEFAULT_GIF = Constants.claps["Standing Ovation"]
    }

    var imageView: ImageView? = null
    var textViewMessage: TextView? = null
    var databaseReference: DatabaseReference? = null

    override fun onStart() {
        super.onStart()
        databaseReference = Firebase.database.getReference(Constants.DB_TABLE)
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

    val messageValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val message = snapshot.getValue(String::class.java)
            if (message.isNullOrEmpty())
                textViewMessage?.setText(R.string.default_congrats_message)
            else
                textViewMessage?.text = message
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() messageValueListener -> ${error.message}")
            textViewMessage?.setText(R.string.default_congrats_message)
        }
    }

    val locationValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val location = snapshot.getValue(String::class.java)
            val params = textViewMessage?.layoutParams as? ConstraintLayout.LayoutParams
            when (location) {
                Constants.DB_VALUE_TOP -> {
                    params?.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    params?.bottomToBottom = -1
                }
                Constants.DB_VALUE_CENTER -> {
                    params?.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    params?.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
                else -> {
                    params?.topToTop = -1
                    params?.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
            }
            textViewMessage?.layoutParams = params
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() messageValueListener -> ${error.message}")
            textViewMessage?.setText(R.string.default_congrats_message)
        }
    }

    val targetGifValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var targetGif = snapshot.getValue(String::class.java)
            if (targetGif.isNullOrEmpty()) {
                targetGif = DEFAULT_GIF
            }

            setGif(targetGif)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() targetGifValueListener -> ${error.message}")
            setGif(DEFAULT_GIF)
        }
    }

    abstract fun setGif(url: String?)
}