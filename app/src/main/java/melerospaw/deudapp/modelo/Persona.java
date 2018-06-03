package melerospaw.deudapp.modelo;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import melerospaw.deudapp.utils.EntidadesUtilKt;
import melerospaw.deudapp.utils.InfinityManagerKt;
import melerospaw.deudapp.utils.SecureOperationKt;

@DatabaseTable (tableName = Persona.TABLE_NAME)
public class Persona implements Serializable{

    public static final int INACTIVO = -1;
    public static final int ACREEDOR = 1;
    public static final int DEUDOR = 2;
    public static final int AMBOS = 3;

    @IntDef({INACTIVO, ACREEDOR, DEUDOR, AMBOS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TipoPersona{}


    public static final String TABLE_NAME = "Persona";
    public static final String FIELD_NOMBRE = "nombre";
    public static final String FIELD_ENTIDADES = "entidades";
    public static final String FIELD_CANTIDAD_TOTAL = "cantidadTotal";
    public static final String FIELD_COLOR = "color";
    public static final String FIELD_TIPO = "tipo";
    public static final String FIELD_IMAGEN = "imagen";

    @DatabaseField(columnName = FIELD_NOMBRE, id = true)        private String nombre;
    @ForeignCollectionField (columnName = FIELD_ENTIDADES)      private Collection<Entidad> entidades;
    @DatabaseField (columnName = FIELD_CANTIDAD_TOTAL)          private float cantidadTotal;
    @DatabaseField (columnName = FIELD_COLOR)                   private int color;
    @DatabaseField (columnName = FIELD_TIPO)                    private int tipo;
    @DatabaseField (columnName = FIELD_IMAGEN)                  private String imagen;


    public Persona(float cantidadTotal, int color, String nombre, Collection<Entidad> entidades,
                   @TipoPersona int tipo) {
        this.cantidadTotal = cantidadTotal;
        this.color = color;
        this.entidades = entidades;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Persona() {
        this.nombre = "";
        this.entidades = new LinkedList<>();
        this.color = -1;
    }

    public Persona(String nombre, @TipoPersona int tipo) {
        this.nombre = nombre;
        this.entidades = new LinkedList<>();
        this.cantidadTotal = 0;
        this.color = -1;
        this.tipo = tipo;
    }

    public Persona(String nombre, String imagen) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.entidades = new LinkedList<>();
        this.tipo = 0;
        this.color = -1;
        this.cantidadTotal = 0;
    }

    public Persona(String nombre, String imagen, int color) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.entidades = new LinkedList<>();
        this.tipo = 0;
        this.color = color;
        this.cantidadTotal = 0;
    }

    public float getCantidadTotal() {
        calcularTotal();
        return cantidadTotal;
    }

    public float getCantidadTotal(Entidad entidadOmitida) {
        calcularTotal(entidadOmitida);
        return cantidadTotal;
    }

    public void setCantidadTotal(float cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Entidad> getDeudas() {
        List<Entidad> entidads = new ArrayList<>();
        for (Entidad entidad : entidades) {
            if (entidad.getTipoEntidad() == Entidad.DEUDA) {
                entidads.add(entidad);
            }
        }

        return entidads;
    }

    public List<Entidad> getDerechosCobro() {
        List<Entidad> derechosCobro = new ArrayList<>();
        for (Entidad entidad : entidades) {
            if (entidad.getTipoEntidad() == Entidad.DERECHO_COBRO) {
                derechosCobro.add(entidad);
            }
        }

        return derechosCobro;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public @TipoPersona int getTipo() {
        return tipo;
    }

    public void setTipo(@TipoPersona int tipo) {
        this.tipo = tipo;
    }

    public void setEntidades(Collection<Entidad> entidades) {
        this.entidades = entidades;
    }

    public List<Entidad> getEntidades() {
        return new ArrayList<>(entidades);
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    /**Returns oldest Entidad human readable date. If there is ony one debt for this , returns the date.
     * @return A {@code String} with a date formatted as "05/11/2015".*/
    public String getOldest(){

        List<Entidad> lista = new LinkedList<>(entidades);

        if (lista.isEmpty()) {
            return "Sin deudas";
        } else if (lista.size() > 1){
            Entidad oldest = lista.get(0);
            for (Entidad entidad : lista){
                if (entidad.compareTo(oldest) > 0)
                    oldest = entidad;
            }
            return oldest.getReadableDate();
        } else {
            return lista.get(0).getReadableDate();
        }
    }

    /**Returns newest Entidad human readable date. If there is ony one debt, returns the date.
     * @return A {@code String} with a date formatted as "05/11/2015".*/
    public String getNewest(){

        List<Entidad> lista = new LinkedList<>(entidades);

        if (lista.isEmpty()) {
            return "Sin deudas";
        } else if (lista.size() > 1){
            Entidad newest = lista.get(0);
            for (Entidad entidad : lista){
                if (entidad.compareTo(newest) < 0)
                    newest = entidad;
            }
            return newest.getReadableDate();
        } else {
            return lista.get(0).getReadableDate();
        }
    }

    public void addEntidad(Entidad entidad) {
        entidades.add(entidad);
        calcularTotal();
    }

    /**Updates the total debt amount.*/
    public void calcularTotal() {
        calcularTotal(null);
    }

    /**Updates the total debt amount.*/
    private void calcularTotal(@Nullable Entidad entidadOmitida) {

        float total = 0;

        if (tipo == ACREEDOR || tipo == DEUDOR) {
            for (Entidad entidad : entidades) {
                if (entidadOmitida == null || !entidad.equals(entidadOmitida)) {
                    total = SecureOperationKt.secureAdd(total, entidad.getCantidad());
                    if (InfinityManagerKt.isInfiniteFloat(total)) {
                        break;
                    }
                }
            }
        } else if (tipo == AMBOS) {
            float totalDeudas = 0;
            float totalDerechosCobro = 0;
            for (Entidad entidad : entidades) {
                if (entidadOmitida == null || !entidad.equals(entidadOmitida)) {
                    if (entidad.getTipoEntidad() == Entidad.DEUDA &&
                            !InfinityManagerKt.isInfiniteFloat(totalDeudas)) {
                        totalDeudas = SecureOperationKt.secureAdd(totalDeudas, entidad.getCantidad());
                    } else if (entidad.getTipoEntidad() == Entidad.DERECHO_COBRO &&
                            !InfinityManagerKt.isInfiniteFloat(totalDerechosCobro)) {
                        totalDerechosCobro = SecureOperationKt.secureAdd(totalDerechosCobro, entidad.getCantidad());
                    }

                    if (InfinityManagerKt.isInfiniteFloat(totalDeudas) &&
                            InfinityManagerKt.isInfiniteFloat(totalDerechosCobro)) {
                        break;
                    }
                }
            }

            if ((InfinityManagerKt.isInfiniteFloat(totalDeudas) &&
                    InfinityManagerKt.isInfiniteFloat(totalDerechosCobro)) ||
                    InfinityManagerKt.isInfiniteFloat(totalDerechosCobro)) {
                total = InfinityManagerKt.getInfiniteFloat();
            } else if (InfinityManagerKt.isInfiniteFloat(totalDeudas)) {
                total = InfinityManagerKt.getNegativeInfiniteFloat();
            } else {
                total = totalDeudas + totalDerechosCobro;
            }
        }

        cantidadTotal = total;
    }

    public boolean estanLasDeudasCanceladas() {
        return EntidadesUtilKt.estanCanceladas(getEntidades());
    }

    public boolean tieneImagen() {
        return !TextUtils.isEmpty(imagen);
    }

    @Override
    public String toString() {
        String cantidadDeudas = getDeudas() == null ? "VACÍO" : Integer.toString(getDeudas().size());
        String cantidadDerechosCobro = getDerechosCobro() == null ? "VACÍO" : Integer.toString(getDerechosCobro().size());

        return getNombre() + "\n" +
                "TIPO: " + getTipoPersonaDisplay() + "\n" +
                "DEUDAS: " + cantidadDeudas + "\n" +
                "DERECHOS COBRO: " + cantidadDerechosCobro + "\n" +
                "TOTAL DEBIDO: " + getCantidadTotal();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == null) {
            throw new IllegalStateException("Estás intentando comparar esta persona con un nulo.");
        } else if (!(other instanceof Persona)) {
                throw new IllegalStateException("Estás intentando comparar una persona con un " +
                        "objeto de la clase " + other.getClass().getSimpleName());
        } else {
            return ((Persona) other).getNombre().equals(nombre);
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getTipoPersonaDisplay(){
        switch (tipo) {
            case ACREEDOR: return "Acreedor";
            case DEUDOR: return "Deudor";
            case AMBOS: return "Ambos";
            case INACTIVO: return "Inactivo";
            default: return "Sin definir";
        }
    }

    public boolean hasDeuda(Entidad entidad) {
        return EntidadesUtilKt.estaContenida(entidad, getEntidades());
    }

    public boolean hasConceptoRepetido(String concepto, Date fecha) {
        return EntidadesUtilKt.contieneDeudaSimilar(concepto, fecha, getEntidades());
    }
}
