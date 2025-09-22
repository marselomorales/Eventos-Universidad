package sia;

public class Profesor extends Persona {
    private String departamento;
    private String categoria;

    public Profesor() {}

    public Profesor(String id, String nombre, String email, String departamento, String categoria) {
        super(id, nombre, email);
        this.departamento = departamento;
        this.categoria = categoria;
    }

    @Override
    public String getTipo() { return "Profesor"; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
