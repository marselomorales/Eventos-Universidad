package sia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Evento {
    private String idEvento;
    private String nombre;
    private String tipo;     // "charla", "taller", "seminario"
    private String fecha;    // "2025-09-10"
    private String hora;     // "14:30"
    private String sala;
    private int capacidad;

    private ArrayList<Persona> asistentes; // ahora gestionamos cupos

    public Evento(String idEvento, String nombre, String tipo,
                  String fecha, String hora, String sala, int capacidad) {
        this.idEvento = idEvento;
        this.nombre = nombre;
        this.tipo = tipo;
        this.fecha = fecha;
        this.hora = hora;
        this.sala = sala;
        this.capacidad = capacidad;
        this.asistentes = new ArrayList<>();
    }

    // --- Encapsulación (getters/setters) ---
    public String getIdEvento() { return idEvento; }
    public void setIdEvento(String idEvento) { this.idEvento = idEvento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    // --- Asistentes / cupos ---
    public List<Persona> getAsistentes() {
        return Collections.unmodifiableList(asistentes);
    }

    public int getTotalAsistentes() { return asistentes.size(); }

    public boolean hayCupos() {
        return asistentes.size() < capacidad;
    }

    public int getCuposDisponibles() {
        return capacidad - asistentes.size();
    }

    // Primera parte de la sobrecarga (por Persona).
    // La segunda (por nombre/rol) la añadimos en la siguiente sub-fase.
    public boolean agregarAsistente(Persona p) {
        if (p == null || !hayCupos()) return false;
        // Evitar duplicados por idPersona
        for (Persona a : asistentes) {
            if (a.getIdPersona().equals(p.getIdPersona())) return false;
        }
        return asistentes.add(p);
    }

    @Override
    public String toString() {
        return "Evento{id='" + idEvento + "', nombre='" + nombre + "', tipo='" + tipo +
               "', fecha='" + fecha + "', hora='" + hora + "', sala='" + sala +
               "', capacidad=" + capacidad + ", inscritos=" + asistentes.size() + "}";
    }
}
