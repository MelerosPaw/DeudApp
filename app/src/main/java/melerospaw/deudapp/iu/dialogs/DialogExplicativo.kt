package melerospaw.deudapp.iu.dialogs

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_explicativo.*
import melerospaw.deudapp.R
import melerospaw.deudapp.modelo.Entidad
import melerospaw.deudapp.utils.SharedPreferencesManager
import melerospaw.deudapp.utils.StringUtils
import melerospaw.deudapp.utils.inflate
import java.util.*

class DialogExplicativo : DialogFragment() {

    var callback: PositiveCallback? = null

    companion object {

        @JvmStatic
        val TAG = DialogExplicativo::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflate(inflater, R.layout.dialog_explicativo, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog.setTitle(getString(R.string.deuda_grupal))
        btnEntendido.setOnClickListener {
            callback?.onDialogClosed(chkNoMostrarDialogo.isChecked)
            dismiss()
        }
        val mensaje = SpannableString(getString(R.string.deuda_grupal_explicacion))
        mensaje.setSpan(StyleSpan(Typeface.BOLD), mensaje.indexOf("\""), mensaje.lastIndexOf("\"") + 1,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        tvDescripcion.text = mensaje
        chkNoMostrarDialogo.visibility = if (!SharedPreferencesManager(context!!).esPrimeraVez()) View.VISIBLE else View.GONE
    }

    interface PositiveCallback {
        fun onDialogClosed(stopShowing: Boolean)
    }
}