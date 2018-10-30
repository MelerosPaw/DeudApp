package melerospaw.deudapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SharedPreferencesManager(context: Context) {

    companion object {
        private const val PREF_NO_ES_PRIMERA_VEZ = "PRIMERA_VEZ"
        private const val PREF_DEBE_MOSTRAR_DIALOG = "DEBE_MOSTRAR_DIALOG"
        private const val PREF_MUST_SHOW_TUTORIAL = "DEBE_MOSTRAR_TUTORIAL"
        private const val PREF_MUST_SHOW_IGNORE_TUTORIAL = "DEBE_MOSTRAR_IGNORAR_TUTORIAL"
        private const val PREF_CURRENCY = "CURRENCY"
    }

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

    fun setCurrency(currency: melerospaw.deudapp.utils.Currency) {
        sharedPreferences.editar { putString(PREF_CURRENCY, currency.signo) }
    }

    fun getCurrency() = sharedPreferences.getString(PREF_CURRENCY, Currency.EURO.signo)

    private fun SharedPreferences.editar(block: SharedPreferences.Editor.() -> SharedPreferences.Editor) {
        this.edit().block().apply()
    }
}