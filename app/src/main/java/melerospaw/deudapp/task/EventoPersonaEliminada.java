package melerospaw.deudapp.task;


import melerospaw.deudapp.modelo.Persona;

public class EventoPersonaEliminada {

    private Persona persona;

    public EventoPersonaEliminada(Persona persona) {
        this.persona = persona;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }
}
