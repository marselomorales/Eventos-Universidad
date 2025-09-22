// path: src/sia/Recurso.java
package sia;

import java.util.Objects;

public abstract class Recurso {
    private String id;
    private String nombre;

    // --- Compatibilidad con código legado ---
    // (algunos componentes esperan estas propiedades/métodos en Recurso)
    private boolean disponible = true;     // en Sala se ignora; en Equipo sí importa
    private String  fechaReservada = "";   // persistía en CSV viejo; ahora es opcional

    public Recurso() {}

    public Recurso(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /** Ej.: "Sala" o "Equipo" */
    public abstract String getTipo();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // --------- Métodos puente para compatibilidad (GUI/CSV viejo) ---------
    public String getIdRecurso() { return getId(); }   // antes se usaba getIdRecurso()
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public String getFechaReservada() { return fechaReservada; }

    /** Reservas "texto" legado (no afectan la lógica nueva). */
    public void reservar(String fecha) {
        this.fechaReservada = fecha != null ? fecha.trim() : "";
    }
    public void reservar(String fecha, String hora) {
        String f = fecha != null ? fecha.trim() : "";
        String h = hora  != null ? hora.trim()  : "";
        this.fechaReservada = (f.isEmpty() && h.isEmpty()) ? "" : (f + (h.isEmpty() ? "" : " " + h));
    }
    // ---------------------------------------------------------------------

    @Override
    public String toString() {
        return getTipo() + "{id='" + id + "', nombre='" + nombre + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recurso)) return false;
        Recurso recurso = (Recurso) o;
        return id != null && id.equals(recurso.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
