package melerospaw.deudapp.preferences

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import melerospaw.deudapp.R
import melerospaw.deudapp.utils.Currency
import melerospaw.deudapp.utils.removeUnicodeInvisibleCharacters
import melerospaw.deudapp.utils.toDpi
import java.util.Arrays


class CustomListPreference : ListPreference {

    private var currencyIcon: TextView? = null
    private var currencySummary: TextView? = null
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private val isHolderInitialized
        get() = currencyIcon != null && currencySummary != null

    constructor(context: Context?) : super(context) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        initialize()
    }

    private fun initialize() {
        sharedPreferencesManager = SharedPreferencesManager(context)
        isPersistent = false
        onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            value = newValue as String
            val realValue = newValue.removeUnicodeInvisibleCharacters()
            sharedPreferencesManager.currency = Currency.getCurrencyByDescription(realValue)
            if (isHolderInitialized) {
                setUpCurrencySummary(realValue)
                setCurrencyIcon(realValue)
            }
            false
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        if (!isHolderInitialized) {
            holder?.let {
                currencyIcon = it.findViewById(R.id.tv_icon) as TextView
                currencySummary = it.findViewById(R.id.tv_summary) as TextView
            }

            val currentValue = sharedPreferencesManager.currency
            setCurrencyIcon(currentValue.signo)
            setUpCurrencySummary(currentValue.nombreCompleto)
        }
    }

    private fun setUpCurrencySummary(currentValue: String) {
//        val valueIndex = findIndexOfValueNonUnicodeCharacters(currentValue)
//        currencySummary?.text = entries[valueIndex]
        currencySummary?.text = currentValue
    }

    private fun setCurrencyIcon(signo: String) {
        currencyIcon!!.textSize = resizeTextSize(signo)
        currencyIcon!!.text = signo
    }

    private fun resizeTextSize(currentText: String): Float {

        var currentTextSize = currencyIcon!!.textSize
        val availableWidth = getCurrencyWidth()
        val mustDecrease = doesTextExceedsAvailableWidth(availableWidth, currentText, currentTextSize)

        while (mustResize(mustDecrease, currentText, currentTextSize, availableWidth)) {
            currentTextSize = if (mustDecrease) currentTextSize - 2 else currentTextSize + 2
        }

        return currentTextSize
    }

    private fun getCurrencyWidth() =
            if (currencyIcon!!.width != 0) {
                currencyIcon!!.width
            } else {
                val params = currencyIcon!!.layoutParams
                currencyIcon!!.measure(params.width, params.height)
                currencyIcon!!.measuredWidth
            }

    private fun doesTextExceedsAvailableWidth(maxAllowedWidth: Int, currentText: String,
                                              currentTextSize: Float): Boolean {
        val paint = Paint()
        paint.textSize = currentTextSize.toDpi(context)
        paint.typeface = Typeface.DEFAULT_BOLD
        return paint.measureText(currentText) > maxAllowedWidth.toDouble()
    }

    private fun mustResize(mustDecrease: Boolean, currentText: String, currentTextSize: Float,
                           maxAllowedWidth: Int): Boolean {

        val isTextExceedingWidth = doesTextExceedsAvailableWidth(maxAllowedWidth, currentText,
                currentTextSize)
        return (isTextExceedingWidth && mustDecrease) || (!isTextExceedingWidth && !mustDecrease)
    }







    fun splitWordsIntoStringsThatFit(source: String, maxWidthPx: Float, paint: Paint): List<String> {

        val result = ArrayList<String>()
        val currentLine = ArrayList<String>()
        val sources = source.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (chunk in sources) {
            if (paint.measureText(chunk) < maxWidthPx) {
                processFitChunk(maxWidthPx, paint, result, currentLine, chunk)
            } else {
                //the chunk is too big, split it.
                val splitChunk = splitIntoStringsThatFit(chunk, maxWidthPx, paint)
                for (chunkChunk in splitChunk) {
                    processFitChunk(maxWidthPx, paint, result, currentLine, chunkChunk)
                }
            }
        }

        if (!currentLine.isEmpty()) {
            result.add(TextUtils.join(" ", currentLine))
        }
        return result
    }

    /**
     * Splits a string to multiple strings each of which does not exceed the width
     * of maxWidthPx.
     */
    private fun splitIntoStringsThatFit(source: String, maxWidthPx: Float, paint: Paint): List<String> {
        if (TextUtils.isEmpty(source) || paint.measureText(source) <= maxWidthPx) {
            return Arrays.asList(source)
        }

        val result = ArrayList<String>()
        var start = 0
        for (i in 1..source.length) {
            val substr = source.substring(start, i)
            if (paint.measureText(substr) >= maxWidthPx) {
                //this one doesn't fit, take the previous one which fits
                val fits = source.substring(start, i - 1)
                result.add(fits)
                start = i - 1
            }
            if (i == source.length) {
                val fits = source.substring(start, i)
                result.add(fits)
            }
        }

        return result
    }

    /**
     * Processes the chunk which does not exceed maxWidth.
     */
    private fun processFitChunk(maxWidth: Float, paint: Paint, result: ArrayList<String>,
                                currentLine: ArrayList<String>, chunk: String) {
        currentLine.add(chunk)
        val currentLineStr = TextUtils.join(" ", currentLine)
        if (paint.measureText(currentLineStr) >= maxWidth) {
            //remove chunk
            currentLine.removeAt(currentLine.size - 1)
            result.add(TextUtils.join(" ", currentLine))
            currentLine.clear()
            //ok because chunk fits
            currentLine.add(chunk)
        }
    }
}