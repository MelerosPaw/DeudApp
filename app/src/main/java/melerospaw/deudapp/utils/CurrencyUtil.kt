package melerospaw.deudapp.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup

fun getCurrency(context: Context) = Currency.getCurrencyBySign(SharedPreferencesManager(context).getCurrency())

fun formatAmount(context: Context, amount: Float): String {
    val desiredDecimals = 2
    val decimalChar = "."
    val desiredDecimalChar = ","
    val formattedAmount = DecimalFormatUtils.decimalToStringIfZero(amount, desiredDecimals,
            decimalChar, desiredDecimalChar)
    val currency = getCurrency(context)

    return when {
        currency.posicion == Currency.Position.DELANTE -> {
            if (amount < 0) {
                "-${currency.signo}${formattedAmount.substring(1, formattedAmount.length)}"
            } else {
                "${currency.signo}$formattedAmount"
            }
        }

        else -> "$formattedAmount ${currency.signo}"
    }
}

fun exchangeViewsPositions(root: ViewGroup, amountView: View, currencyView: View) {
    val firstPosition = root.getChildPosition(amountView)
    root.removeView(currencyView)
    root.addView(currencyView, firstPosition)
}