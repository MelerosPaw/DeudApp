package melerospaw.deudapp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import melerospaw.deudapp.modelo.Contact;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.memoryutil.MemoryUtil;
import melerospaw.memoryutil.Path;
import melerospaw.memoryutil.Result;

public class GestorDatos {

    private static final String SUFFIX_SEPARATOR = "_";

    private static GestorDatos instance;
    private DatabaseHelper databaseHelper;

    private GestorDatos(Context context) {
        databaseHelper = DatabaseHelper.getHelper(context);
    }


    public static GestorDatos getGestor(Context context) {
        if (instance == null) {
            instance = new GestorDatos(context);
        }

        return instance;
    }


    /**
     * Devuelve una lista con todas las personas de la base de datos.
     */
    public List<Persona> getPersonas() {
        return databaseHelper.getPersonas();
    }


    public List<String> getNombresExistentes() {
        return databaseHelper.getNombresExistentes();
    }

    public List<Contact> getPersonaSimple(){
        return ContactManager.parsePersonas(databaseHelper.getNombreEImagen());
    }

    /**
     * Guarda el acreedor en la base de datos.
     */
    public boolean nuevaPersona(Persona persona) {
        return databaseHelper.nuevaPersona(persona);
    }


    /**
     * Devuelve una persona buscándola por su nombre.
     */
    public Persona getPersona(String nombre) {
        return databaseHelper.getPersona(nombre);
    }


    /**
     * Elimina todas las deudas de una persona y deja su cantidadTotal a 0
     */
    public boolean eliminarPersona(final List<Persona> personas) {
        eliminarImagenes(personas);
        return databaseHelper.eliminarPersonas(personas);
    }

    private void eliminarImagenes(List<Persona> personas) {
        for (Persona persona : personas) {
            if (persona.tieneImagen()) {
                Result result = MemoryUtil.deleteFile(persona.getImagen(), false);
                if (!result.isSuccessful()) {
                    Log.e("DEUDAPP", String.format("No se ha podido eliminar la foto de %1$s en %2$s.",
                            persona.getNombre(), persona.getImagen()));
                }
            }
        }
    }

    /**
     * Recarga los campos de inicialización tardía de una persona.
     *
     * @param persona La persona para recargar.
     * @return Devuelve {@code true} si la persona se ha recargado. En caso contrario, lanza una
     * RuntimeException.
     */
    public boolean recargarPersona(Persona persona) {
        return databaseHelper.recargarPersona(persona);
    }

    /**
     * Devuelve una lista con todos los acreedores.
     */
    public List<Persona> getAcreedores() {
        return databaseHelper.getAcreedores();
    }

    /**
     * Devuelve solo los acreedores que tengan deudas activas
     */
    public List<Persona> getAcreedoresActivos() {
        return databaseHelper.getAcreedoresActivos();
    }

    /**
     * Devuelve una lista con todos los deudores.
     */
    public List<Persona> getDeudores() {
        return databaseHelper.getDeudores();
    }

    /**
     * Devuelve solo los acreedores que tengan deudas activas
     */
    public List<Persona> getDeudoresActivos() {
        return databaseHelper.getDeudoresActivos();
    }

    /**
     * Devuelve aquellas personas que tengan deudas y derechos de cobro.
     *
     * @return Lista de personas cuyo tipo sea {@code Persona.AMBOS} o una lista vacía si no hay
     * ninguna.
     */
    public List<Persona> getAmbos() {
        return databaseHelper.getAmbos();
    }

    /**
     * Devuelve aquellas personas que tengan deudas y derechos de cobro aún sin liquidar.
     *
     * @return Lista de personas cuyo tipo sea {@code Persona.AMBOS} y cuya {@code cantidadTotal}
     * no sea 0.
     */
    public List<Persona> getAmbosActivos() {
        return databaseHelper.getAmbosActivos();
    }

    /**
     * Devuelve una deuda.
     */
    public Entidad getEntidad(Integer id) {
        return databaseHelper.getEntidad(id);
    }

    /**
     * Devuelve una lista con las deudas de un deudor.
     */
    public List<Entidad> getDeudas(Persona persona) {
        return persona.getDeudas();
    }

    /**
     * Elimina varias deudas de la base de datos.
     */
    public boolean eliminarEntidades(List<Entidad> entidades) {
        return databaseHelper.eliminarEntidades(entidades);
    }

    /**
     * Guarda en la base de datos una serie de Deudas a las que ya se les ha tenido que asignar
     * su acreedor.
     */
    private boolean nuevasEntidades(List<Entidad> entidades) {
        return databaseHelper.nuevasEntidades(entidades);
    }

    /**
     * Recibe una cantidad de deudas y acreedores, los asocia y los guarda. Si algún acreedor no
     * existe, lo crea.
     */
    public boolean crearEntidadesPersonas(final List<Persona> personas,
                                          final List<Entidad> entidades,
                                          @Persona.TipoPersona final int tipoPersona) {

        ConnectionSource connectionSource = databaseHelper.getConnectionSource();
        boolean correcto;

        try {
            correcto = TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {

                    List<String> nombresExistentes = databaseHelper.getNombresExistentes();
                    boolean guardado = true;

                    for (Persona nuevaPersona : personas) {
                        boolean nueva = true;

                        // Si el acreedor ya existe, la usa
                        if (nombresExistentes.contains(nuevaPersona.getNombre())) {
                            nuevaPersona = databaseHelper.getPersona(nuevaPersona.getNombre());
                            nueva = false;
                        }

                        guardado = guardarActualizar(nuevaPersona, nueva, entidades, tipoPersona);
                    }

                    return guardado;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("No se han podido guardar todas las personas nuevas y sus " +
                    "deudas. " + e.getLocalizedMessage(), e);
        }

        return correcto;
    }

    /**
     * Asigna las deudas al acreedor y viceversa, las guarda en la base de datos y después
     * actualiza el acreedor.
     */
    private boolean guardarActualizar(Persona persona, boolean nuevaPersona,
                                      List<Entidad> entidades, @Persona.TipoPersona int tipoPersona) {

        // Si la persona es nueva, se queda con el tipo inferido de la lista.
        // Si no es nueva, se comprueba lo que era antes y si es necesario, se le cambia.
        if (nuevaPersona) {
            persona.setTipo(tipoPersona);
            databaseHelper.nuevaPersona(persona);
        } else {
            if (persona.getTipo() == Persona.INACTIVO) {
                persona.setTipo(tipoPersona);
            } else if (persona.getTipo() != tipoPersona) {
                persona.setTipo(Persona.AMBOS);
            }
            databaseHelper.actualizarPersona(persona);
        }

        // Si se le mete la nueva Entidad a la colección de la persona, parece que después,
        // cuando se guarda la Entidad, el objeto Persona se actualiza y contiene la que
        // hemos metido y la que hemos guardado en la base de datos. Por eso, cuando luego
        // guardamos la persona, aparecen las Entidades duplicadas.
        // Si se guarda la persona con el objeto nuevo, no se guarda la Entidad en la base de
        // datos.
        // Parece que hay que guardar la entidad que se crea sin relacionarla con el objeto.

        for (Entidad entidad : entidades) {
            entidad.setPersona(persona);
        }

        boolean entidadesCreadas = nuevasEntidades(entidades);
        persona.actualizarTotal();
        return entidadesCreadas;
    }

    public boolean actualizarEntidad(Entidad entidad) {
        return databaseHelper.actualizarEntidad(entidad);
    }

    /**
     * Actualiza la cantidad de una deuda.
     */
    public boolean actualizarPersona(Persona persona, int tipo) {

        persona.actualizarTotal();

        if (persona.getTipo() != tipo) {
            persona.setTipo(Persona.AMBOS);
        }

        return databaseHelper.actualizarPersona(persona);
    }

    public boolean guardarFoto(Context context, Persona persona, Uri uri) {

        boolean guardada;

        Bitmap bitmap = null;

        try {
            InputStream imageStream = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(imageStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (bitmap != null) {

            if (persona.tieneImagen()) {
                MemoryUtil.deleteFile(persona.getImagen(), false);
            }
            String nombreImagen = getUniqueName(persona.getNombre());
            Path rutaPath = databaseHelper.getRutaCarpetaImagenes()
                    .file(nombreImagen)
                    .build();
            Result<File> result = MemoryUtil.saveBitmap(bitmap, rutaPath);
            if (result.isSuccessful()) {
                persona.setImagen(rutaPath.getPath());
            }
            guardada = result.isSuccessful() && databaseHelper.actualizarPersona(persona);
        } else {
            guardada = false;
        }

        return guardada;
    }

    private String getUniqueName(String nombrePersona) {
        String nombreImagen = Normalizer.normalize(nombrePersona, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll(" ", "")
                .trim();

        boolean nombreRepetido = imagenRepetida(nombreImagen);
        while (nombreRepetido) {
            nombreImagen = establecerSufijo(nombreImagen);
            nombreRepetido = imagenRepetida(nombreImagen);
        }

        return nombreImagen;
    }

    private String establecerSufijo(String nombre) {
        String nombreConSufijo;
        if (tieneSufijo(nombre)) {
            nombreConSufijo = aumentarSufijo(nombre);
        } else {
            nombreConSufijo = setSufijo(nombre);
        }

        return nombreConSufijo;
    }

    private boolean tieneSufijo(String nombre) {
        return nombre.contains(SUFFIX_SEPARATOR)  && nombre.lastIndexOf(SUFFIX_SEPARATOR) != nombre.length() - 1;
    }

    private String setSufijo(String nombre) {
        return nombre + SUFFIX_SEPARATOR + "1";
    }

    private String aumentarSufijo(String nombre) {
        int sufijo = obtenerSufijo(nombre) + 1;
        return nombre.substring(0, nombre.length() - 1) + sufijo;
    }

    private int obtenerSufijo(String nombre) {
        return Integer.parseInt(nombre.substring(nombre.length()-1));
    }

    private boolean imagenRepetida(String nombreImagen) {
        return databaseHelper.getImagenesMatching(nombreImagen) > 0;
    }

    public boolean borrarImagen(Persona persona) {

        boolean eliminada;

        if (persona.tieneImagen()) {
            Result result = MemoryUtil.deleteFile(persona.getImagen(), false);
            if (result.isSuccessful()) {
                persona.setImagen(null);
                eliminada = databaseHelper.actualizarPersona(persona);
            } else {
                eliminada = false;
            }
        } else {
            eliminada = true;
        }

        return eliminada;
    }

    public List<Contact> getContacts(Context context) {
        return ContactManager.obtainContacts(context);
    }

    public boolean cambiarNombre(Persona persona, String nuevoNombre) {
        return !isNombreRepetido(nuevoNombre) && databaseHelper.cambiarNombre(persona, nuevoNombre);
    }

    private boolean isNombreRepetido(String nombre){
        return databaseHelper.doesNameExist(nombre);
    }

    public List<Entidad> getEntidades(List<Integer> idsEntidades) {
        return databaseHelper.getEntidades(idsEntidades);
    }

    public boolean recargarEntidad(Entidad entidad) {
        return databaseHelper.recargarEntidad(entidad);
    }
}
