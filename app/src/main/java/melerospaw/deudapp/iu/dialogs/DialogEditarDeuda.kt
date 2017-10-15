package melerospaw.deudapp.iu.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import kotlinx.android.synthetic.main.dialog_editar_deuda.*
import melerospaw.deudapp.R
import melerospaw.deudapp.data.GestorDatos
import melerospaw.deudapp.modelo.Entidad
import melerospaw.deudapp.utils.*
import android.view.MenuInflater
import melerospaw.deudapp.task.BusProvider
import melerospaw.deudapp.task.EventoDeudaModificada
import java.util.*


class DialogEditarDeuda : DialogFragment() {

    private lateinit var entidad: Entidad
    private var posicion: Int = -1
    private lateinit var gestor: GestorDatos
    var positiveCallback: PositiveCallback? = null

    companion object {

        @JvmStatic
        val TAG = DialogEditarDeuda::class.java.simpleName
        private val BUNDLE_ENTIDAD = "ENTIDAD"
        private val BUNDLE_POSICION = "POSITION"

        @JvmStatic
        fun newInstance(entidad: Entidad, posicion: Int): DialogEditarDeuda {
            val df = DialogEditarDeuda()
            val bundle = Bundle()
            bundle.putSerializable(BUNDLE_ENTIDAD, entidad)
            bundle.putInt(BUNDLE_POSICION, posicion)
            df.arguments = bundle
            return df
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gestor = GestorDatos.getGestor(context)
        entidad = arguments.getSerializable(BUNDLE_ENTIDAD) as Entidad
        posicion = arguments.getInt(BUNDLE_POSICION)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflate(inflater, R.layout.dialog_editar_deuda, container)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        with(entidad) {
            tvFecha.text = readableDate
            etConcepto.setText(concepto)
            etCantidad.setText(DecimalFormatUtils.decimalToStringIfZero(Math.abs(cantidad), 2, ".", ","))
            etCantidad.setTextColor(ContextCompat.getColor(context,
                    if (entidad.tipoEntidad == Entidad.DEUDA) R.color.red else R.color.green))
            btnGuardar.setOnClickListener {
                guardar()
            }
            btnCancelar.setOnClickListener {
                dismiss()
            }
            tvFecha.setOnClickListener {
                mostrarDialogFecha()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ScreenUtils.pantallaCompleta(this, true)
    }

    private fun guardar() {
        if (verificarDatos()) {
            with(entidad) {
                fecha = Entidad.formatearFecha(tvFecha.text.toString())
                concepto = etConcepto.texto
                cantidad = StringUtils.prepararDecimal(etCantidad.texto).toFloat()
            }
            if (gestor.actualizarEntidad(entidad)) {
                positiveCallback?.guardar(posicion, entidad)
                dismiss()
                shortToast(getString(R.string.deuda_modificada))
            } else {
                shortToast(getString(R.string.problema_guardar_deuda))
            }
        }
    }

    private fun verificarDatos(): Boolean {
        var correcto: Boolean

        when {
            etConcepto.text.toString().isBlank() -> {
                correcto = false
                shortToast(getString(R.string.concepto_vacio))
            }
            etCantidad.texto.isBlank() -> {
                correcto = false
                shortToast(getString(R.string.cantidad_vacia))
            }
            StringUtils.convertible(StringUtils.prepararDecimal(etCantidad.texto)) == "string" -> {
                correcto = false
                shortToast(getString(R.string.cantidad_no_numerica))
            }
            else -> correcto = true
        }

        return correcto
    }

    private fun mostrarDialogFecha() {
        val fm = activity.supportFragmentManager
        val ft = fm.beginTransaction().addToBackStack(DialogFechaDeuda.TAG)
        val dialog = DialogFechaDeuda.newInstance(entidad.fecha.time)
        dialog.positiveCallback = object : DialogFechaDeuda.PositiveCallback {
            override fun guardarFecha(fecha: Date) {
                tvFecha.text = Entidad.formatearFecha(fecha)
            }
        }
        dialog.show(ft, DialogoModificarCantidad.TAG)
    }

    interface PositiveCallback {
        fun guardar(position: Int, entidad: Entidad)
    }
}
