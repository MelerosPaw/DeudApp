package melerospaw.deudapp.iu;

import java.util.Comparator;

import melerospaw.deudapp.modelo.Entidad;

public class EntidadComparator implements Comparator<Entidad> {

    @Override
    public int compare(Entidad lhs, Entidad rhs) {
        return rhs.getFecha().compareTo(lhs.getFecha());
    }
}
