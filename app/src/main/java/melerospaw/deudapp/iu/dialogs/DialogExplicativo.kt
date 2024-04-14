package melerospaw.deudapp.iu.dialogs

import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import melerospaw.deudapp.R
import melerospaw.deudapp.preferences.SharedPreferencesManager
import melerospaw.deudapp.utils.hidden
import melerospaw.deudapp.utils.inflate

class DialogExplicativo : DialogFragment() {

    companion object {

        @JvmStatic
        val TAG = DialogExplicativo::class.java.simpleName
    }

    private var btnEntendido: Button? = null
    private var chkNoMostrarDialogo: CheckBox? = null
    private var tvDescripcion: TextView? = null

    var callback: PositiveCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflate(inflater, R.layout.dialog_explicativo, container).also { bindViews(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog.setTitle(getString(R.string.deuda_grupal))
        btnEntendido?.setOnClickListener {
            chkNoMostrarDialogo?.isChecked?.let { callback?.onDialogClosed(it) }
            dismiss()
        }
        val mensaje = SpannableString(getString(R.string.deuda_grupal_explicacion))
        mensaje.setSpan(StyleSpan(Typeface.BOLD), mensaje.indexOf("\""), mensaje.lastIndexOf("\"") + 1,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        tvDescripcion?.text = mensaje
        chkNoMostrarDialogo?.hidden(SharedPreferencesManager(context!!).isFirstTime)
    }

    private fun bindViews(view: View) {
        with(view) {
            btnEntendido = findViewById(R.id.btn_entendido)
            chkNoMostrarDialogo = findViewById(R.id.chk_no_mostrar_dialogo)
            tvDescripcion = findViewById(R.id.tv_descripcion)
        }
    }

    interface PositiveCallback {
        fun onDialogClosed(stopShowing: Boolean)
    }
}