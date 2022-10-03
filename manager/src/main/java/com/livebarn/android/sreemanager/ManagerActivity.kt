package com.livebarn.android.sreemanager

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.livebarn.android.sreelibrary.Constants
import com.livebarn.android.sreelibrary.model.CelebrationType

class ManagerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ManagerActivity"
        private val DEFAULT_GIF = Constants.claps["Standing Ovation"]
    }

    private var messageDBReference: DatabaseReference? = null
    private var targetGifDBReference: DatabaseReference? = null
    private var gifsDBReference: DatabaseReference? = null
    private var typeDBReference: DatabaseReference? = null

    private var editTextMessage: EditText? = null
    private var imageViewSelectedGif: ImageView? = null
    private var textViewMessageLength: TextView? = null
    private var spinnerCelebrationType: Spinner? = null
    private var recyclerViewGifs: RecyclerView? = null

    private var oldPosition = 0
    private val gifList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)

        initViews()
    }

    private fun initViews() {
        editTextMessage = findViewById(R.id.edit_text_message)
        imageViewSelectedGif = findViewById(R.id.image_view_selected_gif)
        textViewMessageLength = findViewById(R.id.text_view_message_length)
        spinnerCelebrationType = findViewById(R.id.spinner_celebration_type)
        recyclerViewGifs = findViewById(R.id.recycler_view_gifs)

        findViewById<ImageButton>(R.id.image_button_clear).setOnClickListener {
            editTextMessage?.setText("")
        }

        findViewById<Button>(R.id.button_save).setOnClickListener {
            messageDBReference?.setValue(editTextMessage?.text.toString())
        }

        editTextMessage?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun afterTextChanged(s: Editable?) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textViewMessageLength?.text =
                    String.format("Length: %d", editTextMessage?.length() ?: 0)
            }
        })

        val types = CelebrationType.values().map { it.title }
        spinnerCelebrationType?.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        spinnerCelebrationType?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (oldPosition != position) {
                    oldPosition = position
                    val type = CelebrationType.findById(position)
                    Log.d(TAG, "onItemSelected(position: $position, ${type.title})")
                    typeDBReference?.setValue(type.id)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e(TAG, "onNothingSelected()")
            }
        }

        recyclerViewGifs?.layoutManager = LinearLayoutManager(this)
        recyclerViewGifs?.adapter = GifAdapter(gifList)
    }

    override fun onStart() {
        super.onStart()
        bindDatabase()
    }

    override fun onStop() {
        super.onStop()
        releaseDatabase()
    }

    override fun onDestroy() {
        super.onDestroy()
        editTextMessage = null
        imageViewSelectedGif = null
        textViewMessageLength = null
        spinnerCelebrationType = null
        recyclerViewGifs = null
    }

    private fun bindDatabase() {
        val databaseReference = Firebase.database.getReference(Constants.DB_TABLE)
        messageDBReference = databaseReference.child(Constants.DB_PATH_MESSAGE)
        targetGifDBReference = databaseReference.child(Constants.DB_PATH_TARGET_GIF)
        gifsDBReference = databaseReference.child(Constants.DB_PATH_GIFS)
        typeDBReference = databaseReference.child(Constants.DB_PATH_TYPE_ID)

        messageDBReference?.addValueEventListener(messageValueListener)
        targetGifDBReference?.addValueEventListener(targetGifValueListener)
        gifsDBReference?.addValueEventListener(gifsValueListener)
        typeDBReference?.addValueEventListener(typeValueListener)
    }

    private fun releaseDatabase() {
        messageDBReference?.removeEventListener(messageValueListener)
        targetGifDBReference?.removeEventListener(targetGifValueListener)
        gifsDBReference?.removeEventListener(gifsValueListener)
        typeDBReference?.removeEventListener(typeValueListener)

        messageDBReference = null
        targetGifDBReference = null
        gifsDBReference = null
        typeDBReference = null
    }

    private val messageValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val message = snapshot.getValue(String::class.java) ?: "Congrats!"
            editTextMessage?.setText(message)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() messageValueListener -> ${error.message}")
        }
    }

    private val targetGifValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val targetGif = snapshot.getValue(String::class.java)
            imageViewSelectedGif?.let { view ->
                Glide.with(this@ManagerActivity)
                    .asGif()
                    .load(
                        if (targetGif.isNullOrEmpty())
                            DEFAULT_GIF
                        else
                            targetGif
                    )
                    .into(view)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() targetGifValueListener -> ${error.message}")
        }
    }

    private val gifsValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() targetGifValueListener -> ${error.message}")
        }
    }

    private val typeValueListener = object : ValueEventListener {

        @SuppressLint("NotifyDataSetChanged")
        override fun onDataChange(snapshot: DataSnapshot) {
            snapshot.getValue(Int::class.java)?.let {
                spinnerCelebrationType?.setSelection(it)
                val type = CelebrationType.findById(it)
                gifList.clear()
                when (type) {
                    CelebrationType.HAPPY_B_DAY -> gifList.addAll(Constants.happyBDay.values)
                    else ->  gifList.addAll(Constants.claps.values)
                }
                recyclerViewGifs?.adapter?.notifyDataSetChanged()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() targetGifValueListener -> ${error.message}")
        }
    }

    class GifAdapter(
        private val gifs: MutableList<String>
    ) : RecyclerView.Adapter<GifAdapter.GifViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifViewHolder {
            val imageView = ImageView(parent.context)
            imageView.adjustViewBounds = true

            return GifViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: GifViewHolder, position: Int) {
            val gif = try {
                gifs[position]
            } catch (e: Exception) {
                null
            }
            holder.imageView?.let { view ->
                Glide.with(view)
                    .asGif()
                    .load(
                        if (gif.isNullOrEmpty()) DEFAULT_GIF else gif
                    )
                    .into(view)
            }
        }

        override fun getItemCount() = gifs.size

        class GifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imageView = itemView as? ImageView
        }
    }
}