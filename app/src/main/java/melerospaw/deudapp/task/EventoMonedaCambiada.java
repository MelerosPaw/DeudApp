package melerospaw.deudapp.task;

import melerospaw.deudapp.utils.Currency;

public class EventoMonedaCambiada {

    private Currency nuevaMoneda;

    public EventoMonedaCambiada(Currency nuevaMoneda) {
        this.nuevaMoneda = nuevaMoneda;
    }

    public Currency getNuevaMoneda() {
        return nuevaMoneda;
    }

    public void setNuevaMoneda(Currency nuevaMoneda) {
        this.nuevaMoneda = nuevaMoneda;
    }
}
