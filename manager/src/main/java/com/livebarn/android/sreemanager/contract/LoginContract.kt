package com.livebarn.android.sreemanager.contract

import com.google.firebase.auth.FirebaseUser
import com.livebarn.android.sreelibrary.BasePresenter
import com.livebarn.android.sreelibrary.BaseView

interface LoginContract {
    interface Presenter: BasePresenter {
        fun clickLoginButton(email: String?, password: String?)
        fun clickForgotPassword(email: String?)
        fun clickSignUpButton()
    }

    interface View: BaseView<Presenter> {
        fun onLoginButtonClicked(user: FirebaseUser? = null, message: String?)
        fun onForgotPasswordButtonClicked(message: String?)
        fun onSignUpButtonClicked()
    }
}