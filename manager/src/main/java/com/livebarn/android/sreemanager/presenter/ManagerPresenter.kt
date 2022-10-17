package com.livebarn.android.sreemanager.presenter

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.livebarn.android.sreelibrary.BuildConfig
import com.livebarn.android.sreelibrary.Constants
import com.livebarn.android.sreelibrary.model.CelebrationType
import com.livebarn.android.sreelibrary.model.User
import com.livebarn.android.sreemanager.contract.ManagerContract
import java.text.SimpleDateFormat
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
    private val types = mutableListOf<CelebrationType>()

    private val clapGifs = mutableListOf<String>()
    private val happyBDayGifs = mutableListOf<String>()

    private var messageDBReference: DatabaseReference? = null
    private var targetGifDBReference: DatabaseReference? = null
    private var clapGifsDBReference: DatabaseReference? = null
    private var happyBDayGifsDBReference: DatabaseReference? = null
    private var alignDBReference: DatabaseReference? = null
    private var userDBReference: DatabaseReference? = null

    private var testDBReference: DatabaseReference? = dbReference?.child("testField")

    private var currentType = CelebrationType.NONE
    private var currentUser: User? = null

    init {
        messageDBReference = dbReference?.child(Constants.DB_PATH_MESSAGE)
        targetGifDBReference = dbReference?.child(Constants.DB_PATH_TARGET_GIF)
        clapGifsDBReference = dbReference?.child(Constants.DB_PATH_GIFS)?.child(Constants.DB_PATH_GIF_CLAPS)
        happyBDayGifsDBReference = dbReference?.child(Constants.DB_PATH_GIFS)?.child(Constants.DB_PATH_GIF_HAPPY_BDAY)
        alignDBReference = dbReference?.child(Constants.DB_PATH_LOCATION)
        userDBReference = dbReference?.child(Constants.DB_TABLE_USERS)

        types.addAll(CelebrationType.values())
    }

    override fun clickAlignButton(alignment: String) {
        alignDBReference?.setValue(alignment)
    }

    override fun clickSaveMessageButton(message: String) {
        if (BuildConfig.DEBUG) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            testDBReference?.setValue("click datetime: ${formatter.format(Date())}")
        }
        messageDBReference?.setValue(message)
    }

    override fun bindDatabase() {
        messageDBReference?.addValueEventListener(messageValueListener)
        targetGifDBReference?.addValueEventListener(targetGifValueListener)
        clapGifsDBReference?.addValueEventListener(clapGifsValueListener)
        happyBDayGifsDBReference?.addValueEventListener(happyBDayGifsValueListener)
        alignDBReference?.addValueEventListener(alignValueListener)
    }

    override fun unbindDatabase() {
        messageDBReference?.removeEventListener(messageValueListener)
        targetGifDBReference?.removeEventListener(targetGifValueListener)
        clapGifsDBReference?.removeEventListener(clapGifsValueListener)
        happyBDayGifsDBReference?.removeEventListener(happyBDayGifsValueListener)
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
        gifAt(position)?.let {
            targetGifDBReference?.setValue(it)
        }
    }

    override fun numberOfTypes(): Int {
        return numberOfTypes()
    }

    override fun typeAt(position: Int): CelebrationType? {
        return try {
            types[position]
        } catch (e: Exception) {
            null
        }
    }

    override fun clickType(position: Int) {
        typeAt(position)?.let {
            currentType = it
            gifs.clear()
            when (currentType) {
                CelebrationType.HAPPY_B_DAY -> {
                    gifs.addAll(happyBDayGifs)
                }
                CelebrationType.NONE -> {

                }
                else -> {
                    gifs.addAll(clapGifs)
                }
            }
            view?.onTypeClicked()
        }
    }

    override fun clickSignOut() {
        auth?.signOut()
        if (auth?.currentUser == null) {
            view?.onSignOutActionClicked()
        }
    }

    override fun isAdminUser() = currentUser?.permission == "admin"

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
    }

    override fun onViewDestroyed() {
        view = null
        messageDBReference = null
        targetGifDBReference = null
        clapGifsDBReference = null
        happyBDayGifsDBReference = null
        alignDBReference = null
        userDBReference = null
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

    private val clapGifsValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.hasChildren().not()) {
                insertClapsGifs()
            } else {
                clapGifs.clear()
                for (url in snapshot.children) {
                    (url.value as? String)?.let {
                        clapGifs.add(it)
                    }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() clapGifsValueListener -> ${error.message}")
        }
    }

    private val happyBDayGifsValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.hasChildren().not()) {
                insertHappyBDayGifs()
            } else {
                happyBDayGifs.clear()
                for (url in snapshot.children) {
                    (url.value as? String)?.let {
                        happyBDayGifs.add(it)
                    }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled() happyBDayGifssValueListener -> ${error.message}")
        }
    }

    private fun insertHappyBDayGifs() {
        // TODO("Not yet implemented")
    }

    private fun insertClapsGifs() {
        // TODO("Not yet implemented")
    }
}