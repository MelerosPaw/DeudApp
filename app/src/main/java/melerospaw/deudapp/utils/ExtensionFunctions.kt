package melerospaw.deudapp.utils

import android.animation.LayoutTransition
import android.content.Context
import android.os.Build
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v7.widget.ViewUtils
import android.view.LayoutInflater
import android.view.View
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

fun ViewGroup.forEachChildView(onEachView: (View) -> Unit) {
    for (i in 0 until childCount) {
        onEachView(getChildAt(i))
    }
}

inline fun <reified T: View?> ViewGroup.findFirstChild(condition: (View)-> Boolean): T? {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (condition(child)) {
            return if (child is T) child else null
        }
    }
    return null
}

fun ViewGroup.getChildList(): List<View> {
    val childList = ArrayList<View>()
    forEachChildView { childList.add(it) }
    return childList
}

fun ViewGroup.getChildPosition(view: View): Int {
    val views: List<View> = getChildList()
    for (i in 0 until views.size) {
        if (views[i] == view) {
            return i
        }
    }
    return -1
}
