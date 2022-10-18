package com.livebarn.android.sreemanager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.livebarn.android.sreelibrary.Constants
import com.livebarn.android.sreelibrary.hideKeyboard
import com.livebarn.android.sreelibrary.model.CelebrationType
import com.livebarn.android.sreelibrary.model.User
import com.livebarn.android.sreemanager.app.ManagerApplication
import com.livebarn.android.sreemanager.auth.LoginActivity
import com.livebarn.android.sreemanager.contract.ManagerContract
import com.livebarn.android.sreemanager.presenter.ManagerPresenter

class ManagerActivity : AppCompatActivity(), ManagerContract.View {

    companion object {
        private const val TAG = "ManagerActivity"
        private val DEFAULT_GIF = Constants.claps["Standing Ovation"]
    }

    private var presenter: ManagerContract.Presenter? = null

    private var imageButtonAlignTop: ImageView? = null
    private var imageButtonAlignCenter: ImageView? = null
    private var imageButtonAlignBottom: ImageView? = null

    private var editTextMessage: EditText? = null
    private var imageViewSelectedGif: ImageView? = null
    private var textViewDummyMessage: TextView? = null
    private var textViewMessageLength: TextView? = null
    private var spinnerCelebrationType: Spinner? = null
    private var recyclerViewGifs: RecyclerView? = null

    private var menu: Menu? = null
    private var oldPosition = 0

    private val buttonBackgroundArray = arrayOf(
        com.livebarn.android.sreelibrary.R.drawable.rect_bg_button,
        com.livebarn.android.sreelibrary.R.drawable.rect_bg_button_selected
    )

    private val loginWithResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            initViews()
            presenter?.onViewCreated()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)

        setPresenter(
            ManagerPresenter(
                this,
                (application as? ManagerApplication)?.auth,
                (application as? ManagerApplication)?.dbReference
            )
        )

        setSupportActionBar(findViewById(R.id.toolbar))

        val auth = (application as? ManagerApplication)?.auth
        if (auth?.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            loginWithResult.launch(intent)
        } else {
            initViews()
            presenter?.onViewCreated()
        }
    }

    private fun initViews() {
        editTextMessage = findViewById(R.id.edit_text_message)
        imageViewSelectedGif = findViewById(R.id.image_view_selected_gif)
        textViewMessageLength = findViewById(R.id.text_view_message_length)
        spinnerCelebrationType = findViewById(R.id.spinner_celebration_type)

        imageButtonAlignTop = findViewById(R.id.image_button_align_top)
        imageButtonAlignCenter = findViewById(R.id.image_button_align_center)
        imageButtonAlignBottom = findViewById(R.id.image_button_align_bottom)

        textViewDummyMessage = findViewById(R.id.text_view_dummy_message)

        recyclerViewGifs = findViewById<RecyclerView>(R.id.recycler_view_gifs).apply {
            layoutManager = LinearLayoutManager(this@ManagerActivity)
            presenter?.let {
                recyclerViewGifs?.adapter = GifAdapter(it)
            }
        }
        findViewById<ImageButton>(R.id.image_button_clear).setOnClickListener {
            editTextMessage?.setText("")
        }

        findViewById<Button>(R.id.button_save).setOnClickListener {
            val message = editTextMessage?.text.toString()
            presenter?.clickSaveMessageButton(message)
        }

        editTextMessage?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun afterTextChanged(s: Editable?) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textViewMessageLength?.text =
                    getString(R.string.label_message_length, editTextMessage?.length() ?: 0)
            }
        })

        imageButtonAlignTop?.setOnClickListener {
            presenter?.clickAlignButton(Constants.DB_VALUE_TOP)
        }
        imageButtonAlignCenter?.setOnClickListener {
            presenter?.clickAlignButton(Constants.DB_VALUE_CENTER)
        }
        imageButtonAlignBottom?.setOnClickListener {
            presenter?.clickAlignButton(Constants.DB_VALUE_BOTTOM)
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
                    presenter?.clickType(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e(TAG, "onNothingSelected()")
            }
        }

        presenter?.onViewCreated()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_manager, menu)
        menu?.findItem(R.id.action_handle_users)?.isVisible = presenter?.isAdminUser() == true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_signout -> presenter?.clickSignOut()
            R.id.action_handle_users -> presenter?.clickUsers()
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        presenter?.bindDatabase()
    }

    override fun onStop() {
        super.onStop()
        presenter?.unbindDatabase()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onViewDestroyed()
        presenter = null
        editTextMessage = null
        imageViewSelectedGif = null
        textViewMessageLength = null
        spinnerCelebrationType = null
        recyclerViewGifs = null
        imageButtonAlignTop = null
        imageButtonAlignCenter = null
        imageButtonAlignBottom = null
    }

    override fun onAlignButtonClicked(alignment: String?) {
        var textViewLayoutParams = textViewDummyMessage?.layoutParams as? ConstraintLayout.LayoutParams
        when (alignment) {
            Constants.DB_VALUE_TOP -> {
                imageButtonAlignTop?.setBackgroundResource(buttonBackgroundArray[1])
                imageButtonAlignCenter?.setBackgroundResource(buttonBackgroundArray[0])
                imageButtonAlignBottom?.setBackgroundResource(buttonBackgroundArray[0])
                textViewLayoutParams?.topToTop = R.id.image_view_selected_gif
                textViewLayoutParams?.bottomToBottom = -1
            }
            Constants.DB_VALUE_CENTER -> {
                imageButtonAlignTop?.setBackgroundResource(buttonBackgroundArray[0])
                imageButtonAlignCenter?.setBackgroundResource(buttonBackgroundArray[1])
                imageButtonAlignBottom?.setBackgroundResource(buttonBackgroundArray[0])
                textViewLayoutParams?.topToTop = R.id.image_view_selected_gif
                textViewLayoutParams?.bottomToBottom = R.id.image_view_selected_gif
            }
            Constants.DB_VALUE_BOTTOM -> {
                imageButtonAlignTop?.setBackgroundResource(buttonBackgroundArray[0])
                imageButtonAlignCenter?.setBackgroundResource(buttonBackgroundArray[0])
                imageButtonAlignBottom?.setBackgroundResource(buttonBackgroundArray[1])
                textViewLayoutParams?.topToTop = -1
                textViewLayoutParams?.bottomToBottom = R.id.image_view_selected_gif
            }
            else -> {
                textViewLayoutParams = null
            }
        }
        textViewLayoutParams?.let {
            textViewDummyMessage?.layoutParams = it
        }
    }

    override fun onSaveMessageButtonClicked(message: String?) {
        message?.let {
            editTextMessage?.setText(it)
            editTextMessage?.setSelection(it.length)
        }
        hideKeyboard()
    }

    override fun warnNoAuthority() {
        Toast.makeText(
            this,
            "You need to get an authority to do it. Please ask to Yeojong.",
            Toast.LENGTH_SHORT
        ).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onTypeClicked() {
        recyclerViewGifs?.adapter?.notifyDataSetChanged()
    }

    override fun onGifClicked(url: String?) {
        url?.let {
            imageViewSelectedGif?.let { view ->
                Glide.with(view)
                    .asGif()
                    .load(it)
                    .into(view)
            }
        }
    }

    override fun onSignOutActionClicked() {
        val intent = Intent(this, LoginActivity::class.java)
        loginWithResult.launch(intent)
    }

    override fun onUsersActionClicked() {
        val intent = Intent(this, UsersActivity::class.java)
        startActivity(intent)
    }

    override fun onUserFetched(user: User?) {
        (application as? ManagerApplication)?.currentUser = user
        val title = StringBuilder("Hello")
        user?.let {
            title.append(" ${it.username}")
        }
        supportActionBar?.title = title

        menu?.findItem(R.id.action_handle_users)?.isVisible = presenter?.isAdminUser() == true
    }

    override fun setPresenter(presenter: ManagerContract.Presenter) {
        this.presenter = presenter
    }

    class GifAdapter(
        private var presenter: ManagerContract.Presenter
    ) : RecyclerView.Adapter<GifAdapter.GifViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifViewHolder {
            val imageView = ImageView(parent.context)
            imageView.adjustViewBounds = true

            return GifViewHolder(imageView, presenter::clickGif)
        }

        override fun onBindViewHolder(holder: GifViewHolder, position: Int) {
            val gif = presenter.gifAt(position)
            holder.imageView?.let { view ->
                Glide.with(view)
                    .asGif()
                    .load(
                        if (gif.isNullOrEmpty()) DEFAULT_GIF else gif
                    )
                    .into(view)
            }
        }

        override fun getItemCount() = presenter.numberOfGifs()

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