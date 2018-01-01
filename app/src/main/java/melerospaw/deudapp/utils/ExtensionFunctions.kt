package melerospaw.deudapp.utils

import android.animation.LayoutTransition
import android.content.Context
import android.os.Build
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v7.widget.ViewUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

fun inflate(layoutInflater: LayoutInflater?, @LayoutRes layoutRes: Int, container: ViewGroup?) =
        layoutInflater?.inflate(layoutRes, container, false)

fun Fragment.shortToast(message: String) {
    Toast.makeText(context!!.applicationContext, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.longToast(message: String) {
    Toast.makeText(context!!.applicationContext, message, Toast.LENGTH_LONG).show()
}

fun Context.shortToast(message: String) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}

val TextView.texto: String
    get() = text.toString()

fun enableAnimateChanges(views: List<ViewGroup>) {
    if (Build.VERSION.SDK_INT >= 16) {
        views.forEach {
            it.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        }
    }
}