package melerospaw.deudapp.iu.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import melerospaw.deudapp.R;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.iu.adapters.AdaptadorEntidadesNuevas;
import melerospaw.deudapp.iu.adapters.AdaptadorPersonasNuevas;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.utils.StringUtils;
import melerospaw.deudapp.utils.TecladoUtils;

public class ActivityNuevaEntidad extends AppCompatActivity {

    public static final String BUNDLE_PERSONA = "PERSONA";
    public static final int REQUEST_CODE_ADD_ENTITIES = 1;
    public static final String RESULT_ENTITIES_ADDED = "ENTITIES_ADDED";
    public static final int PERMISO_CONTACTOS = 0;

    @Bind(R.id.toolbar)                 Toolbar toolbar;
    @Bind(R.id.ll_seccion_personas)     LinearLayout llSeccionPersonas;
    @Bind(R.id.tv_personas_vacias)      TextView tvPersonasVacias;
    @Bind(R.id.rv_personas)             RecyclerView rvPersonas;
    @Bind(R.id.tv_entidades_vacias)     TextView tvEntidadesVacias;
    @Bind(R.id.rv_conceptosCantidades)  RecyclerView rvConceptosCantidades;
    @Bind(R.id.btn_guardar)             Button btnGuardar;

    private GestorDatos gestor;
    private AdaptadorPersonasNuevas adaptadorNuevaPersona;
    private AdaptadorEntidadesNuevas adaptadorEntidades;
    private RecyclerView.LayoutManager layoutManagerPersonas;
    private RecyclerView.LayoutManager layoutManagerEntidades;
    private Persona persona;
    private boolean isForResult;
    private @Persona.TipoPersona int tipoPersona;

    public static void start(Context context) {
        Intent starter = new Intent(context, ActivityNuevaEntidad.class);
        context.startActivity(starter);
    }

    public static void startForResult(AppCompatActivity activity, Persona persona) {
        Intent intent = new Intent(activity, ActivityNuevaEntidad.class);
        intent.putExtra(ActivityNuevaEntidad.BUNDLE_PERSONA, persona);
        activity.startActivityForResult(intent, ActivityNuevaEntidad.REQUEST_CODE_ADD_ENTITIES);
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
        gestor.recargarPersona(persona);
        isForResult = persona != null;
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
        List<Persona> personasSimples = gestor.getPersonaSimple();
        if (tieneAccesoAContactos) {
            personasSimples.addAll(gestor.getPersonasFromContactos(this));
        }
        adaptadorNuevaPersona = new AdaptadorPersonasNuevas(
                this, new LinkedList<Persona>(), personasSimples);
        layoutManagerPersonas = new LinearLayoutManager(this);
        rvPersonas.setLayoutManager(layoutManagerPersonas);
        rvPersonas.setAdapter(adaptadorNuevaPersona);
        rvPersonas.setVisibility(View.VISIBLE);
        toggleMensajeVacioPersonas();
    }

    private void toggleMensajeVacioPersonas() {
        tvPersonasVacias.setVisibility(!isForResult && adaptadorNuevaPersona == null || adaptadorNuevaPersona.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    private void inicializarAdaptadorNuevasEntidades() {
        adaptadorEntidades = new AdaptadorEntidadesNuevas(this, new LinkedList<Entidad>());
        layoutManagerEntidades = new LinearLayoutManager(this);
        rvConceptosCantidades.setLayoutManager(layoutManagerEntidades);
        rvConceptosCantidades.setAdapter(adaptadorEntidades);
        toggleMensajeVacioEntidades();
    }

    private void toggleMensajeVacioEntidades() {
        tvEntidadesVacias.setVisibility(adaptadorEntidades == null || adaptadorEntidades.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
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
        adaptadorEntidades.nuevaEntidad(tipoEntidad);
        layoutManagerEntidades.scrollToPosition(adaptadorEntidades.getItemCount() - 1);
        toggleMensajeVacioEntidades();
    }

    private void iniciarProcesoGuardado() {
        clearFocus();
        if (!sePuedeGuardar()) {
            Snackbar.make(rvConceptosCantidades, R.string.faltan_datos, Snackbar.LENGTH_LONG).show();
        } else {
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
        if (isForResult) {
            return adaptadorEntidades.hayAlgo();
        } else {
            return adaptadorNuevaPersona.hayAlguien() && adaptadorEntidades.hayAlgo();
        }
    }

    private void inferirTipoPersona() {
        if (adaptadorEntidades.hayDeudas() && adaptadorEntidades.hayDerechosCobro()) {
            tipoPersona = Persona.AMBOS;
        } else if (adaptadorEntidades.hayDeudas()) {
            tipoPersona = Persona.ACREEDOR;
        } else {
            tipoPersona = Persona.DEUDOR;
        }
    }

    private void guardar() {

        List<Persona> personas = isForResult ?
                Collections.singletonList(persona) : adaptadorNuevaPersona.getPersonas();
        List<Entidad> entidades = adaptadorEntidades.getEntidades();

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
        intent.putExtra(RESULT_ENTITIES_ADDED, new ArrayList<>(entidades));
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
