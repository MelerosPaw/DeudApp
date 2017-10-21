package melerospaw.deudapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import melerospaw.deudapp.BuildConfig;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.memoryutil.Path;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "DeudApp.sqlite";
    private static final int DATABASE_VERSION = 1;
    private final Path.Builder RUTA_CARPETA_IMAGENES;

    private static DatabaseHelper helper;
    private Dao<Persona, String> personaDao;
    private Dao<Entidad, Integer> entidadDao;


    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        RUTA_CARPETA_IMAGENES = new Path.Builder(context)
                .storageDirectory(Path.STORAGE_PRIVATE_INTERNAL)
                .folder("imagenes");
    }


    public static DatabaseHelper getHelper(Context context) {
        if (helper == null) {
            helper = new DatabaseHelper(context);
//            OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return helper;
    }

    public static void closeHelper() {
        if (helper != null) {
            OpenHelperManager.releaseHelper();
            helper = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Persona.class);
            TableUtils.createTable(connectionSource, Entidad.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (BuildConfig.DEBUG) {
            cargarDatosPrueba();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {

    }

    private Dao<Persona, String> getPersonaDao() {

        if (personaDao == null) {
            try {
                personaDao = getDao(Persona.class);
            } catch (SQLException e) {
                throw new RuntimeException("No se ha podido crear el Dao de Persona", e);
            }
        }

        return personaDao;
    }

    private Dao<Entidad, Integer> getEntidadDao() {
        if (entidadDao == null) {
            try {
                entidadDao = getDao(Entidad.class);
            } catch (SQLException e) {
                throw new RuntimeException("No se ha podido crear el RuntimeExcetionDao de Entidad", e);
            }
        }

        return entidadDao;
    }

    @Override
    public void close() {
        super.close();
        personaDao = null;
        entidadDao = null;
    }

    /**
     * Carga los datos de prueba.
     */
    private void cargarDatosPrueba() {

        Persona deudor1 = new Persona("Federico Fernández", Persona.DEUDOR);
        Persona deudor2 = new Persona("Paco Martínez Soria", Persona.DEUDOR);
        Persona deudor3 = new Persona("Puta barata", Persona.DEUDOR);
        Persona acreedor1 = new Persona("Romualdo Aragón", Persona.ACREEDOR);
        Persona acreedor2 = new Persona("Fernando Pérez", Persona.ACREEDOR);
        Persona ambos1 = new Persona("Cristina Amodeo", Persona.AMBOS);
        Persona ambos2 = new Persona("Rocío Pérez", Persona.AMBOS);
        Entidad deuda1 = new Entidad(15f, "Cosa cañera", Entidad.DEUDA);
        Entidad deuda2 = new Entidad(2f, "Putas gratis", Entidad.DEUDA);
        Entidad deuda3 = new Entidad(325.56f, "La causa perdida", Entidad.DEUDA);
        Entidad deuda4 = new Entidad(7025.33f, "Película que no salió bien pero que la rodamos igualmente", Entidad.DEUDA);
        Entidad deuda5 = new Entidad(143f, "Hacienda", Entidad.DEUDA);
        Entidad derechoCobro1 = new Entidad(30.15f, "Conde de Burra", Entidad.DERECHO_COBRO);
        Entidad derechoCobro2 = new Entidad(150.25f, "Pasacalles", Entidad.DERECHO_COBRO);
        Entidad derechoCobro3 = new Entidad(150.25f, "Pasacalles", Entidad.DERECHO_COBRO);
        Entidad derechoCobro4 = new Entidad(150.25f, "Respira mierda", Entidad.DERECHO_COBRO);
        Entidad derechoCobro5 = new Entidad(150.25f, "Cosas caras, como diamantes, caniches repeinados y esas cosas", Entidad.DERECHO_COBRO);

        derechoCobro1.setPersona(deudor1);
        derechoCobro2.setPersona(deudor2);
        derechoCobro3.setPersona(deudor3);
        derechoCobro4.setPersona(ambos1);
        derechoCobro5.setPersona(ambos2);
        deuda1.setPersona(ambos1);
        deuda2.setPersona(ambos1);
        deuda3.setPersona(ambos2);
        deuda4.setPersona(acreedor1);
        deuda5.setPersona(acreedor2);

        nuevaPersona(deudor1);
        nuevaPersona(deudor2);
        nuevaPersona(deudor3);
        nuevaPersona(acreedor1);
        nuevaPersona(acreedor2);
        nuevaPersona(ambos1);
        nuevaPersona(ambos2);

        nuevaEntidad(deuda1);
        nuevaEntidad(deuda2);
        nuevaEntidad(deuda3);
        nuevaEntidad(deuda4);
        nuevaEntidad(deuda5);
        nuevaEntidad(derechoCobro1);
        nuevaEntidad(derechoCobro2);
        nuevaEntidad(derechoCobro3);
        nuevaEntidad(derechoCobro4);
        nuevaEntidad(derechoCobro5);
    }

    Persona getPersona(String nombre) {
        try {
            return getPersonaDao().queryForId(nombre);
        } catch (SQLException e) {
            throw new RuntimeException("No se ha podido obtener la persona mediante su nombre.", e);
        }
    }

    List<Persona> getPersonas() {
        try {
            return getPersonaDao().queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido obtener todas las personas.", e);
        }
    }

    boolean nuevaPersona(Persona persona) {
        try {
            return getPersonaDao().create(persona) == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido crear la persona.", e);
        }
    }

    boolean actualizarPersona(Persona persona) {

        try {
            return getPersonaDao().update(persona) == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se ha podido actualizar la persona.", e);
        }
    }

    boolean eliminarPersonas(final List<Persona> personas) {

        ConnectionSource connectionSource = getConnectionSource();
        boolean borrados;

        try {
            borrados = TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    for (Persona persona : personas) {
                        getEntidadDao().delete(persona.getEntidades());
//                        persona.setEntidades(Collections.<Entidad>emptyList());
//                        persona.actualizarTotal();
//                        persona.setTipo(Persona.INACTIVO);
//                        persona.setImagen(null);
                        helper.getPersonaDao().delete(persona);
//                        getPersonaDao().update(persona);
                    }

                    return true;
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "No se han podido eliminar las deudas de los acreedores. " + e.getMessage());
            borrados = false;
        }

        return borrados;
    }

    boolean recargarPersona(Persona persona) {
        try {
            return getPersonaDao().refresh(persona) == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido recargar la persona.", e);
        }
    }

    boolean recargarEntidad(Entidad entidad) {
        try {
            return getEntidadDao().refresh(entidad) == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido recargar la entidad.", e);
        }
    }

    List<Persona> getAcreedores() {
        try {
            return getPersonaDao().queryForEq(Persona.FIELD_TIPO, Persona.ACREEDOR);
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido obtener los acreedores.", e);
        }
    }

    List<Persona> getAcreedoresActivos() {
        QueryBuilder<Persona, String> queryBuilder = getPersonaDao().queryBuilder();
        Where where = queryBuilder.where();
        PreparedQuery<Persona> preparedQuery;

        try {
            where.eq(Persona.FIELD_TIPO, Persona.ACREEDOR)
                    .and()
                    .gt(Persona.FIELD_CANTIDAD_TOTAL, 0);
        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar crear la cláusula where para obtener los acreedores activos.", e);
        }

        try {
            preparedQuery = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar crear la sentencia preparada para obtener los acreedores activos.", e);
        }


        try {
            return getPersonaDao().query(preparedQuery);
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido obtener los acreedores activos.", e);
        }


    }

    List<Persona> getDeudores() {
        try {
            return getPersonaDao().queryForEq(Persona.FIELD_TIPO, Persona.DEUDOR);
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido obtener los deudores.", e);
        }

    }

    List<Persona> getDeudoresActivos() {
        QueryBuilder<Persona, String> queryBuilder = getPersonaDao().queryBuilder();
        Where where = queryBuilder.where();
        PreparedQuery<Persona> preparedQuery;

        try {
            where.eq(Persona.FIELD_TIPO, Persona.DEUDOR)
                    .and()
                    .gt(Persona.FIELD_CANTIDAD_TOTAL, 0);
        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar crear la cláusula where para obtener " +
                    "los deudores activos.", e);
        }

        try {
            preparedQuery = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar crear la sentencia preparada para " +
                    "obtener los deudores activos.", e);
        }

        try {
            return getPersonaDao().query(preparedQuery);
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido obtener los deudores activos.", e);
        }
    }

    List<Persona> getAmbos() {
        try {
            return getPersonaDao().queryForEq(Persona.FIELD_TIPO, Persona.AMBOS);
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido obtener ambos.", e);
        }
    }

    List<Persona> getAmbosActivos() {
        QueryBuilder<Persona, String> queryBuilder = getPersonaDao().queryBuilder();
        Where where = queryBuilder.where();
        PreparedQuery<Persona> preparedQuery;

        try {
            where.eq(Persona.FIELD_TIPO, Persona.AMBOS)
                    .and()
                    .gt(Persona.FIELD_CANTIDAD_TOTAL, 0);

        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar crear la cláusula where para obtener " +
                    "ambos activos.", e);
        }

        try {
            preparedQuery = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar crear la sentencia preparada para " +
                    "obtener ambos activos.", e);
        }

        try {
            return getPersonaDao().query(preparedQuery);
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido obtener ambos activos.", e);
        }

    }

    private boolean nuevaEntidad(Entidad entidad) {
        try {
            return getEntidadDao().create(entidad) == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se ha podido crear la nueva entidad.", e);
        }
    }

    boolean nuevasEntidades(List<Entidad> entidades) {

        boolean guardadas = true;

        for (Entidad entidad : entidades) {
            guardadas = nuevaEntidad(entidad);
            if (!guardadas) {
                break;
            }
        }

        return guardadas;
    }

    boolean actualizarEntidad(Entidad entidad) {
        try {
            return getEntidadDao().update(entidad) == 1;
        } catch (SQLException e) {
            throw new RuntimeException("No se ha podido actualizar la entidad", e);
        }
    }

    Entidad getEntidad(Integer idEntidad) {
        try {
            return getEntidadDao().queryForId(idEntidad);
        } catch (SQLException e) {
            throw new RuntimeException("No se ha podido obtener la entidad.", e);
        }
    }

    List<Entidad> getEntidades(List<Integer> idsEntidades) {
        List<Entidad> entidades = new LinkedList<>();

        for (Integer id: idsEntidades) {
            try {
                entidades.add(getEntidadDao().queryForId(id));
            } catch (SQLException e) {
                throw new RuntimeException("No se ha podido obtener la entidad.", e);
            }
        }
        return entidades;
    }

    boolean eliminarEntidades(List<Entidad> entidades) {

        try {
            return getEntidadDao().delete(entidades) == entidades.size();
        } catch (SQLException e) {
            throw new RuntimeException("No se ha podido eliminar las entidades.", e);
        }

    }

    List<String> getNombresExistentes() {

        List<String> nombresExistentes = new LinkedList<>();

        try {
            GenericRawResults<String[]> resultadosRaw = getPersonaDao()
                    .queryRaw("SELECT " + Persona.FIELD_NOMBRE + " FROM " + Persona.TABLE_NAME);
            List<String[]> resultadoNombres = resultadosRaw.getResults();
            for (String[] fila : resultadoNombres) {
                nombresExistentes.add(fila[0]);
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido recuperar los contactos de las " +
                    "personas existentes.", e);
        }

        return nombresExistentes;

    }

    long getImagenesMatching(String nombreImagen) {
        String ruta = getRutaCarpetaImagenes().file(nombreImagen).build().getPath();
        QueryBuilder<Persona, String> queryBuilder = getPersonaDao().queryBuilder();
        queryBuilder.setCountOf(true);
        Where where = queryBuilder.where();
        PreparedQuery<Persona> preparedQuery;

        try {
            where.eq(Persona.FIELD_IMAGEN, ruta);
        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar obtener los contactos de imágenes que " +
                    "coincidan con una imagen nueva.", e);
        }

        try {
            preparedQuery = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar crear la sentencia preparada para " +
                    "obtener los contactos de imagenes coincidentes.", e);
        }

        try {
            return getPersonaDao().countOf(preparedQuery);
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido obtener los contactos de imágenes " +
                    "coincidentes.", e);
        }
    }

    Path.Builder getRutaCarpetaImagenes() {
        return RUTA_CARPETA_IMAGENES.duplicate();
    }

    List<Persona> getNombreEImagen() {
        QueryBuilder<Persona, String> queryBuilder = getPersonaDao().queryBuilder();
        queryBuilder.selectColumns(Persona.FIELD_NOMBRE, Persona.FIELD_IMAGEN);
        PreparedQuery<Persona> preparedQuery;

        try {
            preparedQuery = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear la sentencia preparada para " +
                    "obtener personas simples.", e);
        }

        try {
            return getPersonaDao().query(preparedQuery);
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido obtener los contactos de imágenes " +
                    "coincidentes.", e);
        }
    }

    boolean cambiarNombre(final Persona persona, final String nuevoNombre) {
        ConnectionSource connectionSource = getConnectionSource();
        boolean cambiado = false;

        try {
            cambiado = TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    if (getPersonaDao().updateId(persona, nuevoNombre) == 1) {
                        for (Entidad entidad : persona.getEntidades()) {
                            entidad.setPersona(persona);
                            if (getEntidadDao().update(entidad) == 0) {
                                Log.e(TAG, "Se le está cambiando el nombre a una persona, pero " +
                                        "no se ha podido actualizar una de sus entidades.\n" + entidad.toString());
                                return false;
                            }
                        }
                    }
                    return true;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cambiado;
    }

    boolean doesNameExist(String name) {
        QueryBuilder<Persona, String> queryBuilder = getPersonaDao().queryBuilder();
        queryBuilder.setCountOf(true);
        PreparedQuery<Persona> preparedQuery;
        Where where = queryBuilder.where();

        try {
            where.eq(Persona.FIELD_NOMBRE, name);
        } catch (SQLException e) {
            throw new RuntimeException("Error en el where al intentar comprobar si un nombre ya existe.", e);
        }

        try {
            preparedQuery = queryBuilder.prepare();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear la sentencia preparada para " +
                    "comprobar si un nombre ya existe.", e);
        }

        try {
            return getPersonaDao().countOf(preparedQuery) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido comprobar si un nombre ya existe.", e);
        }
    }
}
