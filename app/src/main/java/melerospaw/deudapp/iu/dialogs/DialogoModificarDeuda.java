package melerospaw.deudapp.iu.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import melerospaw.deudapp.R;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.utils.StringUtils;

public class DialogoModificarDeuda extends DialogFragment {

    public static final String TAG = DialogoModificarDeuda.class.getSimpleName();
    public static final String TIPO_AUMENTAR = "Aumentar";
    public static final String TIPO_DISMINUIR = "Disminuir";
    public static final String TIPO_CANCELAR = "Cancelar";
    public static final String TIPO_CANCELAR_TODAS = "Cancelar todo";

    public static final String BUNDLE_MODO= "MODO";
    public static final String BUNDLE_POSICION = "POSICION";
    public static final String BUNDLE_TIPO_ENTIDAD = "TIPO_ENTIDAD";

    @Bind(R.id.tv_titulo)       TextView tvTitulo;
    @Bind(R.id.tv_mensaje)      TextView tvMensaje;
    @Bind(R.id.et_cantidad)     EditText etCantidad;
    @Bind(R.id.tv_euro)         TextView tvEuro;
    @Bind(R.id.tv_guardar)      TextView btnAceptar;
    @Bind(R.id.tv_cancelar)     TextView btnCancelar;

    private String modo;
    private int position;
    private int tipoEntidad;
    private PositiveCallback positiveCallback;


    public static DialogoModificarDeuda getInstance(String modo, Integer position,
                                                    @Nullable @Entidad.TipoEntidad int tipo) {
        DialogoModificarDeuda df = new DialogoModificarDeuda();
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
        this.modo = getArguments().getString(BUNDLE_MODO);
        this.position = getArguments().getInt(BUNDLE_POSICION);
        this.tipoEntidad = getArguments().getInt(BUNDLE_TIPO_ENTIDAD);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_modificar_deuda_layout, container, false);
        ButterKnife.bind(this, v);

        loadView();
        return v;
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
        String aumento = etCantidad.getText().toString();
        if (StringUtils.isCadenaVacia(aumento)) {
            dismiss();
        } else if (StringUtils.convertible(aumento).equals("string"))
            Snackbar.make(btnAceptar, R.string.mensaje_cantidad_no_valida, Snackbar.LENGTH_SHORT).show();
        else {
            positiveCallback.deudaAumentada(position, etCantidad.getText().toString());
            dismiss();
        }
    }


    private void disminuir(){
        String descuentoString = etCantidad.getText().toString();
        if (descuentoString.isEmpty()) {
            dismiss();
        } else if (StringUtils.convertible(descuentoString).equals("string")) {
            Snackbar.make(btnAceptar, R.string.mensaje_cantidad_no_valida, Snackbar.LENGTH_SHORT).show();
        } else {
            positiveCallback.deudarDisminuida(position, descuentoString);
            dismiss();
        }
    }


    private void cancelar() {
        positiveCallback.deudaCancelada(position);
        dismiss();
    }


    private void cancelarTodas(){
        positiveCallback.deudasCanceladas();
        dismiss();
    }


    public void setPositiveCallback(PositiveCallback positiveCallback){
        this.positiveCallback = positiveCallback;
    }


    public interface PositiveCallback{
        void deudaAumentada(int position, String cantidadAumentada);
        void deudarDisminuida(int position, String cantidadDisminuida);
        void deudaCancelada(int position);
        void deudasCanceladas();
    }
}
