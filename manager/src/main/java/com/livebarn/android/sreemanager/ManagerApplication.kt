package com.livebarn.android.sreemanager

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ManagerApplication : Application() {

    var auth: FirebaseAuth? = null

    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth
    }
}