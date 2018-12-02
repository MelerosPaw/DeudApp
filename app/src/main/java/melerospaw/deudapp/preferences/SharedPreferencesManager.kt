package melerospaw.deudapp.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import melerospaw.deudapp.R
import melerospaw.deudapp.utils.Currency
import melerospaw.deudapp.utils.editar

class SharedPreferencesManager(context: Context) {

    companion object {
        private const val PREF_ES_PRIMERA_VEZ = "PRIMERA_VEZ"
        private const val PREF_DEBE_MOSTRAR_DIALOG = "DEBE_MOSTRAR_DIALOG"
        private const val PREF_MUST_SHOW_TUTORIAL = "DEBE_MOSTRAR_TUTORIAL"
        private const val PREF_MUST_SHOW_IGNORE_TUTORIAL = "DEBE_MOSTRAR_IGNORAR_TUTORIAL"
    }

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val prefCurrencyKey: String = context.getString(R.string.clave_preferencia_moneda)
    var currency: Currency
        get() = Currency.getCurrencyByDescription(sharedPreferences.getString(prefCurrencyKey, Currency.EURO.descripcion)!!)
        set(currency) = sharedPreferences.editar { putString(prefCurrencyKey, currency.descripcion) }
    var isFirstTime: Boolean
        get() = sharedPreferences.getBoolean(PREF_ES_PRIMERA_VEZ, true)
        set(isFirstTime) = sharedPreferences.editar { putBoolean(PREF_ES_PRIMERA_VEZ, isFirstTime) }
    var isShowExplanatoryDialog: Boolean
        get() = sharedPreferences.getBoolean(PREF_DEBE_MOSTRAR_DIALOG, true)
        set(mustShow) = sharedPreferences.editar { putBoolean(PREF_DEBE_MOSTRAR_DIALOG, mustShow) }
    var isShowSwipeTutorial: Boolean
        get() = sharedPreferences.getBoolean(PREF_MUST_SHOW_TUTORIAL, true)
        set(mustShow) = sharedPreferences.editar { putBoolean(PREF_MUST_SHOW_TUTORIAL, mustShow) }
    var isShowIgnoreSwipeTutorial: Boolean
        get() = sharedPreferences.getBoolean(PREF_MUST_SHOW_IGNORE_TUTORIAL, false)
        set(mustShow) = sharedPreferences.editar { putBoolean(PREF_MUST_SHOW_IGNORE_TUTORIAL, mustShow) }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun applyFix() {
        if (!sharedPreferences.getBoolean("FIX_APPLIED", false)) {
            val storedCurrencyValue: String? = sharedPreferences.getString(prefCurrencyKey, null)
            var found = false
            for (currency: Currency in Currency.values()) {
                if (currency.signo == storedCurrencyValue) {
                    this.currency = currency
                    found = true
                    break
                }
            }
            if (!found) {
                this.currency = Currency.EURO
            }

            sharedPreferences.edit().putBoolean("FIX_APPLIED", true).apply()
        }
    }
}