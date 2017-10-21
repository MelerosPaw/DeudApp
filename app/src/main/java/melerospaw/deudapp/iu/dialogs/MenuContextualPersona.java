package melerospaw.deudapp.iu.dialogs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import melerospaw.deudapp.R;
import melerospaw.deudapp.utils.ScreenUtils;

public class MenuContextualPersona extends DialogFragment {

    public static final String TAG = MenuContextualPersona.class.getSimpleName();
    private static final String BUNDLE_NOMBRE_PERSONA = "persona";
    private static final String BUNDLE_MOSTRAR_ELIMINAR = "mostrar_eliminar";

    @BindView(R.id.tv_titulo)   TextView tvTitulo;
    @BindView(R.id.tv_eliminar) TextView tvEliminar;

    private MenuContextualPersonaCallback callback;
    private String nombrePersona;
    private boolean mostrarEliminar;
    private Unbinder unbinder;

    public static MenuContextualPersona newInstance(String nombrePersona, boolean mostrarEliminar) {

        Bundle args = new Bundle();
        args.putString(BUNDLE_NOMBRE_PERSONA, nombrePersona);
        args.putBoolean(BUNDLE_MOSTRAR_ELIMINAR, true);

        MenuContextualPersona fragment = new MenuContextualPersona();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveBundleArguments();
    }

    private void retrieveBundleArguments() {
        nombrePersona = getArguments().getString(BUNDLE_NOMBRE_PERSONA);
        mostrarEliminar = getArguments().getBoolean(BUNDLE_MOSTRAR_ELIMINAR);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_contextual_persona_layout, container, false);
        if (view != null) {
            unbinder = ButterKnife.bind(this, view);
            tvEliminar.setEnabled(mostrarEliminar);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTitulo.setText(nombrePersona);
    }

    @Override
    public void onStart() {
        super.onStart();
        ScreenUtils.ajustarAncho(this);
    }

    public void setUp(MenuContextualPersonaCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.tv_ver, R.id.tv_eliminar, R.id.tv_cambiar_nombre})
    public void onViewClicked(View view) {
        switch(view.getId()) {
            case R.id.tv_ver: callback.verDeuda(this); break;
            case R.id.tv_eliminar: callback.eliminarPersona(this); break;
            case R.id.tv_cambiar_nombre: callback.cambiarNombre(this);
        }
    }

    public interface MenuContextualPersonaCallback {
        void verDeuda(MenuContextualPersona dialog);
        void eliminarPersona(MenuContextualPersona dialog);
        void cambiarNombre(MenuContextualPersona dialog);
    }
}
