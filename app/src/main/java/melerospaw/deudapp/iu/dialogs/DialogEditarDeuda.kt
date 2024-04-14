package melerospaw.deudapp.iu.dialogs

import android.animation.LayoutTransition
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import melerospaw.deudapp.R
import melerospaw.deudapp.data.GestorDatos
import melerospaw.deudapp.modelo.Entidad
import melerospaw.deudapp.utils.DecimalFormatUtils
import melerospaw.deudapp.utils.ScreenUtils
import melerospaw.deudapp.utils.StringUtils
import melerospaw.deudapp.utils.doIfNotNull
import melerospaw.deudapp.utils.esRepetida
import melerospaw.deudapp.utils.estaContenida
import melerospaw.deudapp.utils.getInfiniteFloat
import melerospaw.deudapp.utils.getNegativeInfiniteFloat
import melerospaw.deudapp.utils.inflate
import melerospaw.deudapp.utils.isInfiniteFloat
import melerospaw.deudapp.utils.isInfinityCharacter
import melerospaw.deudapp.utils.longToast
import melerospaw.deudapp.utils.mostrarInfinityDialog
import melerospaw.deudapp.utils.setUpAmount
import melerospaw.deudapp.utils.shortToast
import melerospaw.deudapp.utils.texto
import java.util.Date


class DialogEditarDeuda : DialogFragment() {

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
    
    private var tvFecha: TextView? = null
    private var etConcepto: EditText? = null
    private var etCantidad: EditText? = null
    private var llCurrencyRoot: LinearLayout? = null
    private var tvMoneda: TextView? = null
    private var btnGuardar: Button? = null
    private var btnCancelar: Button? = null
    private var tvCambiarFecha: TextView? = null
    private var flRoot: FrameLayout? = null

    private lateinit var entidad: Entidad
    private var posicion: Int = -1
    private lateinit var gestor: GestorDatos
    var positiveCallback: PositiveCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gestor = GestorDatos.getGestor(context)
        posicion = if (arguments != null) arguments!!.getInt(BUNDLE_POSICION, posicion) else posicion
        val idEntidad = if (arguments != null) arguments!!.getInt(BUNDLE_ENTIDAD) else 0
        entidad = gestor.getEntidad(idEntidad)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflate(inflater, R.layout.dialog_editar_deuda, container)?.also {
        bindViews(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        with(entidad) {
            tvFecha?.text = readableDate
            etConcepto?.setText(concepto)
            etCantidad?.setText(DecimalFormatUtils.decimalToStringIfZero(cantidad , 2, ".", ","))
            etCantidad?.setTextColor(ContextCompat.getColor(context,
                    if (entidad.tipoEntidad == Entidad.DEUDA) R.color.red else R.color.green))
        }

        doIfNotNull(llCurrencyRoot, etCantidad, tvMoneda) { rootView, amountView, currencyView ->
            setUpAmount(context, null, rootView, amountView, currencyView)
        }

        btnGuardar?.setOnClickListener { guardar() }
        btnCancelar?.setOnClickListener { dismiss() }
        tvCambiarFecha?.setOnClickListener {mostrarDialogFecha() }

        if (Build.VERSION.SDK_INT >= 16) {
            flRoot?.layoutTransition?.enableTransitionType(LayoutTransition.CHANGING)
        }
    }

    override fun onStart() {
        super.onStart()
        ScreenUtils.pantallaCompleta(this, true)
    }

    private fun bindViews(view: View) {
        with(view) {
            tvFecha = findViewById(R.id.tv_fecha)
            etConcepto = findViewById(R.id.et_concepto)
            etCantidad = findViewById(R.id.et_cantidad)
            llCurrencyRoot = findViewById(R.id.ll_currency_root)
            tvMoneda = findViewById(R.id.tv_moneda)
            btnGuardar = findViewById(R.id.btn_guardar)
            btnCancelar = findViewById(R.id.btn_cancelar)
            tvCambiarFecha = findViewById(R.id.tv_cambiar_fecha)
            flRoot = findViewById(R.id.fl_root)
        }
    }

    private fun guardar() {
        if (verificarDatos()) {
            with(entidad) {

                fecha = Entidad.formatearFecha(tvFecha?.texto)
                concepto = etConcepto?.texto
                etCantidad?.texto?.let {
                    if (it.isInfinityCharacter() || StringUtils.prepararDecimal(it).isInfiniteFloat()) {
                        if (!cantidad.isInfiniteFloat()) {
                            mostrarDialogCantidadInfinita()
                        } else {
                            cantidad = getInfiniteFloatByDebtType(entidad)
                            closeAndSave()
                        }
                    } else {
                        cantidad = StringUtils.prepararDecimal(it).toFloat()
                        closeAndSave()
                    }
                }
            }
        }
    }

    private fun verificarDatos() : Boolean {
        val concepto = etConcepto?.texto
        val cantidad = etCantidad?.texto
        val fecha = tvFecha?.texto

        return when {
            concepto.isNullOrBlank() -> {
                shortToast(getString(R.string.concepto_vacio))
                false
            }

            cantidad.isNullOrBlank() -> {
                shortToast(getString(R.string.cantidad_vacia))
                false
            }

            StringUtils.esConvertible(StringUtils.prepararDecimal(cantidad)) == "string" &&
                    !cantidad.isInfinityCharacter() -> {
                shortToast(getString(R.string.cantidad_no_numerica))
                false
            }

            noHaCambiado(concepto, cantidad, Entidad.formatearFecha(fecha)) -> {
                shortToast(getString(R.string.sin_cambios))
                false
            }

            estaRepetida(concepto, Entidad.formatearFecha(fecha)) -> {
                longToast(String.format(getString(R.string.nombre_repetido),
                        entidad.persona.nombre, concepto, fecha))
                false
            }

            else -> true
        }
    }

    /** No ha cambiado si mantiene la cantidad, la fecha y el concepto iniciales.
     * @param concepto  El concepto nuevo.
     * @param cantidad  La cantidad nueva.
     * @param fecha     La fecha nueva.
     */
    private fun noHaCambiado(concepto: String, cantidad: String, fecha: Date): Boolean {
        val cantidadOriginal = DecimalFormatUtils.decimalToStringIfZero(entidad.cantidad, 2, ".", ",")
        return entidad.concepto == concepto &&
                cantidad == cantidadOriginal &&
                entidad.esMismoDia(fecha)
    }

    /**
     * Está repetida si, habiendo cambiado la fecha o el concepto, ahora es igual de otra que ya
     * tenga la persona.
     * @param concepto  El nuevo concepto
     * @param fecha     La nueva fecha.
     */
    private fun estaRepetida(concepto: String, fecha: Date): Boolean {
        val fakeDebt = Entidad(0F, concepto, Entidad.DEUDA)
        fakeDebt.fecha = fecha
        return !esRepetida(entidad, fakeDebt) &&
                estaContenida(fakeDebt, entidad.persona.entidades)
    }

    private fun mostrarDialogFecha() {
        if (activity != null) {
            val fm = activity!!.supportFragmentManager
            val ft = fm.beginTransaction().addToBackStack(DialogFechaDeuda.TAG)
            val dialog = DialogFechaDeuda.newInstance(Entidad.formatearFecha(tvFecha?.text.toString()).time)
            dialog.positiveCallback = object : DialogFechaDeuda.PositiveCallback {
                override fun guardarFecha(fecha: Date) {
                    tvFecha?.text = Entidad.formatearFecha(fecha)
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
