package sia;

public class Persona {
    private String idPersona;
    private String nombre;
    private String rol;

    public Persona(String idPersona, String nombre, String rol) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.rol = rol;
    }

    // Getters y setters
    public String getIdPersona() { return idPersona; }
    public void setIdPersona(String idPersona) { this.idPersona = idPersona; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    @Override
    public String toString() {
        return nombre + " (" + rol + ")";
    }
}