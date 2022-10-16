package com.livebarn.android.sreelibrary.view

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.livebarn.android.sreelibrary.R
import com.livebarn.android.sreelibrary.util.Validator

/**
 * CustomView for input email, password and username
 *
 * TODO
 * 1. password visibility
 * 2. accept custom validation
 */
class InputLayout(
    context: Context, attrs: AttributeSet?, defStyle: Int
) : ConstraintLayout(context, attrs, defStyle), InputLayoutInterface {

    enum class LayoutInputType {
        EMAIL,
        PASSWORD,
        TEXT
    }

    companion object {
        private const val TAG = "InputLayout"
    }

    constructor(context: Context) : this(context, null, 0) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        initView(attrs)
    }

    init {
        initView(attrs)
    }

    private var editText: EditText? = null
    private var imageViewIcon: ImageView? = null
    private var imageButtonClear: ImageButton? = null
    private var imageButtonPasswordVisibility: ImageButton? = null
    private var viewUnderline: View? = null
    private var listener: ((isValid: Boolean) -> Unit)? = null

    private var isPasswordVisible = false

    var hasValidValue = false
    var text: String = ""

    private fun initView(attrs: AttributeSet?) {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.layout_input, this, false)
        addView(view)

        editText = view.findViewById(R.id.edit_text_content)
        imageViewIcon = view.findViewById(R.id.image_view_edit_text_icon)
        imageButtonClear = view.findViewById(R.id.image_button_clear)
        imageButtonPasswordVisibility = view.findViewById(R.id.image_button_password_visibility)
        viewUnderline = view.findViewById(R.id.view_underline)

        imageButtonClear?.setOnClickListener {
            editText?.setText("")
            isPasswordVisible = false
            imageButtonPasswordVisibility?.setImageResource(R.drawable.ic_visibility)
            editText?.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        imageButtonPasswordVisibility?.setOnClickListener {
            isPasswordVisible = isPasswordVisible.not()
            val textLength = editText?.text.toString().length
            if (isPasswordVisible) {
                imageButtonPasswordVisibility?.setImageResource(R.drawable.ic_visibility_off)
                editText?.transformationMethod = null
            } else {
                imageButtonPasswordVisibility?.setImageResource(R.drawable.ic_visibility)
                editText?.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            editText?.setSelection(textLength)
        }

        attrs?.let {
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.InputLayout,
                0,
                0).apply {

                try {
                    val layoutInputType = when (getInt(R.styleable.InputLayout_layoutInputType, 3)) {
                        1 -> LayoutInputType.EMAIL
                        2 -> LayoutInputType.PASSWORD
                        else -> LayoutInputType.TEXT
                    }
                    val focusNext = getInt(
                        R.styleable.InputLayout_android_nextFocusDown, 0
                    )
                    val iconSrc = getResourceId(
                        R.styleable.InputLayout_iconSrc, R.drawable.ic_email
                    )
                    val hint = getString(R.styleable.InputLayout_android_hint)

                    val minLength = getInt(R.styleable.InputLayout_minLength, -1)
                    val maxLength = getInt(R.styleable.InputLayout_maxLength, 100)

                    Log.d(TAG, "initView() layoutInputType: $layoutInputType")
                    when (layoutInputType) {
                        LayoutInputType.EMAIL -> {
                            editText?.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                            setDefaultEmailValidation()
                        }
                        LayoutInputType.PASSWORD -> {
                            editText?.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                            editText?.transformationMethod = PasswordTransformationMethod.getInstance()
                            setEditTextLengthValidation(
                                if (minLength == -1) 4 else minLength,
                                maxLength
                            )
                            val paddingStart = context.resources
                                .getDimensionPixelSize(R.dimen.padding_start_edit_text)
                            val paddingEnd = context.resources
                                .getDimensionPixelSize(R.dimen.padding_end_edit_text_visible_password)
                            editText?.setPadding(paddingStart, 0, paddingEnd, 0)
                        }
                        else -> {
                            editText?.inputType = InputType.TYPE_TEXT_VARIATION_NORMAL
                            setEditTextLengthValidation(
                                if (minLength == -1) 2 else minLength,
                                maxLength
                            )
                        }
                    }
                    editText?.nextFocusDownId = focusNext
                    editText?.hint = hint
                    imageViewIcon?.setImageResource(iconSrc)

                    editText?.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun afterTextChanged(s: Editable?) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            text = s.toString()
                            imageButtonClear?.visibility =
                                if (text.isNotEmpty()) View.VISIBLE else GONE

                            if (layoutInputType == LayoutInputType.PASSWORD) {
                                imageButtonPasswordVisibility?.visibility =
                                    if (text.isNotEmpty()) View.VISIBLE else View.GONE
                            }
                        }
                    })

                } finally {
                    recycle()
                }
            }
        }
    }

    private fun setDefaultEmailValidation() {
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                hasValidValue = false
                when {
                    text.isEmpty() -> {
                        viewUnderline?.setBackgroundResource(R.drawable.rect_edit_text_bg_default)
                    }
                    Validator.isValidEmail(text) -> {
                        viewUnderline?.setBackgroundResource(R.drawable.rect_edit_text_bg_right)
                        hasValidValue = true
                    }
                    else -> {
                        viewUnderline?.setBackgroundResource(R.drawable.rect_edit_text_bg_wrong)
                    }
                }
                listener?.invoke(hasValidValue)
            }
        })
    }

    private fun setEditTextLengthValidation(minLength: Int? = null, maxLength: Int? = null) {
        hasValidValue = false
        if (minLength == null && maxLength == null) {
            viewUnderline?.setBackgroundResource(R.drawable.rect_edit_text_bg_default)
            listener?.invoke(hasValidValue)
            return
        }

        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val textLength = s.toString().length
                hasValidValue = false
                when {
                    textLength == 0 -> {
                        viewUnderline?.setBackgroundResource(R.drawable.rect_edit_text_bg_default)
                    }
                    minLength != null && textLength >= minLength
                            && maxLength != null && maxLength >= textLength -> {
                        viewUnderline?.setBackgroundResource(R.drawable.rect_edit_text_bg_right)
                        hasValidValue = true
                    }
//                    minLength != null && textLength >= minLength -> {
//                        viewUnderline?.setBackgroundResource(R.drawable.rect_edit_text_bg_right)
//                    }
//                    maxLength != null && maxLength >= textLength -> {
//                        viewUnderline?.setBackgroundResource(R.drawable.rect_edit_text_bg_right)
//                    }
                    else -> {
                        viewUnderline?.setBackgroundResource(R.drawable.rect_edit_text_bg_wrong)
                    }
                }
                listener?.invoke(hasValidValue)
            }
        })
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d(TAG, "onLayout(left: $left, top: $top, right: $right, bottom: $bottom)")
    }

    override fun setValidationListener(listener: ((isValid: Boolean) -> Unit)?) {
        this.listener = listener
    }
}

interface InputLayoutInterface {
    fun setValidationListener(listener: ((isValid: Boolean) -> Unit)?)
}