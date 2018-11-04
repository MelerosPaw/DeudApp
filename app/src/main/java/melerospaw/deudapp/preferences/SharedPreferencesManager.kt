package melerospaw.deudapp.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import melerospaw.deudapp.R
import melerospaw.deudapp.utils.Currency

class SharedPreferencesManager(context: Context) {

        private val PREF_NO_ES_PRIMERA_VEZ = "PRIMERA_VEZ"
        private val PREF_DEBE_MOSTRAR_DIALOG = "DEBE_MOSTRAR_DIALOG"
        private val PREF_MUST_SHOW_TUTORIAL = "DEBE_MOSTRAR_TUTORIAL"
        private val PREF_MUST_SHOW_IGNORE_TUTORIAL = "DEBE_MOSTRAR_IGNORAR_TUTORIAL"
        val PREF_CURRENCY = context.getString(R.string.clave_preferencia_moneda)

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setMustShowExplanatoryDialog(mustShow: Boolean) {
        sharedPreferences.editar {
            putBoolean(PREF_DEBE_MOSTRAR_DIALOG, mustShow)
        }
    }

    fun mustShowExplanatoryDialog() = sharedPreferences.getBoolean(PREF_DEBE_MOSTRAR_DIALOG, true)

    fun setNoEsPrimeraVez(noEsPrimeraVez: Boolean) = sharedPreferences.editar {
        putBoolean(PREF_NO_ES_PRIMERA_VEZ, noEsPrimeraVez)
    }

    fun esPrimeraVez() = !sharedPreferences.getBoolean(PREF_NO_ES_PRIMERA_VEZ, false)

    fun mustShowSwipeTutorial() = sharedPreferences.getBoolean(PREF_MUST_SHOW_TUTORIAL, true)

    fun setMustShowSwipeTutorial(mustShow: Boolean) {
        sharedPreferences.editar {
            putBoolean(PREF_MUST_SHOW_TUTORIAL, mustShow)
        }
    }

    fun mustShowIgnoreSwipeTutorial() = sharedPreferences.getBoolean(PREF_MUST_SHOW_IGNORE_TUTORIAL, false)

    fun setMustShowIgnoreSwipeTutorial(mustShow: Boolean) {
        sharedPreferences.editar {
            putBoolean(PREF_MUST_SHOW_IGNORE_TUTORIAL, mustShow)
        }
    }

    fun setCurrency(currency: Currency) {
        sharedPreferences.editar { putString(PREF_CURRENCY, currency.signo) }
    }

    fun getCurrency() : String = sharedPreferences.getString(PREF_CURRENCY, Currency.EURO.signo)!!

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun SharedPreferences.editar(block: SharedPreferences.Editor.() -> SharedPreferences.Editor) {
        this.edit().block().apply()
    }
    }