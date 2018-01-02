package melerospaw.deudapp.iu.fragments;

import android.animation.LayoutTransition;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import melerospaw.deudapp.R;
import melerospaw.deudapp.constants.ConstantesGenerales;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.iu.activities.ActivityDetallePersona;
import melerospaw.deudapp.iu.activities.ActivityNuevasEntidades;
import melerospaw.deudapp.iu.adapters.AdaptadorPersonas;
import melerospaw.deudapp.iu.dialogs.DialogoCambiarNombre;
import melerospaw.deudapp.iu.dialogs.MenuContextualPersona;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.utils.ColorManager;
import melerospaw.deudapp.utils.DecimalFormatUtils;
import melerospaw.deudapp.utils.StringUtils;

public class FragmentViewPagerPersonas extends Fragment {

    public static final String BUNDLE_TIPO = "tipo";

    @BindView(R.id.rv_personas)     RecyclerView rvPersonas;
    @BindView(R.id.tv_vacio)        TextView tvVacio;
    @BindView(R.id.ll_barra_total)  LinearLayout llBarraTotal;
    @BindView(R.id.fl_total)        FrameLayout flTotal;
    @BindView(R.id.tv_total)        TextView tvTotal;
    @BindView(R.id.ll_subtotal)     LinearLayout llSubtotal;
    @BindView(R.id.tv_subtotal)     TextView tvSubtotal;
    @BindView(R.id.tv_cantidad)     TextView tvCantidad;

    private GestorDatos gestor;
    private AdaptadorPersonas adaptadorPersonas;
    private boolean modoEliminar;
    private String mTipo;
    private Persona personaSeleccionada;
    private Menu menu;
    private Unbinder unbinder;

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
        mTipo = getArguments().getString(BUNDLE_TIPO);
        gestor = GestorDatos.getGestor(getActivity());
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lista_personas, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        loadView();
        return rootView;
    }

    private void loadView() {
        cargarPersonas();
        asignarListenersAdapter();
        inicializarMensajeVacio();
        desactivarModoEliminacion();
        mostrarTotal();
        if (Build.VERSION.SDK_INT >= 16) {
            llSubtotal.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            flTotal.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        if (modoEliminar) {
            setMenuEliminar();
        } else {
            setMenuInicial();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_nueva:
                ActivityNuevasEntidades.start(getContext());
                break;
            case R.id.menu_opcion_eliminar:
                mostrarDialogEliminar(true);
                break;
            case R.id.menu_seleccionar_todo:
                adaptadorPersonas.seleccionarTodo();
                mostrarSubtotal();
                break;
            case R.id.menu_deseleccionar:
                adaptadorPersonas.deseleccionarTodo();
                desactivarModoEliminacion();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void mostrarDialogEliminar(final boolean multiple){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.eliminar)
                .setMessage(multiple ? R.string.confirmar_eliminar_personas : R.string.confirmar_eliminar_persona)
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (multiple) {
                            eliminarMarcados();
                        } else {
                            eliminarPersona(personaSeleccionada);
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create().show();
    }

    private void eliminarMarcados() {
        List<Persona> personasMarcadas = adaptadorPersonas.obtenerMarcados();
        boolean eliminados = gestor.eliminarPersona(personasMarcadas);
        if (eliminados) {
            adaptadorPersonas.eliminarVarios(personasMarcadas);
            adaptadorPersonas.desactivarModoEliminacion();
            desactivarModoEliminacion();
            inicializarMensajeVacio();
        } else {
            Snackbar.make(rvPersonas, R.string.imposible_borrar_deudas, Snackbar.LENGTH_SHORT).show();
        }
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
                mostrarSubtotal();
            }

            @Override
            public void personaDeseleccionada(boolean desactivarModoEliminacion) {
                if (desactivarModoEliminacion) {
                    desactivarModoEliminacion();
                } else {
                    mostrarSubtotal();
                }
            }
        });

        adaptadorPersonas.setOnDeudaModificadaListener(new AdaptadorPersonas.OnDeudaModificadaListener() {
            @Override
            public void onDeudaModificada(float totalActualizado) {
                if (isAdded()) {
                    if (modoEliminar) {
                        mostrarSubtotal();
                    } else {
                        mostrarTotal();
                    }
                    inicializarMensajeVacio();
                }
            }
        });
    }

    private void inicializarMensajeVacio() {
        rvPersonas.setVisibility(adaptadorPersonas.getItemCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        tvVacio.setVisibility(adaptadorPersonas.getItemCount() > 0 ? View.INVISIBLE : View.VISIBLE);
        if (adaptadorPersonas.getItemCount() == 0) {
            SpannableString mensaje = new SpannableString(getString(R.string.mensaje_vacio));
            Drawable d = ContextCompat.getDrawable(getContext(), R.drawable.ic_nueva_persona_contorno);
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

        float total = adaptadorPersonas.obtenerTotal();
        tvCantidad.setText(String.format(getString(R.string.cantidad),
                DecimalFormatUtils.decimalToStringIfZero(total, 2, ".", ",")));

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
        ColorManager.pintarColorDeuda(llBarraTotal, total);
        llBarraTotal.setVisibility(total == 0f ? View.GONE: View.VISIBLE);
        llSubtotal.getLayoutParams().width = 0;
    }

    private void mostrarSubtotal() {

        if (adaptadorPersonas.getItemCount() == 0) {
            desactivarModoEliminacion();
        } else {
            float total = adaptadorPersonas.obtenerTotal();
            float subtotal = adaptadorPersonas.obtenerSubtotal();

            tvTotal.setText(R.string.total_seleccionado);
            tvSubtotal.setText(DecimalFormatUtils.decimalToStringIfZero(subtotal, 2, ".", ","));
            tvCantidad.setText(String.format(getString(R.string.cantidad),
                    DecimalFormatUtils.decimalToStringIfZero(total, 2, ".", ",")));
            ColorManager.pintarColorDeuda(llBarraTotal, total);
            llBarraTotal.setVisibility(total == 0f ? View.GONE: View.VISIBLE);
            llSubtotal.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        }
    }

    private void setMenuEliminar() {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_eliminar, menu);
    }

    private void desactivarModoEliminacion() {
        if (menu != null && modoEliminar) {
            modoEliminar = false;
            setMenuInicial();
            mostrarTotal();
        }
    }

    private void setMenuInicial() {
        if (menu != null && !isMenuInicial()) {
            menu.clear();
            getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
        }
    }

    private boolean isMenuInicial() {
        return menu.findItem(R.id.menu_nueva) != null;
    }

    private void abrirOpciones() {
        MenuContextualPersona menuContextualPersona =
                MenuContextualPersona.newInstance(personaSeleccionada.getNombre(), !modoEliminar);
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
                mostrarDialogEliminar(false);
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
//        adaptadorPersonas.deseleccionarTodo();
//        desactivarModoEliminacion();
        ActivityDetallePersona.start(getActivity(), persona.getNombre());
    }

    private void eliminarPersona(Persona persona) {
        boolean personaEliminada = gestor.eliminarPersona(Collections.singletonList(persona));

        if (personaEliminada) {
            adaptadorPersonas.eliminarPersona(persona);
            if (modoEliminar) {
                mostrarSubtotal();
            } else {
                mostrarTotal();
            }
            inicializarMensajeVacio();
        } else {
            StringUtils.toastCorto(getActivity(),
                    String.format(getString(R.string.imposible_eliminar_deudas),
                            persona.getNombre()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}