package com.livebarn.android.sree

import android.os.Handler
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
import com.livebarn.android.sree.util.ConfettiManager
import com.livebarn.android.sreelibrary.Constants
import nl.dionsegijn.konfetti.xml.KonfettiView
import kotlin.random.Random

abstract class BaseActivity : FragmentActivity() {

    companion object {
        private const val TAG = "BaseActivity"
        private val DEFAULT_GIF = Constants.claps["Standing Ovation"]
    }

    var imageView: ImageView? = null
    var textViewMessage: TextView? = null
    var databaseReference: DatabaseReference? = null
    var confettiView: KonfettiView? = null

    var handler: Handler? = null

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
        handler?.removeCallbacks(runnableConfetti)
        handler = null
        imageView = null
        textViewMessage = null
    }

    private fun showConfetti() {
        for (i in 1..4) {
            val x = if (i % 2 == 1) Random.nextDouble(0.0, 0.5)
            else Random.nextDouble(0.5, 1.0)
            val y = Random.nextDouble(0.0, 1.0)
            handler?.postDelayed({
                confettiView?.start(ConfettiManager.explode(x, y))
            }, i * 200L)
        }

        handler?.removeCallbacks(runnableConfetti)
        handler?.postDelayed(runnableConfetti, 5000L)
    }

    private val runnableConfetti = Runnable {
        showConfetti()
    }

    private val messageValueListener = object : ValueEventListener {
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

    private val locationValueListener = object : ValueEventListener {
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

    private val targetGifValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var targetGif = snapshot.getValue(String::class.java)
            if (targetGif.isNullOrEmpty()) {
                targetGif = DEFAULT_GIF
            }

            setGif(targetGif)
            showConfetti()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() targetGifValueListener -> ${error.message}")
            setGif(DEFAULT_GIF)
        }
    }

    abstract fun setGif(url: String?)

}