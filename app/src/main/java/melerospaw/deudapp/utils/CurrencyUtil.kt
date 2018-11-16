package melerospaw.deudapp.utils

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import melerospaw.deudapp.preferences.SharedPreferencesManager

fun getCurrency(context: Context) = SharedPreferencesManager(context).currency

fun setUpAmount(context: Context, amount: Float? = null, rootView: ViewGroup, amountView: TextView,
                currencyView: TextView) {
    val currency = getCurrency(context)

    if (amount != null) {
        amountView.text = formatAmount(currency, amount)
        currencyView.text = formatCurrency(currency, amount)
    } else {
        currencyView.text = currency.signo
    }

    if (symbolPrecedesAmount(currency)) {
        setCurrencyViewBeforeAmountView(rootView, amountView, currencyView)
    }
}

fun formatAmountWithoutCurrencyPosition(context: Context? = null, currency: Currency? = null,
                                        amount: Float): String {
    if (context == null && currency == null) {
        throw IllegalArgumentException("formatAmountWithoutCurrencyPosition(): Either " +
                "context or currency must not be null.")
    }

    val twoDecimals = 2
    val dot = "."
    val desiredDecimalCharacter =
            currency?.caracterDecimal?.caracter?: getCurrency(context!!).caracterDecimal.caracter

    return DecimalFormatUtils.decimalToStringIfZero(amount, twoDecimals, dot,
            desiredDecimalCharacter.toString())
}

private fun formatAmount(currency: Currency, amount: Float): String {

    val formattedAmount = formatAmountWithoutCurrencyPosition(currency = currency, amount = amount)
    val isNegative = amount < 0

    return if (isNegative && symbolPrecedesAmount(currency)) {
        formattedAmount.substring(1, formattedAmount.length)
    } else {
        formattedAmount
    }
}

private fun formatCurrency(currency: Currency, amount: Float): String {
    val symbol = currency.signo
    val isNegative = amount < 0

    val symbolPlusNegative = addNegative(isNegative, currency, symbol)
    return addSpace(symbolPlusNegative, currency)
}

private fun addNegative(isNegative: Boolean, currency: Currency, symbol: String) =
        if (isNegative && symbolPrecedesAmount(currency)) {
            "-$symbol"
        } else {
            symbol
        }

private fun addSpace(symbol: String, currency: Currency) =
        when (currency.posicion) {
            Currency.Position.DELANTE_CON_ESPACIO -> "$symbol "
            Currency.Position.DETRAS -> " $symbol"
            else -> symbol
        }

fun setCurrencyViewBeforeAmountView(root: ViewGroup, amountView: TextView, currencyView: TextView) {
    val amountViewPosition = root.getChildPosition(amountView)
    val currencyViewPosition = root.getChildPosition(currencyView)
    if (amountViewPosition < currencyViewPosition) {
        root.removeView(currencyView)
        root.addView(currencyView, amountViewPosition)
    }
}

fun symbolPrecedesAmount(currency: Currency) =
        currency.posicion == Currency.Position.DELANTE_CON_ESPACIO ||
                currency.posicion == Currency.Position.DELANTE_SIN_ESPACIO