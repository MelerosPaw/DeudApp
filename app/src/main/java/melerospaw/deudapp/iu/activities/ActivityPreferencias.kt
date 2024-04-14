package melerospaw.deudapp.iu.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import melerospaw.deudapp.R
import melerospaw.deudapp.iu.fragments.FragmentPreferencias


class ActivityPreferencias : AppCompatActivity() {

    companion object {

        @JvmStatic
        fun start(context: Context) {
            context.startActivity(Intent(context, ActivityPreferencias::class.java))
        }
    }

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferencias)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_root_currency_prefs, FragmentPreferencias(), FragmentPreferencias.TAG)
            .commit()
    }
}