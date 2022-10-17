package com.livebarn.android.sreemanager.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.livebarn.android.sreelibrary.BuildConfig
import com.livebarn.android.sreelibrary.view.InputLayout
import com.livebarn.android.sreemanager.R
import com.livebarn.android.sreemanager.app.ManagerApplication
import com.livebarn.android.sreemanager.contract.LoginContract
import com.livebarn.android.sreemanager.presenter.LoginPresenter
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity(), LoginContract.View {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private var presenter: LoginContract.Presenter? = null

    private var inputLayoutEmail: InputLayout? = null
    private var inputLayoutPassword: InputLayout? = null
    private var buttonLogin: Button? = null

    private val registrationWithResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            moveManageActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setPresenter(
            LoginPresenter(this, (application as? ManagerApplication)?.auth)
        )

        initViews()
        presenter?.onViewCreated()
    }

    private fun initViews() {
        inputLayoutEmail = findViewById(R.id.input_layout_email)
        inputLayoutPassword = findViewById(R.id.input_layout_password)
        buttonLogin = findViewById(R.id.button_login)
        buttonLogin?.isEnabled = inputLayoutEmail?.hasValidValue == true &&
                inputLayoutPassword?.hasValidValue == true

        buttonLogin?.setOnClickListener {
            presenter?.clickLoginButton(inputLayoutEmail?.text, inputLayoutPassword?.text)
        }

        findViewById<Button>(R.id.button_forgot_password).setOnClickListener {
            if (BuildConfig.DEBUG) {
                val db = (application as? ManagerApplication)?.dbReference?.child("testField")
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                db?.setValue("click datetime: ${formatter.format(Date())}")
            } else {
                presenter?.clickForgotPassword(inputLayoutEmail?.text)
            }
        }

        findViewById<Button>(R.id.button_need_new_account).setOnClickListener {
            presenter?.clickSignUpButton()
        }

        inputLayoutEmail?.setValidationListener {
            buttonLogin?.isEnabled = it && inputLayoutPassword?.hasValidValue == true
        }

        inputLayoutPassword?.setValidationListener {
            buttonLogin?.isEnabled = it && inputLayoutEmail?.hasValidValue == true
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onViewDestroyed()
        presenter = null
    }

    override fun onLoginButtonClicked(user: FirebaseUser?, message: String?) {
        if (message.isNullOrEmpty()) {
            moveManageActivity()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onForgotPasswordButtonClicked(message: String?) {
        message?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSignUpButtonClicked() {
        val intent = Intent(this, RegistrationActivity::class.java)
        registrationWithResult.launch(intent)
    }

    override fun setPresenter(presenter: LoginContract.Presenter) {
        this.presenter = presenter
    }

    private fun moveManageActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}