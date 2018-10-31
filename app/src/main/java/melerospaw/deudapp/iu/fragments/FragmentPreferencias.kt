package melerospaw.deudapp.iu.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import melerospaw.deudapp.R
import melerospaw.deudapp.preferences.SharedPreferencesManager
import melerospaw.deudapp.utils.Currency
import melerospaw.deudapp.utils.TextDrawableManager

class FragmentPreferencias: PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        val TAG: String = FragmentPreferencias::class.java.simpleName
    }

    lateinit var sharedPreferencesManager: SharedPreferencesManager
    lateinit var currencyPreference: ListPreference

    override fun onCreatePreferences(savedStateInstance: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        sharedPreferencesManager.registerOnSharedPreferenceChangeListener(this)
        currencyPreference = findPreference(sharedPreferencesManager.PREF_CURRENCY) as ListPreference
        currencyPreference.isPersistent = false
        currencyPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
            _, newValue ->
            val realValue = newValue.toString().replace(Regex("[\\p{Cf}]"), "")
            currencyPreference.value = realValue
            sharedPreferencesManager.setCurrency(Currency.getCurrencyBySign(realValue))
            false
        }
        setCurrencyIcon()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
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
        currencyPreference.icon = currencyDrawable
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferencesManager.unregisterOnSharedPreferenceChangeListener(this)
    }
}
