package com.dicoding.picodiploma.loginwithanimation.view.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.dicoding.picodiploma.loginwithanimation.R
import com.google.android.material.textfield.TextInputLayout

class PasswordEditText : AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val parent = parent.parent
                if (parent is TextInputLayout) {
                    if (s != null && s.length < 8) {
                        error = context.getString(R.string.error_password)
                        parent.endIconMode = TextInputLayout.END_ICON_NONE
                    } else {
                        error = null
                        parent.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
