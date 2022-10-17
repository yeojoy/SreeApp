package com.livebarn.android.sreemanager.app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.livebarn.android.sreelibrary.Constants
import com.livebarn.android.sreelibrary.model.User

class ManagerApplication : Application() {

    var auth: FirebaseAuth? = null
    var dbReference: DatabaseReference? = null
    var currentUser: User? = null

    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth
        dbReference = Firebase.database(Constants.DB_URL)
            .getReference(Constants.DB_NAME)
    }
}