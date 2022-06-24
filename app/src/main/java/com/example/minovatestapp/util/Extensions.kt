package com.example.minovatestapp.util

import android.text.Editable
import android.view.View
import android.widget.EditText

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun EditText.clearAndHide() {
    this.text.clear()
    this.hide()
}

fun Editable.getIntOrZero(): Int {
    return if (this.toString().isNotEmpty()) {
        this.toString().toInt()
    } else {
        0
    }
}