package melerospaw.deudapp.iu.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import melerospaw.deudapp.R;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.iu.adapters.AdaptadorEntidadesNuevas;
import melerospaw.deudapp.iu.adapters.AdaptadorPersonasNuevas;
import melerospaw.deudapp.modelo.Contact;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.utils.EntidadesUtil;
import melerospaw.deudapp.utils.StringUtils;
import melerospaw.deudapp.utils.TecladoUtils;

public class ActivityNuevasEntidades extends AppCompatActivity {

    public static final String BUNDLE_PERSONA = "PERSONA";
    public static final int REQUEST_CODE_ADD_ENTITIES = 1;
    public static final String RESULT_ENTITIES_ADDED = "ENTITIES_ADDED";
    public static final int PERMISO_CONTACTOS = 0;

    @BindView(R.id.toolbar)                 Toolbar toolbar;
    @BindView(R.id.ll_seccion_personas)     LinearLayout llSeccionPersonas;
    @BindView(R.id.tv_personas_vacias)      TextView tvPersonasVacias;
    @BindView(R.id.rv_personas)             RecyclerView rvPersonas;
    @BindView(R.id.tv_entidades_vacias)     TextView tvEntidadesVacias;
    @BindView(R.id.rv_conceptosCantidades)  RecyclerView rvNuevasEntidades;
    @BindView(R.id.btn_guardar)             Button btnGuardar;

    private GestorDatos gestor;
    private AdaptadorPersonasNuevas adaptadorNuevaPersona;
    private AdaptadorEntidadesNuevas adaptadorNuevasEntidades;
    private RecyclerView.LayoutManager layoutManagerPersonas;
    private RecyclerView.LayoutManager layoutManagerEntidades;
    private Persona persona;
    private boolean isForResult;
    private @Persona.TipoPersona int tipoPersona;

    public static void start(Context context) {
        Intent starter = new Intent(context, ActivityNuevasEntidades.class);
        context.startActivity(starter);
    }

    public static void startForResult(AppCompatActivity activity, Persona persona) {
        Intent intent = new Intent(activity, ActivityNuevasEntidades.class);
        intent.putExtra(ActivityNuevasEntidades.BUNDLE_PERSONA, persona);
        activity.startActivityForResult(intent, ActivityNuevasEntidades.REQUEST_CODE_ADD_ENTITIES);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevas_entidades_layout);
        ButterKnife.bind(this);

        gestor = GestorDatos.getGestor(this);

        recuperarPersona();
        inicializarToolbar();
        inicializarAdaptadorPersonas();
        inicializarAdaptadorNuevasEntidades();
        inicializarBotones();
        inicializarLayout();
    }

    private void recuperarPersona() {
        persona = (Persona) getIntent().getSerializableExtra(BUNDLE_PERSONA);
        isForResult = persona != null;
        if (isForResult) {
            gestor.recargarPersona(persona);
        }
    }

    private void inicializarToolbar() {
        setSupportActionBar(toolbar);

        if (!isForResult) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void inicializarAdaptadorPersonas() {
        if (!isForResult) {
            int estadoPermiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (estadoPermiso == PackageManager.PERMISSION_GRANTED) {
                cargarAdaptador(true);
            } else {
                solicitarPermiso();
            }
        }
    }

    private void solicitarPermiso() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISO_CONTACTOS);
    }

    private void cargarAdaptador(boolean tieneAccesoAContactos) {
        List<Contact> personasSimples = gestor.getPersonaSimple();
        if (tieneAccesoAContactos) {
            personasSimples.addAll(gestor.getContacts(this));
        }
        adaptadorNuevaPersona = new AdaptadorPersonasNuevas(
                this, new LinkedList<Contact>(), personasSimples);
        layoutManagerPersonas = new LinearLayoutManager(this);
        rvPersonas.setLayoutManager(layoutManagerPersonas);
        rvPersonas.setAdapter(adaptadorNuevaPersona);
        rvPersonas.setVisibility(View.VISIBLE);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                adaptadorNuevaPersona.eliminarItem(viewHolder);
                toggleMensajeVacioPersonas();
                TecladoUtils.ocultarTeclado(ActivityNuevasEntidades.this);
            }
        });
        itemTouchHelper.attachToRecyclerView(rvPersonas);
        toggleMensajeVacioPersonas();
    }

    private void toggleMensajeVacioPersonas() {
        tvPersonasVacias.setVisibility(!isForResult && adaptadorNuevaPersona == null || adaptadorNuevaPersona.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    private void inicializarAdaptadorNuevasEntidades() {
        adaptadorNuevasEntidades = new AdaptadorEntidadesNuevas(this, new LinkedList<Entidad>());
        layoutManagerEntidades = new LinearLayoutManager(this);
        rvNuevasEntidades.setLayoutManager(layoutManagerEntidades);
        rvNuevasEntidades.setAdapter(adaptadorNuevasEntidades);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                adaptadorNuevasEntidades.eliminarItem(viewHolder);
                toggleMensajeVacioEntidades();
                TecladoUtils.ocultarTeclado(ActivityNuevasEntidades.this);
            }
        });
        itemTouchHelper.attachToRecyclerView(rvNuevasEntidades);
        toggleMensajeVacioEntidades();
    }

    private void toggleMensajeVacioEntidades() {
        tvEntidadesVacias.setVisibility(adaptadorNuevasEntidades == null || adaptadorNuevasEntidades.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    private void inicializarBotones() {
        if (isForResult) {
            btnGuardar.setText(R.string.anadir);
        }
    }

    private void inicializarLayout() {
        llSeccionPersonas.setVisibility(isForResult ? View.GONE : View.VISIBLE);
    }

    @OnClick({R.id.btn_nueva_persona, R.id.btn_nueva_deuda, R.id.btn_nuevo_derecho,
            R.id.btn_cancelar, R.id.btn_guardar})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_nueva_persona:
                nuevaPersona();
                break;
            case R.id.btn_nueva_deuda:
                nuevaEntidad(Entidad.DEUDA);
                break;
            case R.id.btn_nuevo_derecho:
                nuevaEntidad(Entidad.DERECHO_COBRO);
                break;
            case R.id.btn_cancelar:
                cerrar();
                break;
            case R.id.btn_guardar:
                iniciarProcesoGuardado();
                break;
        }

        TecladoUtils.ocultarTeclado(this);
    }

    private void nuevaPersona() {
        adaptadorNuevaPersona.nuevaPersona();
        layoutManagerPersonas.scrollToPosition(adaptadorNuevaPersona.getItemCount() - 1);
        toggleMensajeVacioPersonas();
    }

    private void cerrar() {
        if (!isForResult) {
            onBackPressed();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void nuevaEntidad(@Entidad.TipoEntidad int tipoEntidad) {
        adaptadorNuevasEntidades.nuevaEntidad(tipoEntidad);
        layoutManagerEntidades.scrollToPosition(adaptadorNuevasEntidades.getItemCount() - 1);
        toggleMensajeVacioEntidades();
    }

    private void iniciarProcesoGuardado() {
        clearFocus();
        if (sePuedeGuardar()) {
            inferirTipoPersona();
            guardar();
        }
    }

    // Makes any focus to dissappear from both RecyclerViews so OnFocusChange listeners
    // are triggered
    private void clearFocus() {
        if (!isForResult) {
            View v = layoutManagerPersonas.getFocusedChild();
            if (v != null) {
                v.clearFocus();
            }
        }

        View v1 = layoutManagerEntidades.getFocusedChild();
        if (v1 != null) {
            v1.clearFocus();
        }
    }

    public boolean sePuedeGuardar() {

        boolean sePuedeGuardar;

        if (isForResult) {
            if (!adaptadorNuevasEntidades.hayAlgo()){
                Snackbar.make(rvNuevasEntidades, "No has añadido ninguna deuda", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevasEntidades.hayEntidadesIncompletas()) {
                Snackbar.make(rvNuevasEntidades, "Faltan datos por indicar", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevasEntidades.hayEntidadesRepetidas()) {
                Snackbar.make(rvNuevasEntidades, "Hay deudas repetidas", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (hayEntidadesRepetidas()) {
                sePuedeGuardar = false;
            } else {
                sePuedeGuardar = true;
            }
        } else {
            if (!adaptadorNuevaPersona.hayAlguien()) {
                Snackbar.make(rvNuevasEntidades, "No has añadido ninguna persona", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevaPersona.hayNombresRepetidos()) {
                Snackbar.make(rvNuevasEntidades, "Has añadido más de una vez la misma persona", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (!adaptadorNuevasEntidades.hayAlgo()) {
                Snackbar.make(rvNuevasEntidades, "No has añadido ninguna deuda", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevasEntidades.hayEntidadesIncompletas()) {
                Snackbar.make(rvNuevasEntidades, "Faltan datos por indicar", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevasEntidades.hayEntidadesRepetidas()) {
                Snackbar.make(rvNuevasEntidades, "Hay deudas con el mismo concepto", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else {
                sePuedeGuardar = true;
            }
        }

        return sePuedeGuardar;
    }

    private void inferirTipoPersona() {
        if (adaptadorNuevasEntidades.hayDeudas() && adaptadorNuevasEntidades.hayDerechosCobro()) {
            tipoPersona = Persona.AMBOS;
        } else if (adaptadorNuevasEntidades.hayDeudas()) {
            tipoPersona = Persona.ACREEDOR;
        } else {
            tipoPersona = Persona.DEUDOR;
        }
    }

    private boolean hayEntidadesRepetidas() {
        List<Entidad> entidades = new LinkedList<>(persona.getEntidades());
        entidades.addAll(adaptadorNuevasEntidades.getEntidades());
        boolean hayRepetidas = EntidadesUtil.hayEntidadesRepetidas(entidades);
        if (hayRepetidas) {
            informarRepetidos(EntidadesUtil.getRepetidos(entidades));
        }
        return hayRepetidas;
    }

    private void informarRepetidos(List<String> conceptosRepetidos) {
        @StringRes int titulo;
        StringBuilder builder = new StringBuilder();
        if (conceptosRepetidos.size() == 1) {
            titulo = R.string.deuda_repetida;
            builder.append(String.format(getString(R.string.ya_tiene_una_deuda),
                    persona.getNombre(), conceptosRepetidos.get(0)));
        } else {
            titulo = R.string.deudas_repetidas;
            builder.append(String.format(getString(R.string.ya_tiene_varias_deudas), persona.getNombre()));
            for (String concepto : conceptosRepetidos) {
                builder.append("\t- ").append(concepto);
                if (conceptosRepetidos.indexOf(concepto) != conceptosRepetidos.size() - 1) {
                    builder.append("\n");
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(builder.toString())
                .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        dialog1.dismiss();
                    }
                }).create().show();
    }

    private void guardar() {

        List<Persona> personas = isForResult ?
                Collections.singletonList(persona) : adaptadorNuevaPersona.getPersonas();
        List<Entidad> entidades = adaptadorNuevasEntidades.getEntidades();

        boolean guardados = gestor.crearEntidadesPersonas(personas, entidades, tipoPersona);

        if (isForResult) {
            sendCorrectResult(entidades);
        } else {
            navigateBack(guardados);
        }
    }

    private void navigateBack(boolean guardados) {
        if (guardados) {
            NavUtils.navigateUpFromSameTask(this);
        } else {
            StringUtils.toastCorto(this, "No se han podido guardar todas las personas nuevas y sus deudas.");
        }
    }

    private void sendCorrectResult(List<Entidad> entidades) {
        Intent intent = new Intent();
        intent.putIntegerArrayListExtra(RESULT_ENTITIES_ADDED, EntidadesUtil.getIds(entidades));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_CONTACTOS) {
            cargarAdaptador(permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }
}
