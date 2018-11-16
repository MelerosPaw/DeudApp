package melerospaw.deudapp.iu.dialogs

import android.animation.LayoutTransition
import android.content.DialogInterface
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
        val idEntidad = if (arguments != null) arguments!!.getInt(BUNDLE_ENTIDAD) else 0
        entidad = gestor.getEntidad(idEntidad)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflate(inflater, R.layout.dialog_editar_deuda, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(entidad) {
            tvFecha.text = readableDate
            etConcepto.setText(concepto)
            et_cantidad.setText(DecimalFormatUtils.decimalToStringIfZero(cantidad , 2, ".", ","))
            et_cantidad.setTextColor(ContextCompat.getColor(context!!,
                    if (entidad.tipoEntidad == Entidad.DEUDA) R.color.red else R.color.green))
        }

        setUpAmount(context = requireContext(), rootView = llCurrencyRoot, amountView = et_cantidad,
                currencyView = tvMoneda)
        btnGuardar.setOnClickListener { guardar() }
        btnCancelar.setOnClickListener { dismiss() }
        tvCambiarFecha.setOnClickListener {mostrarDialogFecha() }

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

                fecha = Entidad.formatearFecha(tvFecha.texto)
                concepto = etConcepto.texto

                val auxCantidad = et_cantidad.texto
                if (auxCantidad.isInfinityCharacter() ||
                        StringUtils.prepararDecimal(auxCantidad).isInfiniteFloat()) {
                    if (!cantidad.isInfiniteFloat()) {
                        mostrarDialogCantidadInfinita()
                    } else {
                        cantidad = getInfiniteFloatByDebtType(entidad)
                        closeAndSave()
                    }
                } else {
                    cantidad = StringUtils.prepararDecimal(auxCantidad).toFloat()
                    closeAndSave()
                }
            }
        }
    }

    private fun verificarDatos() : Boolean {
        val concepto = etConcepto.texto
        val cantidad = et_cantidad.texto
        val fecha = tvFecha.texto

        when {
            concepto.isBlank() -> {
                shortToast(getString(R.string.concepto_vacio))
                return false
            }

            cantidad.isBlank() -> {
                shortToast(getString(R.string.cantidad_vacia))
                return false
            }

            StringUtils.esConvertible(StringUtils.prepararDecimal(cantidad)) == "string" &&
                    !cantidad.isInfinityCharacter() -> {
                shortToast(getString(R.string.cantidad_no_numerica))
                return false
            }

            esLaMisma(concepto, Entidad.formatearFecha(fecha)) -> {
                shortToast(getString(R.string.sin_cambios))
                return false
            }

            estaRepetida(concepto, Entidad.formatearFecha(fecha)) -> {
                longToast(String.format(getString(R.string.nombre_repetido),
                        entidad.persona.nombre, concepto, fecha))
                return false
            }

            else -> return true
        }
    }

    private fun esLaMisma(concepto: String, fecha: Date) =
            entidad.concepto == concepto && entidad.esMismoDia(fecha)

    private fun estaRepetida(concepto: String, fecha: Date): Boolean {
        val fakeDebt = Entidad(0F, concepto, Entidad.DEUDA)
        fakeDebt.fecha = fecha
        return estaContenida(fakeDebt, entidad.persona.entidades)
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

    private fun getInfiniteFloatByDebtType(entidad: Entidad) =
            if (entidad.tipoEntidad == Entidad.DEUDA) {
                getNegativeInfiniteFloat()
            } else {
                getInfiniteFloat()
            }

    private fun mostrarDialogCantidadInfinita() {
        if (context != null) {
            mostrarInfinityDialog(context!!, positiveCallback = DialogInterface.OnClickListener {
                _, _->
                    entidad.cantidad = getInfiniteFloatByDebtType(entidad)
                    closeAndSave()
            }, negativeCallback = DialogInterface.OnClickListener { _,_ -> dismiss() })
        }
    }

    private fun closeAndSave() {
        positiveCallback?.guardar(posicion, entidad)
        dismiss()
    }

    interface PositiveCallback {
        fun guardar(position: Int, entidad: Entidad)
    }
}
