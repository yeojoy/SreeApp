package com.livebarn.android.sreemanager.presenter

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.livebarn.android.sreelibrary.Constants
import com.livebarn.android.sreelibrary.model.Authority
import com.livebarn.android.sreelibrary.model.Category
import com.livebarn.android.sreelibrary.model.User
import com.livebarn.android.sreemanager.contract.ManagerContract
import java.util.*

class ManagerPresenter(
    private var view: ManagerContract.View?,
    private var auth: FirebaseAuth?,
    dbReference: DatabaseReference?
) : ManagerContract.Presenter {

    companion object {
        private const val TAG = "ManagerPresenter"
    }

    private val gifs = mutableListOf<String>()
    private val categories = mutableListOf<Category>()

    private var messageDBReference: DatabaseReference? = null
    private var targetGifDBReference: DatabaseReference? = null
    private var alignDBReference: DatabaseReference? = null
    private var userDBReference: DatabaseReference? = null
    private var categoryDBReference: DatabaseReference? = null
    private var categoryGifsDBReference: DatabaseReference? = null
    private var gifsDBReference: DatabaseReference? = null

    private var currentUser: User? = null

    init {
        messageDBReference = dbReference?.child(Constants.DB_PATH_MESSAGE)
        targetGifDBReference = dbReference?.child(Constants.DB_PATH_TARGET_GIF)
        alignDBReference = dbReference?.child(Constants.DB_PATH_LOCATION)
        userDBReference = dbReference?.child(Constants.DB_TABLE_USERS)
        categoryDBReference = dbReference?.child(Constants.DB_PATH_CATEGORIES)
        categoryGifsDBReference = dbReference?.child(Constants.DB_PATH_CATEGORY_GIFS)
        gifsDBReference = dbReference?.child(Constants.DB_PATH_GIFS)
    }

    override fun clickAlignButton(alignment: String) {
        if (hasAuthority()) {
            alignDBReference?.setValue(alignment)
        } else {
            view?.warnNoAuthority()
        }
    }

    override fun clickSaveMessageButton(message: String) {
        if (hasAuthority()) {
            messageDBReference?.setValue(message)
        } else {
            view?.warnNoAuthority()
        }
    }

    override fun bindDatabase() {
        messageDBReference?.addValueEventListener(messageValueListener)
        targetGifDBReference?.addValueEventListener(targetGifValueListener)
        alignDBReference?.addValueEventListener(alignValueListener)
    }

    override fun unbindDatabase() {
        messageDBReference?.removeEventListener(messageValueListener)
        targetGifDBReference?.removeEventListener(targetGifValueListener)
        alignDBReference?.removeEventListener(alignValueListener)
    }

    override fun numberOfGifs(): Int {
        return gifs.size
    }

    override fun gifAt(position: Int): String? {
        return try {
            gifs[position]
        } catch (e: Exception) {
            null
        }
    }

    override fun clickGif(position: Int) {
        if (hasAuthority()) {
            gifAt(position)?.let {
                targetGifDBReference?.setValue(it)
            }
        } else {
            view?.warnNoAuthority()
        }
    }

    override fun categoryAt(position: Int): Category? {
        return try {
            categories[position]
        } catch (e: Exception) {
            null
        }
    }

    override fun clickCategory(position: Int) {
        Log.d(TAG, "clickCategory($position)")
        categoryAt(position)?.let {
            gifs.clear()
            if (it.path.isNullOrEmpty()) {
                categoryGifsDBReference?.child(it.key)?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.value?.let { path ->
                            it.path = path.toString()
                            loadGifs(it.path)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled() categoryGifs listener -> ${error.message}")
                    }

                })
            } else {
                if (it.gifs.isEmpty()) {
                    loadGifs(it.path)
                } else {
                    gifs.addAll(it.gifs)
                }
            }
            view?.onCategoryClicked()
        }
    }

    override fun clickSignOut() {
        auth?.signOut()
        if (auth?.currentUser == null) {
            view?.onSignOutActionClicked()
        }
    }

    override fun clickUsers() {
        view?.onUsersActionClicked()
    }

    override fun isAdminUser() =
        currentUser?.authority?.lowercase() == Constants.DB_AUTHORITY_ADMIN.lowercase() ||
                currentUser?.authority?.lowercase() == Constants.DB_AUTHORITY_OWNER.lowercase()

    override fun onViewCreated() {
        auth?.currentUser?.let {
            userDBReference?.child(it.uid)?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.key?.let { key ->
                        currentUser = User.newUser(key, snapshot.value as? HashMap<*, *>)
                        view?.onUserFetched(currentUser)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled() userDBReference's listener -> ${error.message}")
                }
            })
        }

        categoryDBReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories.clear()
                if (snapshot.hasChildren()) {
                    for (child in snapshot.children) {
                        val category = Category(child.key.toString(), child.value.toString())
                        categories.add(category)
                    }

                    val titles = categories.map { it.title }
                    view?.onCategoryFetched(titles)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled() happyBDayGifssValueListener -> ${error.message}")
            }
        })
    }

    override fun onViewDestroyed() {
        view = null
        messageDBReference = null
        targetGifDBReference = null
        categoryDBReference = null
        categoryGifsDBReference = null
        alignDBReference = null
        userDBReference = null
        gifsDBReference = null
    }

    private val messageValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val message = snapshot.getValue(String::class.java)
            view?.onSaveMessageButtonClicked(message)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() messageValueListener -> ${error.message}")
        }
    }

    private val alignValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            view?.onAlignButtonClicked(snapshot.getValue(String::class.java))
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() locationValueListener -> ${error.message}")
        }
    }

    private val targetGifValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            view?.onGifClicked(snapshot.getValue(String::class.java))
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() targetGifValueListener -> ${error.message}")
        }
    }

    private fun hasAuthority(): Boolean {
        if (currentUser == null) return false
        return when (Authority.findByTitle(currentUser!!.authority)) {
            Authority.OWNER -> true
            Authority.ADMIN -> true
            Authority.CONFIRMED_USER -> true
            else -> false
        }
    }

    private fun loadGifs(path: String?) {
        path?.let {
            gifsDBReference?.child(path)?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        val gifs = mutableListOf<String>()
                        for (child in snapshot.children) {
                            val url = child.value.toString()
                            gifs.add(url)
                        }

                        if (gifs.isNotEmpty()) {
                            this@ManagerPresenter.gifs.addAll(gifs)

                            for (category in categories) {
                                if (category.path == it) {
                                    category.gifs.clear()
                                    category.gifs.addAll(gifs)
                                    break
                                }
                            }
                        }
                    }
                    view?.onCategoryClicked()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled() gifsDBReference's Value Listener -> ${error.message}")
                }
            })
        }
    }
}