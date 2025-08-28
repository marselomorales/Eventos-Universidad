package sia;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Recurso {
    private String idRecurso;
    private String nombre;
    private String tipo;
    private boolean disponible;
    private String fechaReservada;
    private final Map<String, Set<String>> reservasPorFecha = new HashMap<>();
    private static final String ALL_DAY = "*";

    public Recurso(String idRecurso, String nombre, String tipo) {
        this.idRecurso = idRecurso;
        this.nombre = nombre;
        this.tipo = tipo;
        this.disponible = true;
        this.fechaReservada = "";
    }

    // Getters y setters
    public String getIdRecurso() { return idRecurso; }
    public void setIdRecurso(String idRecurso) { this.idRecurso = idRecurso; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public String getFechaReservada() { return fechaReservada; }
    public void setFechaReservada(String fechaReservada) { this.fechaReservada = fechaReservada; }

    // Sobrecarga de métodos para reservas
    public boolean reservar(String fecha) {
        return reservar(fecha, null);
    }

    public boolean reservar(String fecha, String hora) {
        if (fecha == null || fecha.isBlank()) return false;
        
        String f = fecha.trim();
        String h = (hora == null || hora.isBlank()) ? ALL_DAY : hora.trim();
        
        Set<String> horas = reservasPorFecha.get(f);
        if (horas == null) {
            horas = new HashSet<>();
            reservasPorFecha.put(f, horas);
        }
        
        if (horas.contains(ALL_DAY)) return false;
        if (ALL_DAY.equals(h) && !horas.isEmpty()) return false;
        if (horas.contains(h)) return false;
        
        horas.add(h);
        this.disponible = false;
        this.fechaReservada = ALL_DAY.equals(h) ? f : f + " " + h;
        return true;
    }

    @Override
    public String toString() {
        String estado = disponible ? "Disponible" : "Reservado (" + fechaReservada + ")";
        return nombre + " (" + tipo + ") - " + estado;
    }
}