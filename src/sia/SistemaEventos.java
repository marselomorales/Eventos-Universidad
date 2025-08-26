package sia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SistemaEventos {
    private final ArrayList<Evento> eventos = new ArrayList<>();
    private final HashMap<String, Evento> indexPorId = new HashMap<>();

    public void seed() {
        // Personas
        Persona p1 = new Persona("P001", "Ana Díaz", "docente");
        Persona p2 = new Persona("P002", "Benjamín Soto", "estudiante");
        Persona p3 = new Persona("P003", "Carla Pérez", "estudiante");

        // Eventos
        Evento e1 = new Evento("E101", "Charla IA aplicada", "charla",
                "2025-09-05", "11:00", "B-201", 50);
        Evento e2 = new Evento("E102", "Taller Git básico", "taller",
                "2025-09-07", "16:00", "Lab-3", 25);
        Evento e3 = new Evento("E103", "Seminario DataViz", "seminario",
                "2025-09-12", "10:00", "Auditorio", 120);

        // Algunos asistentes de inicio
        e1.agregarAsistente(p1);
        e1.agregarAsistente(p2);
        e2.agregarAsistente(p3);

        addEvento(e1);
        addEvento(e2);
        addEvento(e3);
    }

    public void addEvento(Evento e) {
        if (e == null) return;
        eventos.add(e);
        indexPorId.put(e.getIdEvento(), e);
    }

    public List<Evento> getEventos() {
        return new ArrayList<>(eventos);
    }

    public List<Evento> listarEventosConCupo() {
        ArrayList<Evento> res = new ArrayList<>();
        for (Evento e : eventos) {
            if (e.hayCupos()) res.add(e);
        }
        return res;
    }

    public Evento buscarPorId(String id) {
        return indexPorId.get(id);
    }
}
