package melerospaw.deudapp.preferences

import android.content.Context
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.widget.TextView
import melerospaw.deudapp.R
import melerospaw.deudapp.utils.Currency
import melerospaw.deudapp.utils.findIndexOfValueNonUnicodeCharacters
import melerospaw.deudapp.utils.getCurrency

class CustomListPreference : ListPreference {

    private lateinit var currencyIcon: TextView
    private lateinit var currencySummary: TextView
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

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

    fun initialize() {
        sharedPreferencesManager = SharedPreferencesManager(context)
        isPersistent = false
        onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            value = newValue as String
            val realValue = (newValue).replace(Regex("[\\p{Cf}]"), "")
            setUpCurrencySummary(realValue)
            sharedPreferencesManager.setCurrency(Currency.getCurrencyBySign(realValue))
            false
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        holder?.let {
            currencyIcon = it.findViewById(R.id.tv_icon) as TextView
            currencySummary = it.findViewById(R.id.tv_summary) as TextView
        }

        val currentValue = sharedPreferencesManager.getCurrency()
        setCurrencyIcon(currentValue)
        setUpCurrencySummary(currentValue)
    }

    private fun setUpCurrencySummary(currentValue: String) {
        val valueIndex = findIndexOfValueNonUnicodeCharacters(currentValue)
        currencySummary.text = entries[valueIndex]
    }

    private fun setCurrencyIcon(currentValue: String) {
        currencyIcon.text = currentValue
    }

    private fun setValue() {
        value = sharedPreferencesManager.getCurrency()
    }
}