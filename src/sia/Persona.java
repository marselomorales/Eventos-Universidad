/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sia;

/**
 *
 * @author marce
 */
public class Persona {
    private String idPersona;
    private String nombre;
    private String rol; // "docente" / "estudiante"

    public Persona(String idPersona, String nombre, String rol) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getIdPersona() { return idPersona; }
    public void setIdPersona(String idPersona) { this.idPersona = idPersona; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    @Override
    public String toString() {
        return "Persona{id='" + idPersona + "', nombre='" + nombre + "', rol='" + rol + "'}";
    }
}
