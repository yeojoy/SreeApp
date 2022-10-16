package com.livebarn.android.sreemanager.contract

import com.google.firebase.auth.FirebaseUser
import com.livebarn.android.sreelibrary.BasePresenter
import com.livebarn.android.sreelibrary.BaseView

interface RegistrationContract {
    interface Presenter: BasePresenter {
        fun clickSignUpButton(email: String?, password: String?, username: String?)
        fun clickWantLoginButton()
    }

    interface View: BaseView<Presenter> {
        fun onSignUpButtonClicked(user: FirebaseUser? = null, message: String?)
        fun onWantLoginButtonClicked()
    }
}