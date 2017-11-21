package melerospaw.deudapp.iu.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_time_picker.*
import melerospaw.deudapp.R
import melerospaw.deudapp.utils.inflate
import java.util.*

class DialogFechaDeuda : DialogFragment() {

    private lateinit var fecha: Date
    var positiveCallback: PositiveCallback? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fecha = Date(arguments!!.getLong(BUNDLE_FECHA))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflate(inflater, R.layout.dialog_time_picker, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val cal = Calendar.getInstance()
        cal.time = fecha
        dpFecha.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null)

        tvCambiar.setOnClickListener {
            guardar()
        }
        tvCancelar.setOnClickListener {
            dismiss()
        }
    }

    private fun guardar() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, dpFecha.year)
        cal.set(Calendar.MONTH, dpFecha.month)
        cal.set(Calendar.DAY_OF_MONTH, dpFecha.dayOfMonth)
        positiveCallback?.guardarFecha(cal.time)
        dismiss()
    }

    interface PositiveCallback {
        fun guardarFecha(fecha: Date)
    }
}