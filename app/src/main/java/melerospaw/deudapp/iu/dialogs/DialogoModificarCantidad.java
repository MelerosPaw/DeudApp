package melerospaw.deudapp.iu.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import melerospaw.deudapp.R;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.utils.CurrencyUtilKt;
import melerospaw.deudapp.utils.ExtensionFunctionsKt;
import melerospaw.deudapp.utils.InfinityManagerKt;
import melerospaw.deudapp.utils.StringUtils;

public class DialogoModificarCantidad extends DialogFragment {

    public static final String TAG = DialogoModificarCantidad.class.getSimpleName();
    public static final String TIPO_AUMENTAR = "Aumentar";
    public static final String TIPO_DISMINUIR = "Disminuir";
    public static final String TIPO_CANCELAR = "Cancelar";
    public static final String TIPO_CANCELAR_TODAS = "Cancelar todo";

    public static final String EXTRA_MODO = "MODO";
    public static final String EXTRA_POSICION = "POSICION";
    public static final String EXTRA_ENTIDAD = "EXTRA_ENTIDAD";

    private ViewGroup llCurrencyGroup;
    private TextView tvTitulo;
    private TextView tvMensaje;
    private EditText etCantidad;
    private TextView tvMoneda;
    private TextView btnAceptar;
    private TextView btnCancelar;

    private String modo;
    private int position;
    private Entidad entidad;
    private PositiveCallback positiveCallback;


    public static DialogoModificarCantidad getInstance(String modo, Entidad entidad,
                                                       Integer position) {
        DialogoModificarCantidad df = new DialogoModificarCantidad();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_MODO, modo);
        bundle.putInt(EXTRA_POSICION, position);
        bundle.putSerializable(EXTRA_ENTIDAD, entidad);
        df.setArguments(bundle);
        return df;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.modo = getArguments().getString(EXTRA_MODO);
            this.position = getArguments().getInt(EXTRA_POSICION);
            this.entidad = (Entidad) getArguments().getSerializable(EXTRA_ENTIDAD);
        }
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (inflater != null) {
            View v = inflater.inflate(R.layout.dialog_modificar_deuda_layout, container, false);
            bindViews(v);
            loadView();
            return v;
        } else {
            return null;
        }
    }

    private void bindViews(@NonNull View view) {
        llCurrencyGroup = view.findViewById(R.id.ll_currency_root);
        tvTitulo = view.findViewById(R.id.tv_titulo);
        tvMensaje = view.findViewById(R.id.tv_mensaje);
        etCantidad = view.findViewById(R.id.etCantidad);
        tvMoneda = view.findViewById(R.id.tv_moneda);
        btnAceptar = view.findViewById(R.id.tv_guardar);
        btnCancelar = view.findViewById(R.id.tv_cancelar);
    }

    public void loadView(){
        tvTitulo.setText(modo);
        CurrencyUtilKt.setUpAmount(requireContext(), null, llCurrencyGroup, etCantidad,
                tvMoneda);

        switch (modo) {
            case TIPO_AUMENTAR:
                loadViewAumentar();
                break;
            case TIPO_DISMINUIR:
                loadViewDisminuir();
                break;
            case TIPO_CANCELAR:
                loadViewCancelar();
                break;
            case TIPO_CANCELAR_TODAS:
                loadViewCancelarTodas();
                break;
            default:
                // NO-OP No more types
        }
    }

    private void loadViewAumentar() {
        tvMensaje.setText(entidad.getTipoEntidad() == Entidad.DEUDA ?
                R.string.pregunta_aumentar_deuda : R.string.pregunta_aumentar_derecho_cobro);
        btnAceptar.setText(R.string.aumentar);
        btnCancelar.setText(R.string.cancelar);
        btnCancelar.setOnClickListener(v -> dismiss());
        btnAceptar.setOnClickListener(v -> aumentar());
    }

    private void loadViewDisminuir() {
        tvMensaje.setText(entidad.getTipoEntidad() == Entidad.DEUDA ?
                R.string.pregunta_descontar_deuda : R.string.pregunta_descontar_derecho_cobro);
        btnAceptar.setText(R.string.disminuir);
        btnCancelar.setText(R.string.cancelar);
        btnCancelar.setOnClickListener(v -> dismiss());
        btnAceptar.setOnClickListener(v -> disminuir());
    }

    private void loadViewCancelar() {
        tvMensaje.setText(entidad.getTipoEntidad() == Entidad.DEUDA ?
                R.string.pregunta_cancelar_deuda : R.string.pregunta_cancelar_derecho_cobro);
        btnAceptar.setText(R.string.si);
        btnCancelar.setText(R.string.no);
        ExtensionFunctionsKt.hide(tvMoneda, etCantidad);
        btnCancelar.setOnClickListener(v -> dismiss());
        btnAceptar.setOnClickListener(v -> cancelar());
    }

    private void loadViewCancelarTodas() {
        tvTitulo.setText(modo);
        tvMensaje.setText(R.string.pregunta_cancelar_todas);
        btnAceptar.setText(R.string.si);
        btnCancelar.setText(R.string.no);
        ExtensionFunctionsKt.hide(tvMoneda, etCantidad);
        btnCancelar.setOnClickListener(v -> dismiss());
        btnAceptar.setOnClickListener(v -> cancelarTodas());
    }

    private void aumentar() {
        if (esCantidadCorrecta()) {
            proceder();
        }
    }

    private void disminuir() {
        if (esCantidadCorrecta()) {
            proceder();
        }
    }

    private void cancelar() {
        proceder();
    }

    private void cancelarTodas(){
        proceder();
    }

    private boolean esCantidadCorrecta() {

        boolean esCantidadCorrecta;

        String cantidad = etCantidad.getText().toString();
        if (cantidad.isEmpty()) {
            dismiss();
            esCantidadCorrecta = false;
        } else if (StringUtils.esConvertible(cantidad).equals("string")) {
            Snackbar.make(btnAceptar, R.string.mensaje_cantidad_no_valida, Snackbar.LENGTH_SHORT).show();
            esCantidadCorrecta = false;
        } else {
            esCantidadCorrecta = !esCantidadInfinita();
        }

        return esCantidadCorrecta;
    }

    private boolean esCantidadInfinita() {

        boolean isInfinite;
        float preparedDecimal = Float.parseFloat(StringUtils.prepararDecimal(etCantidad.getText().toString()));
        if (InfinityManagerKt.isInfiniteFloat(preparedDecimal) &&
                !InfinityManagerKt.isInfiniteFloat(entidad.getCantidad())) {
            mostrarDialogoInfinitud();
            isInfinite = true;
        } else {
            isInfinite = false;
        }

        return isInfinite;
    }

    private void mostrarDialogoInfinitud() {
        if (getContext() != null) {
            InfinityManagerKt.mostrarInfinityDialog(getContext(),
                    String.format(getString(R.string.dialog__typed_infinite_amount_message),
                    getModoDisplay()), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    proceder();
                }
            }, null);
        }
    }

    private String getModoDisplay() {
        if (modo.equals(TIPO_AUMENTAR)) {
            return "aumentar";
        } else if (modo.equals(TIPO_DISMINUIR)) {
            return "descontar";
        } else {
            // En los otros casos no se introduce cantidad
            return "";
        }
    }

    private void proceder() {
        switch (modo) {
            case TIPO_AUMENTAR:
                positiveCallback.deudaAumentada(position, etCantidad.getText().toString());
                break;
            case TIPO_DISMINUIR:
                positiveCallback.deudarDisminuida(position, etCantidad.getText().toString());
                break;
            case TIPO_CANCELAR:
                positiveCallback.deudaCancelada(position);
                break;
            case TIPO_CANCELAR_TODAS:
                positiveCallback.deudasCanceladas();
                break;
            default:
                // NO-OP No more types
        }

        dismiss();
    }

    public void setPositiveCallback(PositiveCallback positiveCallback) {
        this.positiveCallback = positiveCallback;
    }


    public interface PositiveCallback{
        void deudaAumentada(int position, String cantidadAumentada);
        void deudarDisminuida(int position, String cantidadDisminuida);
        void deudaCancelada(int position);
        void deudasCanceladas();
    }
}
