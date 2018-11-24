package melerospaw.deudapp.iu.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.toolbar.*
import melerospaw.deudapp.R
import melerospaw.deudapp.iu.fragments.FragmentPreferencias


class ActivityPreferencias: AppCompatActivity() {

    companion object {

        @JvmStatic fun start(context: Context) {
            context.startActivity(Intent(context, ActivityPreferencias::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferencias)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fl_root_currency_prefs, FragmentPreferencias(), FragmentPreferencias.TAG)
                .commit()
    }
}