package com.livebarn.android.sreemanager.auth

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.livebarn.android.sreelibrary.view.InputLayout
import com.livebarn.android.sreemanager.R
import com.livebarn.android.sreemanager.app.ManagerApplication
import com.livebarn.android.sreemanager.contract.RegistrationContract
import com.livebarn.android.sreemanager.presenter.RegistrationPresenter

class RegistrationActivity : AppCompatActivity(), RegistrationContract.View {

    private var presenter: RegistrationContract.Presenter? = null

    private var inputLayoutEmail: InputLayout? = null
    private var inputLayoutPassword: InputLayout? = null
    private var inputLayoutUsername: InputLayout? = null
    private var buttonSignUp: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        setPresenter(
            RegistrationPresenter(
                this,
                (application as? ManagerApplication)?.auth,
                (application as? ManagerApplication)?.dbReference
            )
        )

        initViews()

        presenter?.onViewCreated()
    }

    private fun initViews() {
        inputLayoutEmail = findViewById(R.id.input_layout_email)
        inputLayoutPassword = findViewById(R.id.input_layout_password)
        inputLayoutUsername = findViewById(R.id.input_layout_username)
        buttonSignUp = findViewById(R.id.button_signup)

        buttonSignUp?.setOnClickListener {
            presenter?.clickSignUpButton(
                inputLayoutEmail?.text,
                inputLayoutPassword?.text,
                inputLayoutUsername?.text
            )
        }

        inputLayoutEmail?.setValidationListener { isValid ->
            buttonSignUp?.isEnabled = isValid && inputLayoutPassword?.hasValidValue == true
                    && inputLayoutUsername?.hasValidValue == true
        }
        inputLayoutPassword?.setValidationListener { isValid ->
            buttonSignUp?.isEnabled = isValid && inputLayoutEmail?.hasValidValue == true
                    && inputLayoutUsername?.hasValidValue == true
        }
        inputLayoutUsername?.setValidationListener { isValid ->
            buttonSignUp?.isEnabled = isValid && inputLayoutEmail?.hasValidValue == true
                    && inputLayoutPassword?.hasValidValue == true
        }

        findViewById<Button>(R.id.button_want_login).setOnClickListener {
            presenter?.clickWantLoginButton()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onViewDestroyed()
        presenter = null
    }

    override fun onSignUpButtonClicked(user: FirebaseUser?, message: String?) {
        if (message.isNullOrEmpty()) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onWantLoginButtonClicked() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun setPresenter(presenter: RegistrationContract.Presenter) {
        this.presenter = presenter
    }
}