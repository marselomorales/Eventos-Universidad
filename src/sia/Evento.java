package sia;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Evento {
    private String idEvento;
    private String nombre;
    private String tipo;     // charla, taller, etc. (no confundir con Sala/Equipo)
    private String fecha;    // formato libre usado por tu CSV (ej: "2025-09-17")
    private String hora;     // idem (ej: "10:30")
    private String sala;     // nombre de sala textual (puede quedar como referencia)
    private int capacidad;

    private final List<Persona> asistentes = new ArrayList<>();
    private final List<Recurso> recursos   = new ArrayList<>();

    public Evento() {}

    public Evento(String idEvento, String nombre, String tipo, String fecha, String hora, String sala, int capacidad) {
        this.idEvento = idEvento;
        this.nombre = nombre;
        this.tipo = tipo;
        this.fecha = fecha;
        this.hora = hora;
        this.sala = sala;
        this.capacidad = capacidad;
    }

    // Getters/Setters
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

    public List<Persona> getAsistentes() { return asistentes; }
    public List<Recurso> getRecursos() { return recursos; }

    // Operaciones mínimas usadas por GUI/CSV
    public boolean agregarAsistente(Persona p) {
        if (p == null) return false;
        // evita duplicados por id
        for (Persona x : asistentes) if (Objects.equals(x.getId(), p.getId())) return false;
        if (capacidad > 0 && asistentes.size() >= capacidad) return false;
        return asistentes.add(p);
    }

    public boolean eliminarAsistente(String idPersona) {
        return asistentes.removeIf(p -> Objects.equals(p.getId(), idPersona));
    }

    public boolean agregarRecurso(Recurso r) {
        if (r == null) return false;
        for (Recurso x : recursos) if (Objects.equals(x.getId(), r.getId())) return false;
        return recursos.add(r);
    }

    public boolean eliminarRecurso(String idRecurso) {
        return recursos.removeIf(r -> Objects.equals(r.getId(), idRecurso));
    }

    @Override
    public String toString() {
        return "Evento{id='" + idEvento + "', nombre='" + nombre + "', fecha='" + fecha + " " + hora + "'}";
    }
}
