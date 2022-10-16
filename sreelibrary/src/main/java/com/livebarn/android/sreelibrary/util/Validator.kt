package com.livebarn.android.sreelibrary.util

import android.util.Patterns

object Validator {

    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrEmpty()) return false
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}