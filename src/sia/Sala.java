package sia;

public class Sala extends Recurso {
    private int capacidad;
    private String ubicacion;

    public Sala() {}

    public Sala(String id, String nombre, int capacidad, String ubicacion) {
        super(id, nombre);
        this.capacidad = capacidad;
        this.ubicacion = ubicacion;
    }

    @Override
    public String getTipo() { return "Sala"; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
}
