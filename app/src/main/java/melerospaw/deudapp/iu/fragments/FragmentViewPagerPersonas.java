package melerospaw.deudapp.iu.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import melerospaw.deudapp.R;
import melerospaw.deudapp.constants.ConstantesGenerales;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.iu.activities.ActivityDetallePersona;
import melerospaw.deudapp.iu.activities.ActivityNuevaEntidad;
import melerospaw.deudapp.iu.adapters.AdaptadorPersonas;
import melerospaw.deudapp.iu.dialogs.DialogoCambiarNombre;
import melerospaw.deudapp.iu.dialogs.MenuContextualPersona;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.task.BusProvider;
import melerospaw.deudapp.task.EventoDeudaModificada;
import melerospaw.deudapp.utils.StringUtils;

public class FragmentViewPagerPersonas extends Fragment {

    public static final String BUNDLE_TIPO = "tipo";

    @BindView(R.id.rv_personas) RecyclerView rvPersonas;
    @BindView(R.id.tv_vacio)    TextView tvVacio;
    @BindView(R.id.tv_total)    TextView tvTotal;
    @BindView(R.id.tv_cantidad) TextView tvCantidad;

    private GestorDatos gestor;
    private AdaptadorPersonas adaptadorPersonas;
    private boolean modoEliminar;
    private String mTipo;
    private Persona personaSeleccionada;
    private Menu menu;

    public static FragmentViewPagerPersonas newInstance(String tipo) {
        FragmentViewPagerPersonas f = new FragmentViewPagerPersonas();

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TIPO, tipo);
        f.setArguments(bundle);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mTipo = getArguments().getString(BUNDLE_TIPO);
        gestor = GestorDatos.getGestor(getActivity());
        setHasOptionsMenu(true);
        BusProvider.getBus().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lista_personas, container, false);
        ButterKnife.bind(this, rootView);
        loadView();
        return rootView;
    }

    private void loadView() {
        cargarPersonas();
        asignarListenersAdapter();
        inicializarMensajeVacio();
        desactivarModoEliminacion();
        mostrarTotal();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        if (modoEliminar) {
            setMenuEliminar();
        } else {
            setMenuNormal();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nueva) {
            ActivityNuevaEntidad.start(getContext());
        } else if (id == R.id.menu_opcion_eliminar) {
            List<Persona> personasMarcadas = adaptadorPersonas.obtenerMarcados();
            boolean eliminados = gestor.eliminarPersonas(personasMarcadas);
            if (eliminados) {
                adaptadorPersonas.eliminar(personasMarcadas);
                adaptadorPersonas.desactivarModoEliminacion();
                desactivarModoEliminacion();
                inicializarMensajeVacio();
            } else {
                Snackbar.make(rvPersonas, R.string.imposible_borrar_deudas, Snackbar.LENGTH_SHORT).show();
            }
        }

        return id == R.id.nueva || id == R.id.menu_opcion_eliminar;
    }

    private void cargarPersonas() {

        List<Persona> listaPersonas;
        switch (mTipo) {
            case ConstantesGenerales.DEBO:
                listaPersonas = gestor.getAcreedores();
                break;
            case ConstantesGenerales.ME_DEBEN:
                listaPersonas = gestor.getDeudores();
                break;
            default:
                listaPersonas = gestor.getAmbos();
                break;
        }

        adaptadorPersonas = new AdaptadorPersonas(getActivity(), listaPersonas, mTipo);
        rvPersonas.setAdapter(adaptadorPersonas);
    }

    private void asignarListenersAdapter() {
        adaptadorPersonas.setContextualMenuInterface(new AdaptadorPersonas.ContextualMenuInterface() {
            @Override
            public void mostrarMenuContextual(Persona persona, int posicionEnAdapter) {
                personaSeleccionada = persona;
                abrirOpciones();
            }
        });

        adaptadorPersonas.setOnItemClickListener(new AdaptadorPersonas.OnItemClickListener() {
            @Override
            public void onClick(Persona persona) {
                abrirDetalle(persona);
            }
        });

        adaptadorPersonas.setOnPersonaSeleccionadaListener(new AdaptadorPersonas.OnPersonaSeleccionadaListener() {
            @Override
            public void personaSeleccionada(boolean activarModoEliminacion) {
                if (activarModoEliminacion) {
                    activarModoEliminacion();
                }
            }

            @Override
            public void personaDeseleccionada(boolean desactivarModoEliminacion) {
                if (desactivarModoEliminacion) {
                    desactivarModoEliminacion();
                }
            }
        });
    }

    private void inicializarMensajeVacio() {
        rvPersonas.setVisibility(adaptadorPersonas.getItemCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        tvVacio.setVisibility(adaptadorPersonas.getItemCount() > 0 ? View.INVISIBLE : View.VISIBLE);
        if (adaptadorPersonas.getItemCount() == 0) {
            SpannableString mensaje = new SpannableString(getString(R.string.mensaje_vacio));
            Drawable d = ContextCompat.getDrawable(getContext(), android.R.drawable.ic_menu_add);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
            mensaje.setSpan(span, 54, 55, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            tvVacio.setText(mensaje);
        }
    }

    private void activarModoEliminacion() {
        if (menu != null) {
            modoEliminar = true;
            setMenuEliminar();
        }
    }

    private void mostrarTotal() {
        tvCantidad.setText(adaptadorPersonas.obtenerTotal());

        String texto;
        switch (mTipo) {
            case ConstantesGenerales.DEBO:
                texto = "Total debido";
                break;
            case ConstantesGenerales.ME_DEBEN:
                texto = "Total adeudado";
                break;
            default:
                texto = "Balance total";
                break;
        }

        tvTotal.setText(texto);
    }

    private void setMenuEliminar() {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_eliminar, menu);
    }

    private void desactivarModoEliminacion() {
        if (menu != null) {
            modoEliminar = false;
            setMenuNormal();
        }
    }

    private void setMenuNormal() {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
    }

    private void abrirOpciones() {
        MenuContextualPersona menuContextualPersona =
                MenuContextualPersona.newInstance(personaSeleccionada.getNombre());
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(menuContextualPersona, MenuContextualPersona.TAG)
                .commit();

        menuContextualPersona.setUp(new MenuContextualPersona.MenuContextualPersonaCallback() {
            @Override
            public void verDeuda(MenuContextualPersona dialog) {
                abrirDetalle(personaSeleccionada);
                dialog.dismiss();
            }

            @Override
            public void eliminarPersona(MenuContextualPersona dialog) {
                FragmentViewPagerPersonas.this.eliminarPersona(personaSeleccionada);
                dialog.dismiss();
            }

            @Override
            public void cambiarNombre(MenuContextualPersona dialog) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction().addToBackStack(DialogoCambiarNombre.TAG);
                DialogoCambiarNombre dialog2 = DialogoCambiarNombre.getInstance(personaSeleccionada,
                        adaptadorPersonas.getPosition(personaSeleccionada));
                dialog2.setCallback(new DialogoCambiarNombre.Callback() {
                    @Override
                    public void onNameChanged(String nuevoNombre, int posicion) {
                        Persona persona = (gestor.getPersona(nuevoNombre));
                        gestor.recargarPersona(persona);
                        adaptadorPersonas.recargarPosicion(persona, posicion);
                    }
                });
                dialog2.show(ft, DialogoCambiarNombre.TAG);
                dialog.dismiss();
            }
        });
    }

    private void abrirDetalle(Persona persona) {
        ActivityDetallePersona.start(getActivity(), persona.getNombre());
    }

    private void eliminarPersona(Persona persona) {
        boolean personaEliminada = gestor.eliminarPersonas(Collections.singletonList(persona));

        if (personaEliminada) {
            adaptadorPersonas.eliminarPersona(persona);
            inicializarMensajeVacio();
            mostrarTotal();
        } else {
            StringUtils.toastCorto(getActivity(),
                    String.format(getString(R.string.imposible_eliminar_deudas),
                            persona.getNombre()));
        }
    }

    @Subscribe
    public void onEventoDeudaModificada(EventoDeudaModificada evento) {
        mostrarTotal();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().unregister(this);
    }
}