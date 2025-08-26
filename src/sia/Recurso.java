/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sia;

/**
 *
 * @author marce
 */
public class Recurso {
    private String idRecurso;
    private String nombre;
    private String tipo;        // ej: "proyector", "sala", "audio"
    private boolean disponible; // bandera simple para Fase 1

    public Recurso(String idRecurso, String nombre, String tipo) {
        this.idRecurso = idRecurso;
        this.nombre = nombre;
        this.tipo = tipo;
        this.disponible = true; // por defecto disponible
    }

    public String getIdRecurso() { return idRecurso; }
    public void setIdRecurso(String idRecurso) { this.idRecurso = idRecurso; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    @Override
    public String toString() {
        return "Recurso{id='" + idRecurso + "', nombre='" + nombre + "', tipo='" + tipo +
               "', disponible=" + disponible + "}";
    }    
}
