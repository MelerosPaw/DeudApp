package melerospaw.deudapp.utils

import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast

fun Fragment.inflate(layoutInflater: LayoutInflater?, @LayoutRes layoutRes: Int, container: ViewGroup?) =
        layoutInflater?.inflate(layoutRes, container, false)

fun Fragment.shortToast(message: String) {
    Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
}

val EditText.texto: String
    get() = text.toString()