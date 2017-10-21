package melerospaw.deudapp.modelo;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

@DatabaseTable(tableName = "Entidades")
public class Entidad implements Comparable<Entidad>, Serializable {

    private static final int INDEFINIDA = -1;
    public static final int DEUDA = 0;
    public static final int DERECHO_COBRO = 1;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @IntDef({INDEFINIDA, DEUDA, DERECHO_COBRO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TipoEntidad{}

    @DatabaseField(columnName = "id", generatedId = true)                               private Integer id;
    @DatabaseField(columnName = "concepto")                                             private String concepto;
    @DatabaseField(columnName = "cantidad")                                             private float cantidad;
    @DatabaseField(columnName = "fecha")                                                private Date fecha;
    @DatabaseField(columnName = "tipoEntidad")                                          private int tipoEntidad;
    @DatabaseField(columnName = "persona", foreign = true, foreignAutoRefresh = true)   private Persona persona;

    public Entidad() {
        this.cantidad = 0.00f;
        this.concepto = "";
        this.tipoEntidad = INDEFINIDA;
    }

    public Entidad(@TipoEntidad int tipoEntidad) {
        this.cantidad = 0.00f;
        this.concepto = "";
        this.tipoEntidad = tipoEntidad;
        this.fecha = getFechaSimple();
    }

    public Entidad(float cantidad, String concepto, @TipoEntidad int tipo) {
        this.tipoEntidad = tipo;
        this.concepto = concepto;
        this.fecha = getFechaSimple();
        setCantidad(cantidad);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public float getCantidad() {
        return cantidad;
    }

    public void setCantidad(float cantidad) {

        if (tipoEntidad == DEUDA) {
            this.cantidad = -cantidad;
        } else {
            this.cantidad = cantidad;
        }
    }

    public void setTipoEntidad(int tipoEntidad) {
        this.tipoEntidad = tipoEntidad;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        if (!esTipoPersonaCorrecto(persona.getTipo())) {
            throw new IllegalStateException("No se puede asignar la entidad " + this.getConcepto() +
                    " a " + persona.getNombre() + " porque no es del tipo correcto.");
        } else {
            this.persona = persona;
        }
    }

    public @TipoEntidad int getTipoEntidad(){
        return this.tipoEntidad;
    }

    public String getReadableDate() {
        return SDF.format(getFecha());
    }

    // Las deudas pueden tener un deudor o un "ambos". Los derechos de cobro pueden tener un acreedor o un "ambos".
    private boolean esTipoPersonaCorrecto(int tipoPersona){
        boolean esDeudor = getTipoEntidad() == DERECHO_COBRO && tipoPersona == Persona.DEUDOR;
        boolean esAcreedor = getTipoEntidad() == DEUDA && tipoPersona == Persona.ACREEDOR;
        boolean esAmbos = tipoPersona == Persona.AMBOS;

        return esDeudor || esAcreedor || esAmbos;
    }

    public void aumentar(float cantidad) {
        if (tipoEntidad == DERECHO_COBRO) {
            this.cantidad = getCantidad() + cantidad;
        } else if (tipoEntidad == DEUDA){
            this.cantidad = getCantidad() - cantidad;
        } else {
            throw new IllegalStateException("La entidad es del tipo INDEFINIDO. ¿Cómo es posible?");
        }
    }

    public void disminuir(float cantidad) {
        if (tipoEntidad == DERECHO_COBRO) {
            this.cantidad = getCantidad() - cantidad;
        } else if (tipoEntidad == DEUDA){
            this.cantidad = getCantidad() + cantidad;
        } else {
            throw new IllegalStateException("La entidad es del tipo INDEFINIDO. ¿Cómo es posible?");
        }
    }

    public boolean estaDefinida() {
        return getTipoEntidad() != INDEFINIDA && estaCompleta();
    }

    public boolean estaCompleta() {
        return !TextUtils.isEmpty(concepto.trim()) && cantidad != 0f;
    }

    @Override
    public int compareTo(@NonNull Entidad otraEntidad) {
        return esMismoDia(otraEntidad) ? 0 : -1;
    }

    public boolean esMismoDia(Entidad otraEntidad) {
        Calendar calOtraEntidad = Calendar.getInstance();
        calOtraEntidad.setTime(otraEntidad.getFecha());
        Calendar calEstaEntidad = Calendar.getInstance();
        calEstaEntidad.setTime(getFecha());

        boolean esMismoAno = calEstaEntidad.get(Calendar.YEAR) == calOtraEntidad.get(Calendar.YEAR);
        boolean esMismoMes = calEstaEntidad.get(Calendar.MONTH) == calOtraEntidad.get(Calendar.MONTH);
        boolean esMismoDia = calEstaEntidad.get(Calendar.DAY_OF_MONTH) == calOtraEntidad.get(Calendar.DAY_OF_MONTH);

        return esMismoAno && esMismoMes && esMismoDia;
    }

    public boolean estaCancelada() {
        return cantidad == 0f;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return concepto + " - " + cantidad;
    }

    public static final Comparator<Entidad> COMPARATOR = new Comparator<Entidad>() {
        @Override
        public int compare(Entidad entidad, Entidad t1) {
            if (entidad.getFecha().after(t1.getFecha())) {
                return 1;
            } else if (entidad.getFecha().before(t1.getFecha())) {
                return -1;
            } else {
                String normalizedConcept1 = Normalizer.normalize(entidad.getConcepto(), Normalizer.Form.NFD);
                String normalizedConcept2 = Normalizer.normalize(t1.getConcepto(), Normalizer.Form.NFD);
                return normalizedConcept1.compareToIgnoreCase(normalizedConcept2);
            }
        }
    };

    public static Date formatearFecha(String fecha) throws ParseException {
        return SDF.parse(fecha);
    }

    public static String formatearFecha(Date fecha) {
        return SDF.format(fecha);
    }

    private Date getFechaSimple() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
