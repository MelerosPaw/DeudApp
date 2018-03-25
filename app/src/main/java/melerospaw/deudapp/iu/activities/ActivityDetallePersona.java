package melerospaw.deudapp.iu.activities;

import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.otto.Bus;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import melerospaw.deudapp.R;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.iu.adapters.AdaptadorDeudas;
import melerospaw.deudapp.iu.dialogs.DialogEditarDeuda;
import melerospaw.deudapp.iu.dialogs.DialogoModificarCantidad;
import melerospaw.deudapp.iu.widgets.ContextRecyclerView;
import melerospaw.deudapp.iu.widgets.CustomLinearLayoutManager;
import melerospaw.deudapp.iu.widgets.CustomTransitionSet;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.task.BusProvider;
import melerospaw.deudapp.task.EventoDeudaModificada;
import melerospaw.deudapp.utils.ColorManager;
import melerospaw.deudapp.utils.DecimalFormatUtils;
import melerospaw.deudapp.utils.ExtensionFunctionsKt;
import melerospaw.deudapp.utils.InfinityManagerKt;
import melerospaw.deudapp.utils.StringUtils;

public class ActivityDetallePersona extends AppCompatActivity {

    public static final String BUNDLE_PERSONA = "PERSONA";
    private static final int RC_FOTO = 0;

    @BindView(R.id.toolbar)                     Toolbar toolbar;
    @BindView(R.id.tv_toolbar_titulo)           TextView tvToolbarTitulo;
    @BindView(R.id.tv_titulo)                   TextView tvTitulo;
    @BindView(R.id.tv_toolbar_subtitulo)        TextView tvToolbarSubtitulo;
    @BindView(R.id.tv_subtitulo)                TextView tvSubtitulo;
    @BindView(R.id.rv_deudas)                   ContextRecyclerView rvDeudas;
    @BindView(R.id.tv_concepto)                 TextView tvConcepto;
    @BindView(R.id.tv_cantidad)                 TextView tvCantidad;
    @BindView(R.id.app_bar)                     AppBarLayout appBar;
    @BindView(R.id.collapsing_toolbar_layout)   CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.fl_barra_total)              LinearLayout llBarraTotal;
    @BindView(R.id.iv_foto)                     ImageView ivFoto;

    private GestorDatos gestor;
    private Bus bus = BusProvider.getBus();
    private CustomLinearLayoutManager layoutManager = new CustomLinearLayoutManager(this);
    private AdaptadorDeudas adaptador;
    private Persona persona;
    private Menu menu;
    private boolean isAnimationGoingOn;
    private boolean isDeshacerShowing;
    private Snackbar snackbar;
    private BaseTransientBottomBar.BaseCallback<Snackbar> snackbarCallback =
            new BaseTransientBottomBar.BaseCallback<Snackbar>() {
        @Override
        public void onDismissed(Snackbar transientBottomBar, int event) {
            super.onDismissed(transientBottomBar, event);
            eliminarProvisionalDefinitivo();
            isDeshacerShowing = false;
        }
    };

    public static void start(Context context, String nombre) {
        Intent starter = new Intent(context, ActivityDetallePersona.class);
        Bundle bundle = new Bundle();
        bundle.putString(ActivityDetallePersona.BUNDLE_PERSONA, nombre);
        starter.putExtras(bundle);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_deuda_layout);

        String nombre = getIntent().getExtras().getString(BUNDLE_PERSONA);
        gestor = GestorDatos.getGestor(this);
        persona = gestor.getPersona(nombre);
        ButterKnife.bind(this);
        loadView();
    }

    public void loadView(){
        setToolbar();
        inicializarAdapter();
        cambiarColorTotal(null);
        mostrarFoto();
        mostrarTotal(null);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        tvTitulo.setText(persona.getNombre());
        tvToolbarTitulo.setText(persona.getNombre());
        setTextIfImagePresent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setIcon(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_add));
        collapsingToolbarLayout.setTitle(" ");
    }

    private void setTextIfImagePresent() {
        if (persona.tieneImagen()) {
            String subtitulo = persona.getEntidades().isEmpty() ?
                    persona.getOldest() : getString(R.string.primera_deuda_contraida) + persona.getOldest();
            tvSubtitulo.setText(subtitulo);
            tvToolbarSubtitulo.setText(subtitulo);
            tvToolbarSubtitulo.setVisibility(View.VISIBLE);
            tvToolbarTitulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
//            Palette palette = Palette.from(MemoryUtil.loadBitmap(persona.getImagen()).getResult()).generate();
//            @ColorInt int color = palette.getVibrantColor(ContextCompat.getColor(this, android.R.color.white));
//            tvTitulo.setTextColor(color);
//            tvSubtitulo.setTextColor(color);

        } else {
            tvToolbarSubtitulo.setVisibility(View.GONE);
            tvToolbarTitulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        }
        if (menu != null) {
            menu.findItem(R.id.borrar_imagen).setVisible(persona.tieneImagen()).setEnabled(persona.tieneImagen());
        }
    }

    private void inicializarAdapter() {
        List<Entidad> entidades = persona.getEntidades();
        Collections.sort(entidades, Entidad.COMPARATOR);
        adaptador = new AdaptadorDeudas(this, entidades);
        adaptador.setCallback(new AdaptadorDeudas.AdaptadorEntidadesCallback() {
            @Override
            public boolean sizeAboutToChange() {
                return prepararAnimacion();
            }

            @Override
            public void onAumentarDeudaSeleccionado(Entidad entidad, int adapterPosition) {
                mostrarDialog(DialogoModificarCantidad.TIPO_AUMENTAR, adapterPosition, entidad.getTipoEntidad());
            }

            @Override
            public void onDescontarDeudaSeleccionado(Entidad entidad, int adapterPosition) {
                mostrarDialog(DialogoModificarCantidad.TIPO_DISMINUIR, adapterPosition, entidad.getTipoEntidad());
            }

            @Override
            public void onCancelarDeudaSeleccionado(Entidad entidad, int adapterPosition) {
                mostrarDialog(DialogoModificarCantidad.TIPO_CANCELAR, adapterPosition, entidad.getTipoEntidad());
            }

            @Override
            public void onLongClick(Entidad entidad, int posicion) {
                mostrarDialogoEdicionDeuda(entidad, posicion);
            }
        });
        rvDeudas.setLayoutManager(layoutManager);
        rvDeudas.setAdapter(adaptador);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT
                        | ItemTouchHelper.LEFT | ItemTouchHelper.START | ItemTouchHelper.END) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return true;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        if (isDeshacerShowing) {
                            eliminarProvisionalDefinitivo();
                            if (snackbar != null) {
                                snackbar.removeCallback(snackbarCallback);
                            }
                        }
                        adaptador.eliminarItem(viewHolder);
                        mostrarDeshacer();
                        mostrarTotal(adaptador.getItemProvisional());
                        cambiarColorTotal(adaptador.getItemProvisional());
                    }
                });
        itemTouchHelper.attachToRecyclerView(rvDeudas);
    }

    private void mostrarDeshacer() {
        snackbar = Snackbar.make(rvDeudas, R.string.undo_deleting_debt,
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adaptador.deshacerEliminar();
                mostrarTotal(null);
                cambiarColorTotal(null);
                isDeshacerShowing = false;
            }
        });

        snackbar.addCallback(snackbarCallback);
        snackbar.show();
        isDeshacerShowing = true;
    }

    private void eliminarProvisionalDefinitivo() {
        if (adaptador.getItemProvisional() != null) {
            gestor.eliminarEntidades(Collections.singletonList(adaptador.getItemProvisional()));
            actualizarTotal();
            toggleDeleteAllMenuOption();
            adaptador.eliminarProvisionales();
            bus.post(new EventoDeudaModificada(persona));
        }
    }

    private boolean prepararAnimacion() {
        if (isAnimationGoingOn) {
            return false;
        }
        isAnimationGoingOn = true;
        CustomTransitionSet transitionSet = new CustomTransitionSet();
        transitionSet.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                layoutManager.setScrollEnabled(false);
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                enableAnimation();
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {
                enableAnimation();
            }

            @Override
            public void onTransitionPause(@NonNull Transition transition) {
                layoutManager.setScrollEnabled(true);
            }

            @Override
            public void onTransitionResume(@NonNull Transition transition) {
                layoutManager.setScrollEnabled(false);
            }
        });
        TransitionManager.beginDelayedTransition(rvDeudas, transitionSet);
        return true;
    }

    private void enableAnimation() {
        layoutManager.setScrollEnabled(true);
        isAnimationGoingOn = false;
    }

    private void cambiarColorTotal(@Nullable Entidad deudaOmitida) {
        ColorManager.pintarColorDeuda(llBarraTotal, persona.getCantidadTotal(deudaOmitida));
    }

    private void mostrarTotal(@Nullable Entidad entidadOmitida) {
        boolean mostrarConcepto;
        CharSequence concepto;
        CharSequence cantidad;
        float cantidadTotal = persona.getCantidadTotal(entidadOmitida);

        if (cantidadTotal == 0f) {
            mostrarConcepto = false;
            concepto = "";
            cantidad = getString(R.string.deudas_canceladas);
        } else {
            mostrarConcepto = true;
            switch (persona.getTipo()) {
                case Persona.ACREEDOR:
                    concepto = getString(R.string.debes_un_total_de);
                    break;
                case Persona.DEUDOR:
                    concepto = getString(R.string.te_deben_un_total_de);
                    break;
                case Persona.AMBOS:
                    if (cantidadTotal < 0F) {
                        concepto = getString(R.string.cuenta_a_deber);
                    } else if (cantidadTotal > 0F) {
                        concepto = getString(R.string.cuenta_a_cobrar);
                    } else {
                        concepto = getString(R.string.puedes_cancelar_deuda);
                    }
                    break;
                case Persona.INACTIVO:
                default:
                    concepto = "";
                    mostrarConcepto = false;
            }

            cantidad = String.format(getString(R.string.cantidad),
                    DecimalFormatUtils.decimalToStringIfZero(cantidadTotal, 2, ".", ","));
        }

        tvConcepto.setText(concepto);
        tvCantidad.setText(cantidad);
        tvConcepto.setVisibility(mostrarConcepto ? View.VISIBLE : View.GONE);
    }

    private void toggleScroll() {
        ViewCompat.setNestedScrollingEnabled(rvDeudas, persona.tieneImagen());
    }

    private void toggleDeleteAllMenuOption() {
        if (menu != null) {
            menu.findItem(R.id.cancelar_todas).setVisible(!persona.estanLasDeudasCanceladas()).setEnabled(!persona.estanLasDeudasCanceladas());
            menu.findItem(R.id.borrar_imagen).setVisible(persona.tieneImagen()).setEnabled(persona.tieneImagen());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_detalles, menu);
        toggleDeleteAllMenuOption();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateBack();
                break;
            case R.id.menu_nueva:
                ActivityNuevasDeudas.startForResult(this, persona);
                break;
            case R.id.imagen:
                buscarImagen();
                break;
            case R.id.cancelar_todas:
                modificarEntidad(DialogoModificarCantidad.TIPO_CANCELAR_TODAS, -1);
                break;
            case R.id.borrar_imagen:
                borrarImagen();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void borrarImagen() {
        if (gestor.borrarImagen(persona)) {
            gestor.recargarPersona(persona);
            mostrarFoto();
        } else {
            Toast.makeText(this, R.string.imposible_borrar_foto, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateBack() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        if (intent != null && NavUtils.shouldUpRecreateTask(this, intent) &&
                Build.VERSION.SDK_INT > 15) {
            TaskStackBuilder.create(this).addNextIntent(intent).startActivities();
        } else {
            onBackPressed();
//            NavUtils.navigateUpFromSameTask(this);
//            NavUtils.navigateUpTo(this, intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        ContextRecyclerView.RecyclerContextMenuInfo info =
                (ContextRecyclerView.RecyclerContextMenuInfo) menuInfo;
        Entidad entidadSeleccionada = adaptador.getEntidadByPosition(info.position);
        getMenuInflater().inflate(R.menu.menu_contextual_deuda, menu);
        toggleContextMenuOptions(menu, entidadSeleccionada);
    }

    private void toggleContextMenuOptions(Menu menu, Entidad entidadSeleccionada) {
        if (entidadSeleccionada.estaCancelada()) {
            menu.findItem(R.id.menu_opcion_descontar).setVisible(false).setEnabled(false);
            menu.findItem(R.id.menu_opcion_cancelar).setVisible(false).setEnabled(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextRecyclerView.RecyclerContextMenuInfo info =
                (ContextRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_opcion_aumentar:
                modificarEntidad(DialogoModificarCantidad.TIPO_AUMENTAR, info.position);
                break;
            case R.id.menu_opcion_descontar:
                modificarEntidad(DialogoModificarCantidad.TIPO_DISMINUIR, info.position);
                break;
            case R.id.menu_opcion_cancelar:
                modificarEntidad(DialogoModificarCantidad.TIPO_CANCELAR, info.position);
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;

    }

    private void buscarImagen() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.selecciona_una_imagen)),RC_FOTO);
    }

    public void actualizarTotal() {
        boolean actualizada = gestor.actualizarPersona(persona, persona.getTipo());
        if (!actualizada) {
            ExtensionFunctionsKt.shortToast(this, getString(R.string.cannot_update_person));
        }
        mostrarTotal(null);
        cambiarColorTotal(null);
    }

    private void modificarEntidad(String tipo, int position) {

        if (adaptador.isPositionInAdapter(position)) {
            mostrarDialog(tipo, position, adaptador.getEntidadByPosition(position).getTipoEntidad());
        } else if (position == -1) {
            mostrarDialog(tipo, position, Entidad.DEUDA);
        }
    }

    public void mostrarDialog(String tipo, int position, @Entidad.TipoEntidad int tipoEntidad) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction().addToBackStack(DialogoModificarCantidad.TAG);
        DialogoModificarCantidad dialog = DialogoModificarCantidad.getInstance(tipo, position, tipoEntidad);
        dialog.setPositiveCallback(new DialogoModificarCantidad.PositiveCallback() {
            @Override
            public void deudaAumentada(int position, String cantidadAumentada) {
                aumentarDeuda(position, cantidadAumentada);
            }

            @Override
            public void deudarDisminuida(int position, String cantidadDisminuida) {
                disminuirDeuda(position, cantidadDisminuida);
            }

            @Override
            public void deudaCancelada(int position) {
                cancelarDeuda(position);
            }

            @Override
            public void deudasCanceladas() {
                cancelarDeudas();
            }
        });
        dialog.show(ft, DialogoModificarCantidad.TAG);
    }

    public void mostrarDialogoEdicionDeuda(Entidad entidad, int posicion) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction().addToBackStack(DialogEditarDeuda.getTAG());
        DialogEditarDeuda dialog = DialogEditarDeuda.newInstance(entidad.getId(), posicion);
        dialog.setPositiveCallback(new DialogEditarDeuda.PositiveCallback() {
            @Override
            public void guardar(int posicion, @NotNull Entidad entidad) {
                if (gestor.actualizarEntidad(entidad)) {
                    adaptador.alterItemInPosition(posicion, entidad);
                    adaptador.ordenar(posicion, entidad);
                    mostrarTotal(null);
                    cambiarColorTotal(null);
                    BusProvider.getBus().post(new EventoDeudaModificada(persona));
                    ExtensionFunctionsKt.shortToast(ActivityDetallePersona.this, getString(R.string.deuda_modificada));
                } else {
                    ExtensionFunctionsKt.shortToast(ActivityDetallePersona.this, getString(R.string.problema_guardar_deuda));
                }
            }
        });
        dialog.show(ft, DialogoModificarCantidad.TAG);
    }

    /**
     * Builds a {@code Dialog} to ask for an amount and adds it to the debt indicated by its
     * position on the adaptador data set.
     *
     * @param position Position of the debt in the adaptador's data set.
     */
    private void aumentarDeuda(final int position, String aumento) {

        final Entidad entidad = gestor.getEntidad(adaptador.getEntidadByPosition(position).getId());
        final float cantidadAumento = Float.parseFloat(StringUtils.prepararDecimal(aumento));

        if (!InfinityManagerKt.isInfiniteFloat(cantidadAumento) &&
                InfinityManagerKt.isInfiniteFloat(cantidadAumento + entidad.getCantidad())) {
            InfinityManagerKt.mostrarInfinityDialog(this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    entidad.aumentar(cantidadAumento);
                    actualizarEntidadYAdapter(position, entidad);
                }
            }, null);
        } else {
            entidad.aumentar(cantidadAumento);
            actualizarEntidadYAdapter(position, entidad);
        }
    }

    private void disminuirDeuda(int position, String cantidadDisminucion) {
        Entidad entidad = adaptador.getEntidadByPosition(position);

        float descuento = Float.parseFloat(StringUtils.prepararDecimal(cantidadDisminucion));

        boolean descuentoEsMayorQueDerechoCobro = entidad.getTipoEntidad() == Entidad.DERECHO_COBRO
                && descuento > entidad.getCantidad();
        boolean descuentoEsMayorQueDeuda = entidad.getTipoEntidad() == Entidad.DEUDA
                && descuento > -entidad.getCantidad();

        // No puede descontar más que la cantidad debida
        if (descuentoEsMayorQueDerechoCobro || descuentoEsMayorQueDeuda) {
            Snackbar.make(rvDeudas, R.string.mensaje_disminucion_excesiva, Snackbar.LENGTH_LONG).show();
        } else {
            entidad.disminuir(descuento);
            actualizarEntidadYAdapter(position, entidad);
        }
    }

    private void cancelarDeuda(int position) {
        Entidad entidad = adaptador.getEntidadByPosition(position);
        entidad.setCantidad(0);

        actualizarEntidadYAdapter(position, entidad);
    }

    private void actualizarEntidadYAdapter(int position, Entidad entidad) {
        gestor.actualizarEntidad(entidad);
        adaptador.alterItemInPosition(position, entidad);
        actualizarTotal();
        toggleDeleteAllMenuOption();
        bus.post(new EventoDeudaModificada(persona));
    }

    private void cancelarDeudas() {
        for (int i = 0; i < adaptador.getItemCount(); i++) {
            cancelarDeuda(i);
        }
    }

    public void actualizarLista(List<Entidad> entidades) {
        adaptador.nuevasEntidades(entidades);
        layoutManager.scrollToPosition(0);
        mostrarTotal(null);
        cambiarColorTotal(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_FOTO) {
                procesarResultFoto(data);
            } else if (requestCode == ActivityNuevasDeudas.REQUEST_CODE_ADD_ENTITIES) {
                procesarResultNuevasDeudas(data);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void procesarResultFoto(Intent data) {
        if (data != null && data.getData() != null && data.getData().getPath() != null) {
            if (gestor.guardarFoto(this, persona, data.getData())) {
                gestor.recargarPersona(persona);
                mostrarFoto();
            }
        } else {
            Snackbar.make(ivFoto, R.string.imposible_obtener_foto, Snackbar.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unchecked")
    private void procesarResultNuevasDeudas(Intent data) {
        List<Integer> idsEntidades = data.getIntegerArrayListExtra(ActivityNuevasDeudas.RESULT_ENTITIES_ADDED);
        List<Entidad> entidades = gestor.getEntidades(idsEntidades);
        actualizarLista(entidades);
        BusProvider.getBus().post(new EventoDeudaModificada(persona));
    }

    private void mostrarFoto() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBar.getLayoutParams();
        if (persona.tieneImagen()) {
            Glide.with(this)
                    .load(new File(persona.getImagen()))
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ivFoto);
            float altura = getResources().getDisplayMetrics().heightPixels / 2F;
            params.height = (int) altura;
        } else {
            params.height = (int) getResources().getDisplayMetrics().density * 56;
            ivFoto.setImageBitmap(null);
        }

        tvTitulo.setVisibility(View.VISIBLE);
        tvSubtitulo.setVisibility(View.VISIBLE);
        setExpandEnabled(persona.tieneImagen());
        toggleScroll();
        toggleDeleteAllMenuOption();
        setTextIfImagePresent();
    }

    private void setExpandEnabled(boolean enabled) {
        appBar.setExpanded(enabled);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        if (enabled) {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        } else {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        }
    }
}