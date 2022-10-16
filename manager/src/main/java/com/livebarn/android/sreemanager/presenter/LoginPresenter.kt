package com.livebarn.android.sreemanager.presenter

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.livebarn.android.sreelibrary.Constants
import com.livebarn.android.sreelibrary.util.Validator
import com.livebarn.android.sreemanager.contract.LoginContract

class LoginPresenter(
    private var view: LoginContract.View?,
    private var auth: FirebaseAuth?
): LoginContract.Presenter {

    companion object {
        private const val TAG = "LoginPresenter"
    }

    override fun clickLoginButton(email: String?, password: String?) {
        if (email.isNullOrEmpty()) {
            view?.onLoginButtonClicked(message = "Please input your email address.")
            return
        }

        if (Validator.isValidEmail(email).not()) {
            view?.onLoginButtonClicked(message = "Please input the correct email address.")
            return
        }

        if (password.isNullOrEmpty()) {
            view?.onLoginButtonClicked(message = "Please input your password")
            return
        }

        if (password.length < 4) {
            view?.onLoginButtonClicked(message = "Password should be at least 4 characters.")
            return
        }

        auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth?.currentUser?.let {
                    view?.onLoginButtonClicked(user = it, null)
                }
            } else {
                view?.onLoginButtonClicked(message = "Please try again. ${task.exception?.message}")
                Log.d(TAG, "error: ${task.exception?.message}")
            }
        }
    }

    override fun clickForgotPassword(email: String?) {
        if (email.isNullOrEmpty()) {
            view?.onForgotPasswordButtonClicked(message = "Please input your email address.")
            return
        }

        if (Validator.isValidEmail(email).not()) {
            view?.onForgotPasswordButtonClicked(message = "Please input the correct email address.")
            return
        }

        auth?.sendPasswordResetEmail(email)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                view?.onForgotPasswordButtonClicked(message = "Please check your email. Sometimes it might be in Spam box.")
            } else {
                view?.onForgotPasswordButtonClicked(message = "Please try again. It has an error ${task.exception?.message}")
                Log.d(TAG, "error: ${task.exception?.message}")
            }
        }
    }

    override fun clickSignUpButton() {
        view?.onSignUpButtonClicked()
    }

    override fun onViewCreated() {}

    override fun onViewDestroyed() {
        view = null
        auth = null
    }

}