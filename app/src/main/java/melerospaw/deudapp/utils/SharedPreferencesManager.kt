package melerospaw.deudapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SharedPreferencesManager(context: Context) {

    private val prefNoEsPrimeraVez = "PRIMERA_VEZ"
    private val prefDebeMostrarDialog = "DEBE_MOSTRAR_DIALOG"
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun setMustShowExplanatoryDialog(mustShow: Boolean) = sharedPreferences.edit().putBoolean(prefDebeMostrarDialog, mustShow).apply()

    fun mustShowExplanatoryDialog() = sharedPreferences.getBoolean(prefDebeMostrarDialog, true)

    fun setNoEsPrimeraVez(noEsPrimeraVez: Boolean) = sharedPreferences.edit().putBoolean(prefNoEsPrimeraVez, noEsPrimeraVez).apply()

    fun esPrimeraVez() = !sharedPreferences.getBoolean(prefNoEsPrimeraVez, false)
}