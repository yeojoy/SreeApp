package com.livebarn.android.sreemanager

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.livebarn.android.sreelibrary.hideKeyboard
import com.livebarn.android.sreelibrary.model.CelebrationType
import com.livebarn.android.sreemanager.app.ManagerApplication
import com.livebarn.android.sreemanager.auth.LoginActivity

class ManagerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ManagerActivity"
        private val DEFAULT_GIF = Constants.claps["Standing Ovation"]
    }

    private val clapsGifList = mutableListOf<String>()
    private val happyBDayGifList = mutableListOf<String>()

    private var messageDBReference: DatabaseReference? = null
    private var targetGifDBReference: DatabaseReference? = null
    private var gifsDBReference: DatabaseReference? = null
    private var alignDBReference: DatabaseReference? = null

    private var imageButtonAlignTop: ImageView? = null
    private var imageButtonAlignCenter: ImageView? = null
    private var imageButtonAlignBotoom: ImageView? = null

    private var editTextMessage: EditText? = null
    private var imageViewSelectedGif: ImageView? = null
    private var textViewDummyMessage: TextView? = null
    private var textViewMessageLength: TextView? = null
    private var spinnerCelebrationType: Spinner? = null
    private var recyclerViewGifs: RecyclerView? = null

    private var oldPosition = 0
    private val gifList = mutableListOf<String>()
    private var currentType = CelebrationType.NONE

    private val buttonBackgroundArray = arrayOf(
        com.livebarn.android.sreelibrary.R.drawable.rect_bg_button,
        com.livebarn.android.sreelibrary.R.drawable.rect_bg_button_selected
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)

        initViews()

        val auth = (application as ManagerApplication).auth
        auth?.let {
            if (it.currentUser == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                return
            }
        }
    }

    private fun initViews() {
        editTextMessage = findViewById(R.id.edit_text_message)
        imageViewSelectedGif = findViewById(R.id.image_view_selected_gif)
        textViewMessageLength = findViewById(R.id.text_view_message_length)
        spinnerCelebrationType = findViewById(R.id.spinner_celebration_type)

        imageButtonAlignTop = findViewById(R.id.image_button_align_top)
        imageButtonAlignCenter = findViewById(R.id.image_button_align_center)
        imageButtonAlignBotoom = findViewById(R.id.image_button_align_bottom)

        textViewDummyMessage = findViewById(R.id.text_view_dummy_message)

        recyclerViewGifs = findViewById(R.id.recycler_view_gifs)
        findViewById<ImageButton>(R.id.image_button_clear).setOnClickListener {
            editTextMessage?.setText("")
        }

        findViewById<Button>(R.id.button_save).setOnClickListener {
            val message = editTextMessage?.text.toString()
            messageDBReference?.setValue(message)
            editTextMessage?.setSelection(message.length)
            hideKeyboard()
        }

        editTextMessage?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun afterTextChanged(s: Editable?) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textViewMessageLength?.text =
                    getString(com.livebarn.android.sreelibrary.R.string.label_message_length, editTextMessage?.length() ?: 0)
            }
        })

        // TODO bind imagebutton event
        imageButtonAlignTop?.setOnClickListener {
            alignDBReference?.setValue(Constants.DB_VALUE_TOP)
        }
        imageButtonAlignCenter?.setOnClickListener {
            alignDBReference?.setValue(Constants.DB_VALUE_CENTER)
        }
        imageButtonAlignBotoom?.setOnClickListener {
            alignDBReference?.setValue(Constants.DB_VALUE_BOTTOM)
        }

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
                    currentType = CelebrationType.findById(position)
                    Log.d(TAG, "onItemSelected(position: $position, ${currentType.title})")
                    updateGifList()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e(TAG, "onNothingSelected()")
            }
        }

        recyclerViewGifs?.layoutManager = LinearLayoutManager(this)
        recyclerViewGifs?.adapter = GifAdapter(gifList, this::onGifSelected)
    }

    override fun onStart() {
        super.onStart()
        bindDatabase()
    }

    override fun onBackPressed() {
        super.onBackPressed()
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
        imageButtonAlignTop = null
        imageButtonAlignCenter = null
        imageButtonAlignBotoom = null
    }

    private fun onGifSelected(position: Int) {
        updateTargetGif(
            try {
                gifList[position]
            } catch (e: Exception) {
                null
            }
        )
    }

    private fun updateTargetGif(url: String?) {
        url?.let {
            targetGifDBReference?.setValue(url)
        }
    }

    private fun bindDatabase() {
        val databaseReference = Firebase.database.getReference(Constants.DB_NAME)
        messageDBReference = databaseReference.child(Constants.DB_PATH_MESSAGE)
        targetGifDBReference = databaseReference.child(Constants.DB_PATH_TARGET_GIF)
        gifsDBReference = databaseReference.child(Constants.DB_PATH_GIFS)
        alignDBReference = databaseReference.child(Constants.DB_PATH_LOCATION)

        messageDBReference?.addValueEventListener(messageValueListener)
        targetGifDBReference?.addValueEventListener(targetGifValueListener)
        gifsDBReference?.addValueEventListener(gifsValueListener)
        alignDBReference?.addValueEventListener(alignValueListener)
    }

    private fun releaseDatabase() {
        messageDBReference?.removeEventListener(messageValueListener)
        targetGifDBReference?.removeEventListener(targetGifValueListener)
        gifsDBReference?.removeEventListener(gifsValueListener)

        messageDBReference = null
        targetGifDBReference = null
        gifsDBReference = null
    }

    private val messageValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val message = snapshot.getValue(String::class.java)
                ?: getString(R.string.default_congrats_message)
            editTextMessage?.setText(message)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() messageValueListener -> ${error.message}")
        }
    }

    private val alignValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val textViewLayoutParams = textViewDummyMessage?.layoutParams as? ConstraintLayout.LayoutParams
            when (snapshot.getValue(String::class.java)) {
                Constants.DB_VALUE_TOP -> {
                    imageButtonAlignTop?.setBackgroundResource(buttonBackgroundArray[1])
                    imageButtonAlignCenter?.setBackgroundResource(buttonBackgroundArray[0])
                    imageButtonAlignBotoom?.setBackgroundResource(buttonBackgroundArray[0])
                    textViewLayoutParams?.topToTop = R.id.image_view_selected_gif
                    textViewLayoutParams?.bottomToBottom = -1
                }
                Constants.DB_VALUE_CENTER -> {
                    imageButtonAlignTop?.setBackgroundResource(buttonBackgroundArray[0])
                    imageButtonAlignCenter?.setBackgroundResource(buttonBackgroundArray[1])
                    imageButtonAlignBotoom?.setBackgroundResource(buttonBackgroundArray[0])
                    textViewLayoutParams?.topToTop = R.id.image_view_selected_gif
                    textViewLayoutParams?.bottomToBottom = R.id.image_view_selected_gif
                }
                else -> {
                    imageButtonAlignTop?.setBackgroundResource(buttonBackgroundArray[0])
                    imageButtonAlignCenter?.setBackgroundResource(buttonBackgroundArray[0])
                    imageButtonAlignBotoom?.setBackgroundResource(buttonBackgroundArray[1])
                    textViewLayoutParams?.topToTop = -1
                    textViewLayoutParams?.bottomToBottom = R.id.image_view_selected_gif
                }
            }
            textViewDummyMessage?.layoutParams = textViewLayoutParams
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() locationValueListener -> ${error.message}")
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
            if (snapshot.hasChild(Constants.DB_PATH_GIF_CLAPS).not()
                || snapshot.child(Constants.DB_PATH_GIF_CLAPS).hasChildren().not()) {
                // TODO insert data
                insertClapsGifs()
                insertHappyBDayGifs()
            } else {
                clapsGifList.clear()
                for (url in snapshot.child(Constants.DB_PATH_GIF_CLAPS).children) {
                    (url.value as? String)?.let {
                        clapsGifList.add(it)
                    }
                }
            }

            if (snapshot.hasChild(Constants.DB_PATH_GIF_HAPPY_BDAY)
                && snapshot.child(Constants.DB_PATH_GIF_HAPPY_BDAY).hasChildren()) {
                happyBDayGifList.clear()
                for (url in snapshot.child(Constants.DB_PATH_GIF_HAPPY_BDAY).children) {
                    (url.value as? String)?.let {
                        happyBDayGifList.add(it)
                    }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() targetGifValueListener -> ${error.message}")
        }
    }

    private fun insertHappyBDayGifs() {
        gifsDBReference?.child(Constants.DB_PATH_GIF_HAPPY_BDAY)?.let {
            for (url in Constants.happyBDay.values) {
                it.push().setValue(url)
            }
        }
    }

    private fun insertClapsGifs() {
        gifsDBReference?.child(Constants.DB_PATH_GIF_CLAPS)?.let {
            for (url in Constants.claps.values) {
                it.push().setValue(url)
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun updateGifList() {
        gifList.clear()
        when (currentType) {
            CelebrationType.HAPPY_B_DAY -> gifList.addAll(happyBDayGifList)
            CelebrationType.ANNIVERSARY -> gifList.addAll(clapsGifList)
            CelebrationType.NEW_PEOPLE -> gifList.addAll(clapsGifList)
            else -> {

            }
        }
        recyclerViewGifs?.adapter?.notifyDataSetChanged()
    }

    class GifAdapter(
        private val gifs: MutableList<String>,
        private var itemSelectListener: ((position: Int) -> Unit)? = null
    ) : RecyclerView.Adapter<GifAdapter.GifViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifViewHolder {
            val imageView = ImageView(parent.context)
            imageView.adjustViewBounds = true

            return GifViewHolder(imageView, itemSelectListener)
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

        class GifViewHolder(
            itemView: View,
            private var onClickListener: ((position: Int) -> Unit)?
        ) : RecyclerView.ViewHolder(itemView) {
            var imageView = itemView as? ImageView

            init {
                itemView.setOnClickListener {
                    onClickListener?.invoke(adapterPosition)
                }
            }
        }
    }
}