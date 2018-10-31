package melerospaw.deudapp.iu.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.preference.PreferenceFragmentCompat
import melerospaw.deudapp.R
import melerospaw.deudapp.utils.SharedPreferencesManager
import melerospaw.deudapp.utils.TextDrawableManager

@Suppress("DEPRECATION")
class FragmentPreferencias: PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        val TAG: String = FragmentPreferencias::class.java.simpleName
    }

    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreatePreferences(savedStateInstance: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        setCurrencyIcon()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // TODO 31/10/2018 Poner persistent a false y obtener el value del seleccionado mediante la PReference de tipo List
        if (context != null && key != null) {
            if (key == requireContext().getString(R.string.clave_preferencia_moneda)) {
                setCurrencyIcon()
            }
        }
    }

    private fun setCurrencyIcon() {
        val currencyDrawable = TextDrawableManager.createDrawable(
                sharedPreferencesManager.getCurrency(), android.R.color.transparent,
                R.color.colorPrimary)
        findPreference(sharedPreferencesManager.PREF_CURRENCY).icon = currencyDrawable

    }
}
