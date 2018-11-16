package melerospaw.deudapp.iu.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.security.cert.Extension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import melerospaw.deudapp.R;
import melerospaw.deudapp.constants.ConstantesGenerales;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.iu.activities.ActivityDetallePersona;
import melerospaw.deudapp.iu.activities.ActivityNuevasDeudas;
import melerospaw.deudapp.iu.activities.ActivityPreferencias;
import melerospaw.deudapp.iu.adapters.AdaptadorPersonas;
import melerospaw.deudapp.iu.dialogs.DialogoCambiarNombre;
import melerospaw.deudapp.iu.dialogs.MenuContextualPersona;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.utils.ColorManager;
import melerospaw.deudapp.utils.CurrencyUtilKt;
import melerospaw.deudapp.utils.ExtensionFunctionsKt;
import melerospaw.deudapp.utils.SecureOperationKt;

public class FragmentViewPagerPersonas extends Fragment {

    public static final String BUNDLE_TIPO = "TIPO";

    @BindView(R.id.rv_personas)                 RecyclerView rvPersonas;
    @BindView(R.id.ll_vacio)                    ViewGroup llVacio;
    @BindView(R.id.tv_vacio)                    TextView tvVacio;
    @BindView(R.id.ll_barra_total)              FrameLayout flBarraTotal;
    @BindView(R.id.fl_total)                    FrameLayout flTotal;
    @BindView(R.id.tv_total)                    TextView tvTotal;
    @BindView(R.id.ll_subtotal)                 LinearLayout llSubtotal;
    @BindView(R.id.tv_subtotal)                 TextView tvSubtotal;
    @BindView(R.id.ll_total_simple)             LinearLayout llTotalSimple;
    @BindView(R.id.tv_cantidad)                 TextView tvCantidad;
    @BindView(R.id.tv_moneda)                   TextView tvMoneda;
    @BindView(R.id.ll_barra_total_resumen)      LinearLayout llTotalResumen;
    @BindView(R.id.ll_root_total_debido)        LinearLayout llRootTotalDebido;
    @BindView(R.id.tv_total_debido)             TextView tvTotalDebido;
    @BindView(R.id.tv_total_debido_moneda)      TextView tvTotalDebidoMoneda;
    @BindView(R.id.ll_root_total_adeudado)      LinearLayout llRootTotalAdeudado;
    @BindView(R.id.tv_total_adeudado)           TextView tvTotalAdeudado;
    @BindView(R.id.tv_total_adeudado_moneda)    TextView tvTotalAdeudadoMoneda;
    @BindView(R.id.ll_root_total_ambos)         LinearLayout llRootTotalAmbos;
    @BindView(R.id.tv_total_ambos)              TextView tvTotalAmbos;
    @BindView(R.id.tv_total_ambos_moneda)       TextView tvTotalAmbosMoneda;
    @BindView(R.id.ll_root_total_total)         LinearLayout llRootTotalTotal;
    @BindView(R.id.tv_total_total)              TextView tvTotalTotal;
    @BindView(R.id.tv_total_total_moneda)       TextView tvTotalTotalMoneda;

    private GestorDatos gestor;
    private AdaptadorPersonas adaptadorPersonas;
    private boolean modoEliminar;
    private String mTipo;
    private Persona personaSeleccionada;
    private Menu menu;
    private Unbinder unbinder;
    private boolean showResumen;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
        asignarListenerTotal();
        ExtensionFunctionsKt.enableAnimateChanges(Arrays.asList(llSubtotal, flTotal, flBarraTotal));
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
                ActivityNuevasDeudas.start(getContext());
                break;
            case R.id.menu_config:
                ActivityPreferencias.start(getContext());
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
        final boolean hayPersonas = adaptadorPersonas.getItemCount() > 0;
        ExtensionFunctionsKt.visible(rvPersonas, hayPersonas);
        ExtensionFunctionsKt.visible(llVacio, !hayPersonas);
    }

    private void activarModoEliminacion() {
        if (menu != null) {
            modoEliminar = true;
            setMenuEliminar();
        }
    }

    private void mostrarTotal() {

        tvTotal.setText(getTextoTotal());
        llSubtotal.getLayoutParams().width = 0;

        final Context context = requireContext();
        final float total = adaptadorPersonas.obtenerTotal();
        CurrencyUtilKt.setUpAmount(context, total, llTotalSimple, tvCantidad, tvMoneda);
        ColorManager.pintarColorDeuda(flBarraTotal, total);
        ExtensionFunctionsKt.hidden(flBarraTotal, total == 0F);
        setTotales(context);
    }

    private String getTextoTotal() {
        String texto;
        switch (mTipo) {
            case ConstantesGenerales.DEBO:
                texto = getString(R.string.total_debido);
                break;
            case ConstantesGenerales.ME_DEBEN:
                texto = getString(R.string.total_adeudado);
                break;
            default:
                texto = getString(R.string.balance_total);
                break;
        }

        return texto;
    }

    private void setTotales(Context context) {
        final float totalAcreedores = mTipo.equals(ConstantesGenerales.DEBO) ?
                adaptadorPersonas.obtenerTotal() : gestor.getTotalAcreedores();
        final float totalDeudores= mTipo.equals(ConstantesGenerales.ME_DEBEN) ?
                adaptadorPersonas.obtenerTotal() : gestor.getTotalDeudores();
        final float totalAmbos = mTipo.equals(ConstantesGenerales.AMBOS) ?
                adaptadorPersonas.obtenerTotal() : gestor.getTotalAmbos();
        final float totalTotal = SecureOperationKt.secureAdd(
                SecureOperationKt.secureAdd(totalAcreedores, totalDeudores), totalAmbos);

        CurrencyUtilKt.setUpAmount(context, totalAcreedores, llRootTotalDebido,tvTotalDebido, tvTotalDebidoMoneda);
        CurrencyUtilKt.setUpAmount(context, totalDeudores, llRootTotalAdeudado, tvTotalAdeudado, tvTotalAdeudadoMoneda);
        CurrencyUtilKt.setUpAmount(context, totalAmbos, llRootTotalAmbos, tvTotalAmbos, tvTotalAmbosMoneda);
        CurrencyUtilKt.setUpAmount(context, totalTotal, llRootTotalTotal, tvTotalTotal, tvTotalTotalMoneda);
    }

    private void mostrarSubtotal() {

        if (adaptadorPersonas.getItemCount() == 0) {
            desactivarModoEliminacion();
        } else {
            final float total = adaptadorPersonas.obtenerTotal();
            final float subtotal = adaptadorPersonas.obtenerSubtotal();

            tvTotal.setText(R.string.total_seleccionado);
            tvSubtotal.setText(CurrencyUtilKt.formatAmountWithoutCurrencyPosition(getContext(),
                    null, subtotal));
            CurrencyUtilKt.setUpAmount(requireContext(), total, llTotalSimple, tvCantidad, tvMoneda);
            ColorManager.pintarColorDeuda(flBarraTotal, total);
            ExtensionFunctionsKt.hidden(flBarraTotal, total == 0f);
            llSubtotal.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        }
    }

    private void asignarListenerTotal() {
        flBarraTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTotal();
            }
        });
    }

    public void toggleTotal() {
        showResumen = !showResumen;
        ExtensionFunctionsKt.hidden(llTotalSimple, showResumen);
        ExtensionFunctionsKt.hidden(llTotalResumen, !showResumen);
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
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(DialogoCambiarNombre.TAG);
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
        boolean personaEliminada = gestor.eliminarPersona(Collections.singletonList(persona));

        if (personaEliminada) {
            adaptadorPersonas.eliminarPersona(persona);
            if (modoEliminar) {
                mostrarSubtotal();
            } else {
                mostrarTotal();
            }
            inicializarMensajeVacio();
        } else if (getActivity() != null) {
                ExtensionFunctionsKt.shortToast(getActivity(),
                        String.format(getString(R.string.imposible_eliminar_deudas),
                                persona.getNombre()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.new_person)
    public void onClick() {
        ActivityNuevasDeudas.start(getActivity());
    }
}