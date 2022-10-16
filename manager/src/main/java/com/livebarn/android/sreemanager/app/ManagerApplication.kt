package com.livebarn.android.sreemanager.app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.livebarn.android.sreelibrary.Constants

class ManagerApplication : Application() {

    var auth: FirebaseAuth? = null
    var dbReference: DatabaseReference? = null
    var currentUser: FirebaseUser? = null

    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth
        currentUser = auth?.currentUser
        dbReference = Firebase.database(Constants.DB_URL)
            .getReference(Constants.DB_NAME)
    }
}