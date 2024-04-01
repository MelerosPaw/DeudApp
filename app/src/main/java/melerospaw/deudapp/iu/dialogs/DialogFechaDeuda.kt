package melerospaw.deudapp.iu.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import melerospaw.deudapp.R
import melerospaw.deudapp.utils.inflate
import java.util.Calendar
import java.util.Date

class DialogFechaDeuda : DialogFragment() {

    companion object {

        @JvmStatic
        val TAG: String = DialogFechaDeuda::class.java.simpleName
        private val BUNDLE_FECHA = "FECHA"

        @JvmStatic
        fun newInstance(fecha: Long): DialogFechaDeuda {
            val df = DialogFechaDeuda()
            val bundle = Bundle()
            bundle.putLong(BUNDLE_FECHA, fecha)
            df.arguments = bundle
            return df
        }
    }

    private var dpFecha: DatePicker? = null
    private var tvCambiar: TextView? = null
    private var tvCancelar: TextView? = null

    private lateinit var fecha: Date
    var positiveCallback: PositiveCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fecha = Date(arguments!!.getLong(BUNDLE_FECHA))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflate(inflater, R.layout.dialog_time_picker, container).also {
        bindViews(it)
        bindViews(it)
        bindViews(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initDate()
        tvCambiar?.setOnClickListener { guardar() }
        tvCancelar?.setOnClickListener { dismiss() }
    }

    private fun bindViews(view: View) {
        with(view) {
            dpFecha = findViewById(R.id.dp_fecha)
            tvCambiar = findViewById(R.id.tv_cambiar)
            tvCancelar = findViewById(R.id.tv_cancelar)
        }
    }

    private fun initDate() {
        dpFecha?.let {
            val cal = Calendar.getInstance().apply { time = fecha }
            it.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null)
        }
    }

    private fun guardar() {
        dpFecha?.run {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            positiveCallback?.guardarFecha(cal.time)
            dismiss()
        }
    }

    interface PositiveCallback {
        fun guardarFecha(fecha: Date)
    }
}