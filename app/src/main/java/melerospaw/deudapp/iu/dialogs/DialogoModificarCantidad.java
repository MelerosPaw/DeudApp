package melerospaw.deudapp.iu.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import melerospaw.deudapp.R;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.modelo.Entidad.TipoEntidad;
import melerospaw.deudapp.utils.InfinityManagerKt;
import melerospaw.deudapp.utils.StringUtils;

public class DialogoModificarCantidad extends DialogFragment {

    public static final String TAG = DialogoModificarCantidad.class.getSimpleName();
    public static final String TIPO_AUMENTAR = "Aumentar";
    public static final String TIPO_DISMINUIR = "Disminuir";
    public static final String TIPO_CANCELAR = "Cancelar";
    public static final String TIPO_CANCELAR_TODAS = "Cancelar todo";

    public static final String BUNDLE_MODO= "MODO";
    public static final String BUNDLE_POSICION = "POSICION";
    public static final String BUNDLE_TIPO_ENTIDAD = "TIPO_ENTIDAD";

    @BindView(R.id.tv_titulo)       TextView tvTitulo;
    @BindView(R.id.tv_mensaje)      TextView tvMensaje;
    @BindView(R.id.et_cantidad)     EditText etCantidad;
    @BindView(R.id.tv_euro)         TextView tvEuro;
    @BindView(R.id.tv_guardar)      TextView btnAceptar;
    @BindView(R.id.tv_cancelar)     TextView btnCancelar;

    private String modo;
    private int position;
    private int tipoEntidad;
    private Unbinder unbinder;
    private PositiveCallback positiveCallback;


    public static DialogoModificarCantidad getInstance(String modo, Integer position,
                                                       @TipoEntidad int tipo) {
        DialogoModificarCantidad df = new DialogoModificarCantidad();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_MODO, modo);
        bundle.putInt(BUNDLE_POSICION, position);
        bundle.putInt(BUNDLE_TIPO_ENTIDAD, tipo);
        df.setArguments(bundle);
        return df;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.modo = getArguments().getString(BUNDLE_MODO);
            this.position = getArguments().getInt(BUNDLE_POSICION);
            this.tipoEntidad = getArguments().getInt(BUNDLE_TIPO_ENTIDAD);
        }
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (inflater != null) {
            View v = inflater.inflate(R.layout.dialog_modificar_deuda_layout, container, false);
            unbinder = ButterKnife.bind(this, v);

            loadView();
            return v;
        } else {
            return null;
        }
    }

    public void loadView(){
        tvTitulo.setText(modo);
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
        tvMensaje.setText(tipoEntidad == Entidad.DEUDA ?
                R.string.pregunta_aumentar_deuda : R.string.pregunta_aumentar_derecho_cobro);
        btnAceptar.setText(R.string.aumentar);
        btnCancelar.setText(R.string.cancelar);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aumentar();
            }
        });
    }

    private void loadViewDisminuir() {
        tvMensaje.setText(tipoEntidad == Entidad.DEUDA ?
                R.string.pregunta_descontar_deuda : R.string.pregunta_descontar_derecho_cobro);
        btnAceptar.setText(R.string.disminuir);
        btnCancelar.setText(R.string.cancelar);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disminuir();
            }
        });
    }

    private void loadViewCancelar() {
        tvMensaje.setText(tipoEntidad == Entidad.DEUDA ?
                R.string.pregunta_cancelar_deuda : R.string.pregunta_cancelar_derecho_cobro);
        btnAceptar.setText(R.string.si);
        btnCancelar.setText(R.string.no);
        tvEuro.setVisibility(View.GONE);
        etCantidad.setVisibility(View.GONE);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelar();
            }
        });
    }

    private void loadViewCancelarTodas() {
        tvTitulo.setText(modo);
        tvMensaje.setText(R.string.pregunta_cancelar_todas);
        btnAceptar.setText(R.string.si);
        btnCancelar.setText(R.string.no);
        tvEuro.setVisibility(View.GONE);
        etCantidad.setVisibility(View.GONE);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelarTodas();
            }
        });
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
        } else if (StringUtils.convertible(cantidad).equals("string")) {
            Snackbar.make(btnAceptar, R.string.mensaje_cantidad_no_valida, Snackbar.LENGTH_SHORT).show();
            esCantidadCorrecta = false;
        } else {
            esCantidadCorrecta = !esCantidadInfinita();
        }

        return esCantidadCorrecta;
    }

    private boolean esCantidadInfinita() {

        boolean isInfinite;
        Float preparedDecimal = Float.parseFloat(StringUtils.prepararDecimal(etCantidad.getText().toString()));
        if (InfinityManagerKt.isInfiniteFloat(preparedDecimal)) {
            mostrarDialogoInfinitud();
            isInfinite = true;
        } else {
            isInfinite = false;
        }

        return isInfinite;
    }

    private void mostrarDialogoInfinitud() {
        if (getContext() != null) {
            InfinityManagerKt.mostrarInfinityDialog(getContext(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    proceder();
                }
            }, null);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
