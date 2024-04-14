package melerospaw.deudapp.utils

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.ListPreference
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

fun inflate(layoutInflater: LayoutInflater, @LayoutRes layoutRes: Int, container: ViewGroup?): View =
        layoutInflater.inflate(layoutRes, container, false)

fun Fragment.shortToast(message: String) {
    context?.applicationContext?.let { Toast.makeText(it, message, Toast.LENGTH_SHORT).show() }
}

fun Fragment.longToast(message: String) {
    context?.applicationContext?.let { Toast.makeText(it, message, Toast.LENGTH_LONG).show() }
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

inline fun <reified T : View?> ViewGroup.findFirstChild(condition: (View) -> Boolean): T? {
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

fun ListPreference.findIndexOfValueNonUnicodeCharacters(value: String): Int {
    val parsedValue = value.removeUnicodeInvisibleCharacters()
    for (i in 0 until entryValues.size) {
        val parsedEntry = entryValues[i].toString().removeUnicodeInvisibleCharacters()
        if (parsedValue == parsedEntry) {
            return i
        }
    }

    return -1
}

fun SharedPreferences.editar(block: SharedPreferences.Editor.() -> SharedPreferences.Editor) {
    this.edit().block().apply()
}

fun String.removeUnicodeInvisibleCharacters() = replace(Regex("[\\p{Cf}]"), "")

fun Number.toDpi(context: Context) = toFloat() * context.resources.displayMetrics.density

fun Number.toPx(context: Context) = toFloat() / context.resources.displayMetrics.density

fun View.hidden(hidden: Boolean) {
    if (hidden) hide() else visible()
}

fun View.visible(visible: Boolean) {
    if (visible) visible() else invisible()
}

fun View.hide() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.isHidden() = visibility == View.GONE

fun View.isVisible() = visibility == View.VISIBLE

fun View.isInvisible() = visibility == View.INVISIBLE

fun visible(vararg views: View) {
    for (view in views) {
        view.visible()
    }
}

fun invisible(vararg views: View) {
    for (view in views) {
        view.invisible()
    }
}

fun hide(vararg views: View) {
    for (view in views) {
        view.hide()
    }
}

fun <FIRST, SECOND, THIRD, R> doIfNotNull(
    firstParam: FIRST?,
    secondParam: SECOND?,
    thirdParam: THIRD?,
    doWhat: (first: FIRST, SECOND, THIRD) -> R,
): R? = if (firstParam != null && secondParam != null && thirdParam != null) {
    doWhat(firstParam, secondParam, thirdParam)
} else {
    null
}