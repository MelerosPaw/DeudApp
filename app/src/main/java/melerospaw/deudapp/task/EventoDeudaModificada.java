package melerospaw.deudapp.task;


import melerospaw.deudapp.modelo.Persona;

public class EventoDeudaModificada {

    private Persona persona;

    public EventoDeudaModificada(Persona persona) {
        this.persona = persona;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }
}
