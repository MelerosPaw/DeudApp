package melerospaw.deudapp.iu.dialogs

import android.animation.LayoutTransition
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_editar_deuda.*
import melerospaw.deudapp.R
import melerospaw.deudapp.data.GestorDatos
import melerospaw.deudapp.modelo.Entidad
import melerospaw.deudapp.utils.*
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
        fun newInstance(idEntidad: Int, posicion: Int): DialogEditarDeuda {
            val df = DialogEditarDeuda()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_ENTIDAD, idEntidad)
            bundle.putInt(BUNDLE_POSICION, posicion)
            df.arguments = bundle
            return df
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gestor = GestorDatos.getGestor(context)
        posicion = if (arguments != null) arguments!!.getInt(BUNDLE_POSICION, posicion) else posicion
        var idEntidad = if (arguments != null) arguments!!.getInt(BUNDLE_ENTIDAD) else 0
        entidad = gestor.getEntidad(idEntidad)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflate(inflater, R.layout.dialog_editar_deuda, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(entidad) {
            tvFecha.text = readableDate
            etConcepto.setText(concepto)
            etCantidad.setText(DecimalFormatUtils.decimalToStringIfZero(Math.abs(cantidad), 2, ".", ","))
            etCantidad.setTextColor(ContextCompat.getColor(context!!,
                    if (entidad.tipoEntidad == Entidad.DEUDA) R.color.red else R.color.green))
            btnGuardar.setOnClickListener {
                guardar()
            }
            btnCancelar.setOnClickListener {
                dismiss()
            }
            ibCambiarFecha.setOnClickListener {
                mostrarDialogFecha()
            }
        }
        if (Build.VERSION.SDK_INT >= 16) {
            flRoot.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
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
            positiveCallback?.guardar(posicion, entidad)
            dismiss()
        }
    }

    private fun verificarDatos() =
        when {
            etConcepto.text.toString().isBlank() -> {
                shortToast(getString(R.string.concepto_vacio))
                false
            }
            etCantidad.texto.isBlank() -> {
                shortToast(getString(R.string.cantidad_vacia))
                false
            }
            StringUtils.convertible(StringUtils.prepararDecimal(etCantidad.texto)) == "string" -> {
                shortToast(getString(R.string.cantidad_no_numerica))
                false
            }
            estaRepetida() -> {
                longToast(String.format(getString(R.string.nombre_repetido),
                        entidad.persona.nombre, etConcepto.texto))
                false
            }
            else -> true
        }

    private fun estaRepetida() : Boolean =
        when {
            entidad.concepto == etConcepto.texto -> false
            else -> {
                val persona = entidad.persona
                persona.entidades.any{ it.concepto == etConcepto.texto }
            }
    }

    private fun mostrarDialogFecha() {
        if (activity != null) {
            val fm = activity!!.supportFragmentManager
            val ft = fm.beginTransaction().addToBackStack(DialogFechaDeuda.TAG)
            val dialog = DialogFechaDeuda.newInstance(Entidad.formatearFecha(tvFecha.text.toString()).time)
            dialog.positiveCallback = object : DialogFechaDeuda.PositiveCallback {
                override fun guardarFecha(fecha: Date) {
                    tvFecha.text = Entidad.formatearFecha(fecha)
                }
            }
            dialog.show(ft, DialogoModificarCantidad.TAG)
        }
    }

    interface PositiveCallback {
        fun guardar(position: Int, entidad: Entidad)
    }
}
