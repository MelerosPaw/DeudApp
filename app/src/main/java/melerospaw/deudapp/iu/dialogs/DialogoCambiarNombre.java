package melerospaw.deudapp.iu.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import melerospaw.deudapp.R;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.modelo.Persona;

public class DialogoCambiarNombre extends DialogFragment {

    public static final String TAG = DialogoCambiarNombre.class.getSimpleName();
    public static final String BUNDLE_PERSONA = "BUNDLE_PERSONA";
    public static final String BUNDLE_POSICION = "BUNDLE_POSICION";

    @Bind(R.id.fl_loading_view) FrameLayout flLoadingView;
    @Bind(R.id.tv_mensaje)      TextView tvMensaje;
    @Bind(R.id.et_nombre)       EditText etNombre;

    private GestorDatos gestor;
    private Persona persona;
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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.persona = (Persona) getArguments().getSerializable(BUNDLE_PERSONA);
        this.posicion = getArguments().getInt(BUNDLE_POSICION, -1);
        this.gestor = GestorDatos.getGestor(getContext());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_cambiar_nombre, container, false);
        ButterKnife.bind(this, v);
        loadView();
        return v;
    }


    public void loadView() {
        tvMensaje.setText(String.format(getString(R.string.mensaje_dialogo_cambiar_nombre), persona.getNombre()));
    }

    @OnClick({R.id.tv_cancelar, R.id.tv_guardar})
    public void onClick(View view) {
        if (view.getId() == R.id.tv_guardar) {
            cambiarNombre();
        } else {
            dismiss();
        }
    }

    private void cambiarNombre() {
        String nuevoNombre = etNombre.getText().toString();
        if (nuevoNombre.isEmpty()) {
            Toast.makeText(getContext(), "Escribe un nombre.", Toast.LENGTH_SHORT).show();
        } else {
            flLoadingView.setVisibility(View.VISIBLE);
            if (gestor.cambiarNombre(persona, nuevoNombre)) {
                Toast.makeText(getContext(), "Nombre cambiado", Toast.LENGTH_SHORT).show();
                gestor.recargarPersona(persona);
                callback.onNameChanged(nuevoNombre, posicion);
                dismiss();
            } else {
                Toast.makeText(getContext(), "Ya existe una persona con este mismo nombre", Toast.LENGTH_SHORT).show();
                flLoadingView.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onNameChanged(String nombre, int posicion);
    }
}
