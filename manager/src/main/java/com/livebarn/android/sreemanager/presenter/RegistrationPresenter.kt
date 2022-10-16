package com.livebarn.android.sreemanager.presenter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.livebarn.android.sreelibrary.Constants
import com.livebarn.android.sreelibrary.util.Validator
import com.livebarn.android.sreemanager.contract.RegistrationContract

class RegistrationPresenter(
    private var view: RegistrationContract.View?,
    private var auth: FirebaseAuth?,
    dbReference: DatabaseReference?
) : RegistrationContract.Presenter {

    private var userDBReference: DatabaseReference? =
        dbReference?.child(Constants.DB_TABLE_USERS)

    override fun clickSignUpButton(email: String?, password: String?, username: String?) {
        if (email.isNullOrEmpty()) {
            view?.onSignUpButtonClicked(message = "Please input your email address.")
            return
        }

        if (Validator.isValidEmail(email).not()) {
            view?.onSignUpButtonClicked(message = "Please input the correct email address.")
            return
        }

        if (password.isNullOrEmpty()) {
            view?.onSignUpButtonClicked(message = "Please input your password")
            return
        }

        if (password.length < 4) {
            view?.onSignUpButtonClicked(message = "Password should be at least 4 characters.")
            return
        }

        if (username.isNullOrEmpty()) {
            view?.onSignUpButtonClicked(message = "Please input your username for the app")
            return
        }

        auth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth?.currentUser?.let {
                    val userMap = hashMapOf(
                        "username" to username,
                        "createdAt" to System.currentTimeMillis(),
                        "permission" to "user"
                    )
                    userDBReference?.child(it.uid)?.setValue(userMap)
                    view?.onSignUpButtonClicked(user = it, null)
                }
            } else {
                view?.onSignUpButtonClicked(message = "Please try again. ${task.exception?.message}")
            }
        }
    }

    override fun clickWantLoginButton() {
        view?.onWantLoginButtonClicked()
    }

    override fun onViewCreated() {

    }

    override fun onViewDestroyed() {
        view = null
        auth = null
    }
}