package melerospaw.deudapp.modelo;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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

    public float getCantidadTotal() {
        actualizarTotal();
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
        List<Entidad> deudas = new ArrayList<>();
        for (Entidad entidad : entidades) {
            if (entidad.getTipoEntidad() == Entidad.DEUDA) {
                deudas.add(entidad);
            }
        }

        return deudas;
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

    /**Returns oldest Deuda human readable date. If there is ony one debt for this , returns the date.
     * @return A {@code String} with a date formatted as "05/11/2015".*/
    public String getOldest(){

        List<Entidad> lista = new LinkedList<>(entidades);

        if (lista.size() > 1){
            Entidad oldest = lista.get(0);
            for (Entidad entidad : lista){
                if (entidad.compareTo(oldest) > 0)
                    oldest = entidad;
            }
            return oldest.getReadableDate();
        } else
            return lista.get(0).getReadableDate();
    }

    public void addEntidad(Entidad entidad) {
        entidades.add(entidad);
        actualizarTotal();
    }

    /**Updates the total debt amount.*/
    public void actualizarTotal(){

        float total = 0;

        if (tipo == ACREEDOR || tipo == DEUDOR) {
            for (Entidad entidad : entidades) {
                total += entidad.getCantidad();
            }
        } else if (tipo == AMBOS){
            float totalDeudas = 0;
            float totalDerechosCobro = 0;
            for (Entidad entidad : entidades) {
                if (entidad.getTipoEntidad() == Entidad.DEUDA) {
                    totalDeudas += entidad.getCantidad();
                } else if (entidad.getTipoEntidad() == Entidad.DERECHO_COBRO) {
                    totalDerechosCobro += entidad.getCantidad();
                }
            }

            total = totalDeudas + totalDerechosCobro;
        }

        cantidadTotal = total;
    }

    public boolean estanLasDeudasCanceladas() {
        for (Entidad entidad : entidades) {
            if (!entidad.estaCancelada()) {
                return false;
            }
        }

        return true;
    }

    public boolean tieneImagen() {
        return !TextUtils.isEmpty(imagen);
    }

    @Override
    public String toString(){
        return getNombre() + "\n" +
                "TIPO: " + getTipoPersonaString() + "\n" +
                "DEUDAS: " + getDeudas().size() + "\n" +
                "DERECHOS COBRO: " + getDerechosCobro().size() + "\n" +
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

    public String getTipoPersonaString(){
        switch (tipo) {
            case ACREEDOR: return "Acreedor";
            case DEUDOR: return "Deudor";
            case AMBOS: return "Ambos";
            case INACTIVO: return "Inactivo";
            default: return "Sin definir";
        }
    }
}
