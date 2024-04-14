package melerospaw.deudapp.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import melerospaw.deudapp.R

const val INFINITY_CHAR = "\u221e"
const val NEGATIVE_INFINITY_CHAR = "-\u221e"
@StringRes const val DEFAULT_INFINITY_DIALOG_MESSAGE: Int = R.string.dialog__infinite_amount_message

fun String.isInfinityCharacter() = isPositiveInfinityCharacter() || isNegativeInfinityCharacter()

fun String.isPositiveInfinityCharacter() = this == INFINITY_CHAR

fun String.isNegativeInfinityCharacter() = this == NEGATIVE_INFINITY_CHAR

fun getInfiniteFloat() = Float.POSITIVE_INFINITY

fun getNegativeInfiniteFloat() = Float.NEGATIVE_INFINITY

fun Float.getSignedInfinityCharacter() =
    if (this == Float.POSITIVE_INFINITY) {
        INFINITY_CHAR
    } else {
        NEGATIVE_INFINITY_CHAR
    }

fun Any.isInfiniteFloat() =
        when (this) {
            is Float -> {
                this == Float.POSITIVE_INFINITY || this == Float.NEGATIVE_INFINITY
            }
            is String -> {
                try {
                    val floatValue = toFloat()
                    floatValue == Float.POSITIVE_INFINITY || floatValue == Float.NEGATIVE_INFINITY
                } catch (e: NumberFormatException) {
                    false
                }
            }
            else -> false
        }

fun mostrarInfinityDialog(context: Context, mensaje: String?,
                          positiveCallback: DialogInterface.OnClickListener? = null,
                          negativeCallback: DialogInterface.OnClickListener? = null) {
    AlertDialog.Builder(context)
            .setTitle(R.string.dialog__infinite_amount_title)
            .setMessage(mensaje ?: context.getString(DEFAULT_INFINITY_DIALOG_MESSAGE))
            .setPositiveButton(R.string.si) { dialog, which ->
                    positiveCallback?.onClick(dialog, which)
                    dialog.dismiss()
            }
            .setNegativeButton(R.string.no) { dialog, which ->
                negativeCallback?.onClick(dialog, which)
                dialog.dismiss()
            }
            .show()
}

fun additionResultIsInfinite(operand1: Float, operand2: Float) =
    noOperandsAreInfinite(operand1, operand2) && (operand1 + operand2).isInfiniteFloat()

fun substractionResultIsInfinite(operand1: Float, operand2: Float) =
    noOperandsAreInfinite(operand1, operand2) && (operand1 - operand2).isInfiniteFloat()


fun noOperandsAreInfinite(vararg operands: Float) = operands.none {it.isInfiniteFloat()}

fun showUselessOperationDialog(context: Context) {
    AlertDialog.Builder(context)
            .setTitle(R.string.useless_operation_title)
            .setMessage(R.string.useless_operation_message)
            .setPositiveButton(R.string.i_know) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
}

fun operandsAreInfinite(vararg operands : Float) = listOf(operands).all { it.isInfiniteFloat() }

fun mostrarInfinityDialog(context: Context,
                          @StringRes mensaje: Int = DEFAULT_INFINITY_DIALOG_MESSAGE,
                          positiveCallback: DialogInterface.OnClickListener? = null,
                          negativeCallback: DialogInterface.OnClickListener? = null) {
    mostrarInfinityDialog(context, context.getString(mensaje), positiveCallback, negativeCallback)
}
