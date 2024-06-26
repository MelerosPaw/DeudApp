package melerospaw.deudapp.iu.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import melerospaw.deudapp.R;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.iu.adapters.AdaptadorNuevasDeudas;
import melerospaw.deudapp.iu.adapters.AdaptadorPersonasNuevas;
import melerospaw.deudapp.iu.dialogs.DialogExplicativo;
import melerospaw.deudapp.iu.vo.EntidadVO;
import melerospaw.deudapp.modelo.Contact;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.modelo.Persona.TipoPersona;
import melerospaw.deudapp.preferences.SharedPreferencesManager;
import melerospaw.deudapp.utils.EntidadesUtilKt;
import melerospaw.deudapp.utils.ExtensionFunctionsKt;
import melerospaw.deudapp.utils.TecladoUtils;

public class ActivityNuevasDeudas extends AppCompatActivity implements View.OnClickListener{

    public static final String BUNDLE_PERSONA = "PERSONA";
    public static final int REQUEST_CODE_ADD_ENTITIES = 1;
    public static final String RESULT_ENTITIES_ADDED = "ENTITIES_ADDED";
    public static final int PERMISO_CONTACTOS = 0;

    private Toolbar toolbar;
    private LinearLayout llSeccionPersonas;
    private Button btnNuevaPersona;
    private TextView tvPersonasVacias;
    private Button btnNuevaDeuda;
    private Button btnNuevoDerecho;
    private RecyclerView rvPersonas;
    private TextView tvEntidadesVacias;
    private Button btnCancelar;
    private RecyclerView rvNuevasEntidades;
    private Button btnGuardar;

    private GestorDatos gestor;
    private AdaptadorPersonasNuevas adaptadorNuevasPersonas;
    private AdaptadorNuevasDeudas adaptadorNuevasDeudas;
    private RecyclerView.LayoutManager layoutManagerPersonas;
    private RecyclerView.LayoutManager layoutManagerEntidades;
    private Persona persona;
    private boolean isForResult;
    private @TipoPersona int tipoPersona;

    public static void start(Context context) {
        Intent starter = new Intent(context, ActivityNuevasDeudas.class);
        context.startActivity(starter);
    }

    public static void startForResult(AppCompatActivity activity, Persona persona) {
        Intent intent = new Intent(activity, ActivityNuevasDeudas.class);
        intent.putExtra(ActivityNuevasDeudas.BUNDLE_PERSONA, persona);
        activity.startActivityForResult(intent, ActivityNuevasDeudas.REQUEST_CODE_ADD_ENTITIES);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevas_entidades_layout);

        gestor = GestorDatos.getGestor(this);

        bindViews();
        setUpClickListeners();
        recuperarPersona();
        inicializarToolbar();
        inicializarAdaptadorPersonas();
        inicializarAdaptadorNuevasEntidades();
        inicializarBotones();
        inicializarLayout();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (requestCode == PERMISO_CONTACTOS) {
                boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (granted) {
                    cargarAdaptador(true);
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                    mostrarDialogoPermisoContactos();
                } else {
                    cargarAdaptador(false);
                }
            }
        }
    }

    @Override
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
                TecladoUtils.ocultarTeclado(this);
                break;
            case R.id.btn_guardar:
                iniciarProcesoGuardado();
                TecladoUtils.ocultarTeclado(this);
                break;
            default:
                // NO-OP No more cases
        }
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        llSeccionPersonas = findViewById(R.id.ll_seccion_personas);
        btnNuevaPersona = findViewById(R.id.btn_nueva_persona);
        tvPersonasVacias = findViewById(R.id.tv_personas_vacias);
        btnNuevaDeuda = findViewById(R.id.btn_nueva_deuda);
        btnNuevoDerecho = findViewById(R.id.btn_nuevo_derecho);
        rvPersonas = findViewById(R.id.rv_personas);
        tvEntidadesVacias = findViewById(R.id.tv_entidades_vacias);
        btnCancelar = findViewById(R.id.btn_cancelar);
        rvNuevasEntidades = findViewById(R.id.rv_conceptosCantidades);
        btnGuardar = findViewById(R.id.btn_guardar);
    }

    private void setUpClickListeners() {
        btnNuevaPersona.setOnClickListener(this);
        btnNuevaDeuda.setOnClickListener(this);
        btnNuevoDerecho.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnGuardar.setOnClickListener(this);
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

        if (!isForResult && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void inicializarAdaptadorPersonas() {
        if (!isForResult) {
            int estadoPermiso = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            if (estadoPermiso == PackageManager.PERMISSION_GRANTED) {
                cargarAdaptador(true);
            } else {
                solicitarPermisoContactos();
            }
        }
    }

    private void solicitarPermisoContactos() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISO_CONTACTOS);
    }

    private void cargarAdaptador(boolean tieneAccesoAContactos) {
        List<Contact> personasSimples = gestor.getPersonaSimple();
        if (tieneAccesoAContactos) {
            personasSimples.addAll(gestor.getContacts(this));
        }
        adaptadorNuevasPersonas = new AdaptadorPersonasNuevas(
                this, new LinkedList<Contact>(), personasSimples);
        layoutManagerPersonas = new LinearLayoutManager(this);
        rvPersonas.setLayoutManager(layoutManagerPersonas);
        rvPersonas.setAdapter(adaptadorNuevasPersonas);
        ExtensionFunctionsKt.visible(rvPersonas, true);
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
                adaptadorNuevasPersonas.eliminarItem(viewHolder);
                toggleMensajeVacioPersonas();
                TecladoUtils.ocultarTeclado(ActivityNuevasDeudas.this);
            }
        });
        itemTouchHelper.attachToRecyclerView(rvPersonas);
        toggleMensajeVacioPersonas();
    }

    private void toggleMensajeVacioPersonas() {
        ExtensionFunctionsKt.visible(tvPersonasVacias, !isForResult &&
                adaptadorNuevasPersonas == null ||
                adaptadorNuevasPersonas.getItemCount() == 0);
    }

    private void inicializarAdaptadorNuevasEntidades() {
        adaptadorNuevasDeudas = new AdaptadorNuevasDeudas(this, new LinkedList<Entidad>(),
                !isForResult);
        layoutManagerEntidades = new LinearLayoutManager(this);
        rvNuevasEntidades.setLayoutManager(layoutManagerEntidades);
        rvNuevasEntidades.setAdapter(adaptadorNuevasDeudas);
        adaptadorNuevasDeudas.setMostrarDialogoExplicativoListener(
                new AdaptadorNuevasDeudas.OnMostrarDialogoExplicativoListener() {
            @Override
            public void onMostrarCuadroIndicativo() {
                mostrarDialogExplicativo();
            }
        });
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
                        adaptadorNuevasDeudas.eliminarItem(viewHolder);
                        toggleMensajeVacioEntidades();
                        TecladoUtils.ocultarTeclado(ActivityNuevasDeudas.this);
                    }
                });
        itemTouchHelper.attachToRecyclerView(rvNuevasEntidades);
        toggleMensajeVacioEntidades();
    }

    private void mostrarDialogExplicativo() {
        final SharedPreferencesManager spm = new SharedPreferencesManager(ActivityNuevasDeudas.this);
        if (spm.isShowExplanatoryDialog()) {
            final DialogExplicativo dialogExplicativo = new DialogExplicativo();
            dialogExplicativo.setCallback(new DialogExplicativo.PositiveCallback() {
                @Override
                public void onDialogClosed(boolean stopShow) {
                    spm.setShowExplanatoryDialog(!stopShow);
                    spm.setFirstTime(false);
                }
            });
            dialogExplicativo.show(getSupportFragmentManager(), DialogExplicativo.getTAG());
        }
    }

    private void toggleMensajeVacioEntidades() {
        ExtensionFunctionsKt.visible(tvEntidadesVacias, adaptadorNuevasDeudas == null ||
                adaptadorNuevasDeudas.getItemCount() == 0);
    }

    private void inicializarBotones() {
        if (isForResult) {
            btnGuardar.setText(R.string.anadir);
        }
    }

    private void inicializarLayout() {
        ExtensionFunctionsKt.hidden(llSeccionPersonas, isForResult);
    }

    private void nuevaPersona() {
        adaptadorNuevasPersonas.nuevaPersona();
        layoutManagerPersonas.scrollToPosition(adaptadorNuevasPersonas.getItemCount() - 1);
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
        adaptadorNuevasDeudas.nuevaEntidad(tipoEntidad);
        layoutManagerEntidades.scrollToPosition(adaptadorNuevasDeudas.getItemCount() - 1);
        toggleMensajeVacioEntidades();
    }

    private void iniciarProcesoGuardado() {
        clearFocus();
        if (sePuedeGuardar()) {
            inferirTipoPersona();
            guardar();
        }
    }

    // Makes any focus disappear from both RecyclerViews so OnFocusChange listeners are triggered
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
            if (adaptadorNuevasDeudas.estaVacio()){
                Snackbar.make(rvNuevasEntidades, "No has añadido ninguna deuda", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevasDeudas.hayEntidadesIncompletas()) {
                Snackbar.make(rvNuevasEntidades, "Faltan datos por indicar", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevasDeudas.hayConceptosRepetidos()) {
                Snackbar.make(rvNuevasEntidades, "Hay deudas repetidas", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else {
                sePuedeGuardar = !hayEntidadesRepetidas();
            }
        } else {
            if (!adaptadorNuevasPersonas.hayAlguien()) {
                Snackbar.make(rvNuevasEntidades, "No has añadido ninguna persona", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevasPersonas.hayNombresRepetidos()) {
                Snackbar.make(rvNuevasEntidades, "Has añadido más de una vez la misma persona", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevasDeudas.estaVacio()) {
                Snackbar.make(rvNuevasEntidades, "No has añadido ninguna deuda", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevasDeudas.hayEntidadesIncompletas()) {
                Snackbar.make(rvNuevasEntidades, "Faltan datos por indicar", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else if (adaptadorNuevasDeudas.hayConceptosRepetidos()) {
                Snackbar.make(rvNuevasEntidades, "Hay deudas con el mismo concepto", Snackbar.LENGTH_LONG).show();
                sePuedeGuardar = false;
            } else {
                sePuedeGuardar = true;
            }
        }

        return sePuedeGuardar;
    }

    private void inferirTipoPersona() {
        if (adaptadorNuevasDeudas.hayDeudas() && adaptadorNuevasDeudas.hayDerechosCobro()) {
            tipoPersona = Persona.AMBOS;
        } else if (adaptadorNuevasDeudas.hayDeudas()) {
            tipoPersona = Persona.ACREEDOR;
        } else {
            tipoPersona = Persona.DEUDOR;
        }
    }

    private boolean hayEntidadesRepetidas() {
        List<Entidad> entidades = new LinkedList<>(persona.getEntidades());
        entidades.addAll(adaptadorNuevasDeudas.getEntidades());
        boolean hayRepetidas = EntidadesUtilKt.hayEntidadesRepetidas(adaptadorNuevasDeudas.getEntidadesVO());
        if (hayRepetidas) {
            informarRepetidos(EntidadesUtilKt.getRepetidos(entidades));
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
                Collections.singletonList(persona) : adaptadorNuevasPersonas.getPersonas();
        if (adaptadorNuevasDeudas.hayEntidadesGrupales()) {
            resolverEntidadesGrupales(adaptadorNuevasDeudas.getEntidadesVO(), adaptadorNuevasPersonas.getItemCount());
        }
        List<Entidad> entidades = adaptadorNuevasDeudas.getEntidades();

        boolean guardados = gestor.crearEntidadesPersonas(personas, entidades, tipoPersona);

        if (isForResult) {
            sendCorrectResult(entidades);
        } else {
            navigateBack(guardados);
        }
    }

    private void resolverEntidadesGrupales(List<EntidadVO> entidades, int cantidadDeudores) {
        EntidadesUtilKt.repartirEntidadesGrupales(entidades, cantidadDeudores);
    }

    private void navigateBack(boolean guardados) {
        if (guardados) {
            NavUtils.navigateUpFromSameTask(this);
        } else {
            ExtensionFunctionsKt.shortToast(this, "No se han podido guardar todas las personas nuevas y sus deudas.");
        }
    }

    private void sendCorrectResult(List<Entidad> entidades) {
        Intent intent = new Intent();
        intent.putIntegerArrayListExtra(RESULT_ENTITIES_ADDED, EntidadesUtilKt.getIds(entidades));
        setResult(RESULT_OK, intent);
        finish();
    }

    private void mostrarDialogoPermisoContactos() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Permiso rechazado")
                .setMessage(getMensajeDialogoContactos())
                .setPositiveButton("Estoy seguro", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cargarAdaptador(false);
                    }
                })
                .setNegativeButton("Cambiar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        solicitarPermisoContactos();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
        ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString getMensajeDialogoContactos(){
        String sourceString = getString(R.string.mensaje_dialog_permisos);
        String spannedSubstring = "Política de privacidad";
        int spanStart = sourceString.indexOf(spannedSubstring);
        int spanEnd = sourceString.indexOf(spannedSubstring) + spannedSubstring.length();
        int spanFlag = Spanned.SPAN_INCLUSIVE_EXCLUSIVE;

        SpannableString spannableString = new SpannableString(sourceString);
        spannableString.setSpan(new URLSpan(getString(R.string.url_politica_privacidad)),
                spanStart, spanEnd, spanFlag);

        return spannableString;
    }
}
