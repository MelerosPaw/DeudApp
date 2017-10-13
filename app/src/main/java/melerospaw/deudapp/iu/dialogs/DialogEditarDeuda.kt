package melerospaw.deudapp.iu.dialogs

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
                shortToast("La deuda se ha modificado")
            } else {
                shortToast("Se ha producido un problema al guardar la deuda")
            }
        }
    }

    private fun verificarDatos(): Boolean {
        var correcto: Boolean

        when {
            etConcepto.text.toString().isBlank() -> {
                correcto = false
                shortToast("El concepto no puede quedarse vacío")
            }
            etCantidad.texto.isBlank() -> {
                correcto = false
                shortToast("La cantidad no puede quedarse vacía")
            }
            StringUtils.convertible(StringUtils.prepararDecimal(etCantidad.texto)) == "string" -> {
                correcto = false
                shortToast("La cantidad tiene que ser un número")
            }
            else -> correcto = true
        }

        return correcto
    }

    interface PositiveCallback {
        fun guardar(position: Int, entidad: Entidad)
    }
}
