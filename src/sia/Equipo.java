package sia;

public class Equipo extends Recurso {
    private String tipoEquipo;   // p.ej. "Proyector", "Notebook"

    public Equipo() {}

    public Equipo(String id, String nombre, String tipoEquipo, boolean disponible) {
        super(id, nombre);
        this.tipoEquipo = tipoEquipo;
        setDisponible(disponible); // usa el campo de compatibilidad en Recurso
    }

    @Override
    public String getTipo() { return "Equipo"; }

    public String getTipoEquipo() { return tipoEquipo; }
    public void setTipoEquipo(String tipoEquipo) { this.tipoEquipo = tipoEquipo; }
}
