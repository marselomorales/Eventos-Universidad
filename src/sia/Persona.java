package sia;

import java.util.Objects;

public abstract class Persona {
    private String id;
    private String nombre;
    private String email;

    public Persona() {}

    public Persona(String id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    /** Ej.: "Estudiante" o "Profesor" */
    public abstract String getTipo();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    // --- Métodos puente para compatibilidad con código legado (Swing, CSV viejo, etc.) ---
    public String getIdPersona() { return getId(); }   // antes se usaba getIdPersona()
    public String getRol() { return getTipo(); }       // antes se usaba getRol() ("Alumno"/"Docente")
    
    @Override
    public String toString() {
        return getTipo() + "{id='" + id + "', nombre='" + nombre + "', email='" + email + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Persona)) return false;
        Persona persona = (Persona) o;
        return id != null && id.equals(persona.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
