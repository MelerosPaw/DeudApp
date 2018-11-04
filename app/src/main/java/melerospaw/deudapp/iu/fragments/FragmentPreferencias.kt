package melerospaw.deudapp.iu.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import melerospaw.deudapp.R
import melerospaw.deudapp.preferences.CustomListPreference
import melerospaw.deudapp.preferences.SharedPreferencesManager
import melerospaw.deudapp.task.BusProvider
import melerospaw.deudapp.task.EventoMonedaCambiada
import melerospaw.deudapp.utils.Currency

class FragmentPreferencias: PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        val TAG: String = FragmentPreferencias::class.java.simpleName
    }

    private lateinit var currencyPreference: CustomListPreference
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreatePreferences(savedStateInstance: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        currencyPreference = findPreference(sharedPreferencesManager.PREF_CURRENCY) as CustomListPreference
        sharedPreferencesManager.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferencesManager.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val newCurrency = Currency.getCurrencyBySign(sharedPreferencesManager.getCurrency())
        BusProvider.getBus().post(EventoMonedaCambiada(newCurrency))
    }
}
