package melerospaw.deudapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SharedPreferencesManager(context: Context) {

    private val prefNoEsPrimeraVez = "PRIMERA_VEZ"
    private val prefDebeMostrarDialog = "DEBE_MOSTRAR_DIALOG"
    private val prefMustShowTutorial = "DEBE_MOSTRAR_TUTORIAL"
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setMustShowExplanatoryDialog(mustShow: Boolean) {
        sharedPreferences.editar {
            putBoolean(prefDebeMostrarDialog, mustShow)
        }
    }

    fun mustShowExplanatoryDialog() = sharedPreferences.getBoolean(prefDebeMostrarDialog, true)

    fun setNoEsPrimeraVez(noEsPrimeraVez: Boolean) = sharedPreferences.editar {
        putBoolean(prefNoEsPrimeraVez, noEsPrimeraVez)
    }

    fun esPrimeraVez() = !sharedPreferences.getBoolean(prefNoEsPrimeraVez, false)

    fun mustShowSwipeTutorial() = sharedPreferences.getBoolean(prefMustShowTutorial, true)

    fun setMustShowSwipeTutorial(mustShow: Boolean) {
        sharedPreferences.editar {
            putBoolean(prefMustShowTutorial, mustShow)
        }
    }

    private fun SharedPreferences.editar(block: SharedPreferences.Editor.() -> SharedPreferences.Editor) {
        this.edit().block().apply()
    }
}