package melerospaw.deudapp.iu.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import melerospaw.deudapp.R;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.utils.StringUtils;

public class DialogoCambiarNombre extends DialogFragment {

    public static final String TAG = DialogoCambiarNombre.class.getSimpleName();
    public static final String BUNDLE_PERSONA = "BUNDLE_PERSONA";
    public static final String BUNDLE_ENTIDAD = "BUNDLE_ENTIDAD";
    public static final String BUNDLE_POSICION = "EXTRA_POSICION";

    @BindView(R.id.fl_loading_view) FrameLayout flLoadingView;
    @BindView(R.id.tv_mensaje)      TextView tvMensaje;
    @BindView(R.id.et_nombre)       EditText etNombre;

    private GestorDatos gestor;
    private Persona persona;
    private Entidad entidad;
    private int posicion;
    private Callback callback;


    public static DialogoCambiarNombre getInstance(Persona persona, int posicion) {
        DialogoCambiarNombre df = new DialogoCambiarNombre();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_PERSONA, persona);
        bundle.putInt(BUNDLE_POSICION, posicion);
        df.setArguments(bundle);
        return df;
    }

    public static DialogoCambiarNombre getInstance(Entidad entidad, Persona persona) {
        DialogoCambiarNombre df = new DialogoCambiarNombre();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_ENTIDAD, entidad);
        bundle.putSerializable(BUNDLE_PERSONA, persona);
        df.setArguments(bundle);
        return df;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.persona = (Persona) getArguments().getSerializable(BUNDLE_PERSONA);
        this.entidad = (Entidad) getArguments().getSerializable(BUNDLE_ENTIDAD);
        this.posicion = getArguments().getInt(BUNDLE_POSICION, -1);
        this.gestor = GestorDatos.getGestor(getContext());
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_cambiar_nombre, container, false);
        ButterKnife.bind(this, v);
        loadView();
        return v;
    }

    private void loadView() {
        etNombre.setText(getNombre());
        tvMensaje.setText(getMensaje());
    }

    @OnClick({R.id.tv_cancelar, R.id.tv_guardar})
    public void onClick(View view) {
        if (view.getId() == R.id.tv_guardar) {
            cambiarNombre();
        } else {
            dismiss();
        }
    }

    private String getNombre() {
        return entidad != null ? entidad.getConcepto() : persona.getNombre();
    }

    private String getMensaje() {
        return getString(entidad != null ?
                        R.string.mensaje_dialogo_cambiar_concepto : R.string.mensaje_dialogo_cambiar_nombre,
                getNombre());
    }

    private void cambiarNombre() {
        String nuevoNombre = etNombre.getText().toString();
        if (nuevoNombre.trim().isEmpty()) {
            Toast.makeText(getContext(), getMensajeNombreSinRellenar(), Toast.LENGTH_SHORT).show();
        } else {
            showLoading(true);
            if (esElMismoNombre(nuevoNombre)) {
                Toast.makeText(getContext(), getMensajeNombreSinModificar() , Toast.LENGTH_SHORT).show();
                showLoading(false);
            } else if (nombreEstaRepetido(nuevoNombre)) {
                Toast.makeText(getContext(), getMensajeNombreRepetido(nuevoNombre), Toast.LENGTH_SHORT).show();
                showLoading(false);
            } else {
                callback.onNameChanged(nuevoNombre, posicion);
                dismiss();
            }
        }
    }

    private void showLoading(boolean show) {
        flLoadingView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @StringRes
    private int getMensajeNombreSinRellenar() {
        return esEntidad() ? R.string.escribe_un_concepto : R.string.escribe_un_nombre;
    }

    private String getMensajeNombreSinModificar() {
        return getString(R.string.nombre_sin_modificar,
                (esEntidad() ? getString(R.string.concepto) : getString(R.string.nombre)).toLowerCase());
    }

    private String getMensajeNombreRepetido(String nombreRepetido) {
        return esEntidad() ?
                getString(R.string.concepto_repetido, persona.getNombre(), nombreRepetido) : getString(R.string.persona_repetida);
    }

    private boolean esElMismoNombre(CharSequence nuevoNombre) {
        return esEntidad() ?
                entidad.getConcepto().contentEquals(nuevoNombre) : persona.getNombre().contentEquals(nuevoNombre);
    }

    private boolean nombreEstaRepetido(String nuevoNombre) {
        return esEntidad() ?
                persona.hasConceptoRepetido(nuevoNombre) : gestor.cambiarNombre(persona, nuevoNombre);
    }

    private boolean esEntidad() {
        return entidad != null;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onNameChanged(String nombre, int posicion);
    }
}
