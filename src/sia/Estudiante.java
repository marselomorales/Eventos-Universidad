package sia;

public class Estudiante extends Persona {
    private String carrera;
    private int nivel;

    public Estudiante() {}

    public Estudiante(String id, String nombre, String email, String carrera, int nivel) {
        super(id, nombre, email);
        this.carrera = carrera;
        this.nivel = nivel;
    }

    @Override
    public String getTipo() { return "Estudiante"; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
}
