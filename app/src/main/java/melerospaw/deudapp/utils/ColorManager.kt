package melerospaw.deudapp.utils

import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.View
import melerospaw.deudapp.R

class ColorManager {

    companion object {

        @JvmStatic fun pintarColorDeuda(vista: View, cantidad: Float) {

            val colorId = when {
                cantidad < 0 -> R.color.light_red
                cantidad > 0 -> R.color.light_green
                else -> R.color.light_blue
            }

            vista.setBackgroundColor(ContextCompat.getColor(vista.context, colorId))
        }
    }
}