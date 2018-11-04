package melerospaw.deudapp.utils

import android.graphics.drawable.Drawable
import com.amulyakhare.textdrawable.TextDrawable

class TextDrawableManager {

    companion object {

        @JvmStatic fun createDrawable(text: String, color: Int): Drawable {
            return TextDrawable.builder()
                    .beginConfig()
                    .endConfig()
                    .buildRound(text, color)
        }

        @JvmStatic fun createDrawable(text: Char, color: Int) = createDrawable(text.toString(), color)

        @JvmStatic fun createDrawable(text: String, shapeColor: Int, textColor: Int): Drawable =
                TextDrawable.builder()
                        .beginConfig()
                        .textColor(textColor)
                        .endConfig()
                        .buildRect(text, shapeColor)
    }
}