package sia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un evento en el sistema.
 * Contiene información sobre el evento, asistentes y recursos asignados.
 */
public class Evento {
    private String idEvento;
    private String nombre;
    private String tipo;
    private String fecha;
    private String hora;
    private String sala;
    private int capacidad;
    private ArrayList<Persona> asistentes;
    private ArrayList<Recurso> recursos;
    
    //Constructor para crear un nuevo evento
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
        this.recursos = new ArrayList<>();
    }

    // Getters y setters
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
    
    public void setAsistentes(List<Persona> nuevos) {
        this.asistentes = (nuevos == null) ? new ArrayList<>() : new ArrayList<>(nuevos);
    }
    public void setRecursos(List<Recurso> nuevos) {
        this.recursos = (nuevos == null) ? new ArrayList<>() : new ArrayList<>(nuevos);
    }


    // Métodos para gestión de asistentes
    public List<Persona> getAsistentes() {
        return Collections.unmodifiableList(asistentes);
    }

    public boolean hayCupos() {
        return asistentes.size() < capacidad;
    }

    public int getCuposRestantes() {
        return Math.max(0, capacidad - asistentes.size());
    }

    // Sobrecarga de métodos para agregar asistentes
    public boolean agregarAsistente(Persona p) {
        if (p == null || !hayCupos()) return false;
        
        // Verificar duplicados por ID
        for (Persona a : asistentes) {
            if (a.getIdPersona().equalsIgnoreCase(p.getIdPersona())) {
                return false;
            }
        }
        return asistentes.add(p);
    }

    public boolean agregarAsistente(String nombre, String rol) {
        if (nombre == null || rol == null || !hayCupos()) return false;
        
        // Generar ID único
        String nuevoId = "A" + (asistentes.size() + 100);
        return agregarAsistente(new Persona(nuevoId, nombre, rol));
    }

    // Métodos para gestión de recursos
    public boolean agregarRecurso(Recurso recurso) {
        if (recurso == null) return false;
        return recursos.add(recurso);
    }

    public List<Recurso> getRecursos() {
        return Collections.unmodifiableList(recursos);
    }

    // Sobrecarga de métodos para reservar recursos
    public boolean reservarRecurso(String idRecurso, String fecha) {
        return reservarRecurso(idRecurso, fecha, null);
    }

    public boolean reservarRecurso(String idRecurso, String fecha, String hora) {
        if (idRecurso == null || fecha == null) return false;
        
        for (Recurso r : recursos) {
            if (r.getIdRecurso().equalsIgnoreCase(idRecurso)) {
                if (hora == null || hora.isBlank()) {
                    return r.reservar(fecha);
                } else {
                    return r.reservar(fecha, hora);
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String estado = hayCupos() ? "Disponible" : "SIN CUPO";
        return idEvento + " - " + nombre + " (" + tipo + ") - " + fecha + " - " + estado;
    }
}