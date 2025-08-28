package sia;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SistemaEventos {
    private final ArrayList<Evento> eventos = new ArrayList<>();
    private final Map<String, Evento> indexPorId = new HashMap<>();
    private final List<Recurso> recursosDisponibles = new ArrayList<>();

    public void seed() {
        // Personas precargadas
        Persona p1 = new Persona("A101", "Ana Díaz", "docente");
        Persona p2 = new Persona("A102", "Benjamín Soto", "estudiante");
        Persona p3 = new Persona("A103", "Carla Pérez", "estudiante");
        Persona p4 = new Persona("A104", "Diego López", "docente");
        Persona p5 = new Persona("A105", "Elena Castro", "estudiante");
        Persona p6 = new Persona("A106", "Fernando Martínez", "docente");
        Persona p7 = new Persona("A107", "Gabriela Silva", "estudiante");
        Persona p8 = new Persona("A108", "Héctor Rodríguez", "estudiante");
        Persona p9 = new Persona("A109", "Isabel González", "docente");
        Persona p10 = new Persona("A110", "Javier Méndez", "estudiante");

        // Recursos precargados
        List<Recurso> recursos = Arrays.asList(
            new Recurso("R001", "Proyector 4K", "Audiovisual"),
            new Recurso("R002", "Sala de Conferencias", "Espacio"),
            new Recurso("R003", "Micrófono Inalámbrico", "Audio"),
            new Recurso("R004", "Computadora i7", "Tecnología"),
            new Recurso("R005", "Pizarra Interactiva", "Mobiliario"),
            new Recurso("R006", "Escenario Móvil", "Estructura"),
            new Recurso("R007", "Sillas Conferencia", "Mobiliario"),
            new Recurso("R008", "Mesas para Taller", "Mobiliario"),
            new Recurso("R009", "Sistema de Luces LED", "Iluminación"),
            new Recurso("R010", "Sistema de Sonido 5.1", "Audio"),
            new Recurso("R011", "Cámaras de Video", "Audiovisual"),
            new Recurso("R012", "Plantillas de Presentación", "Material"),
            new Recurso("R013", "Internet Fibra Óptica", "Conectividad"),
            new Recurso("R014", "Impresora 3D", "Tecnología"),
            new Recurso("R015", "Tabletas Gráficas", "Tecnología")
        );
        
        recursosDisponibles.addAll(recursos);

        // Eventos precargados con diferentes tipos
        Evento e1 = new Evento("E101", "Charla de Inteligencia Artificial", "charla",
                "2025-09-05", "11:00", "B-201", 50);
        Evento e2 = new Evento("E102", "Taller de Git y GitHub", "taller",
                "2025-09-07", "16:00", "Lab-3", 25);
        Evento e3 = new Evento("E103", "Seminario de Visualización de Datos", "seminario",
                "2025-09-12", "10:00", "Auditorio", 120);
        Evento e4 = new Evento("E104", "Concierto de Música Clásica", "cultural",
                "2025-09-15", "19:00", "Auditorio", 200);
        Evento e5 = new Evento("E105", "Exposición de Arte Contemporáneo", "cultural",
                "2025-09-20", "15:00", "Galería", 80);
        Evento e6 = new Evento("E106", "Workshop de Robótica", "taller",
                "2025-09-25", "14:00", "Lab-4", 30);
        Evento e7 = new Evento("E107", "Conferencia de Cybersecurity", "conferencia",
                "2025-10-02", "09:00", "A-101", 100);
        Evento e8 = new Evento("E108", "Presentación de Proyectos Finales", "exposicion",
                "2025-10-10", "13:00", "Hall Principal", 150);
        Evento e9 = new Evento("E109", "Debate sobre Ética en IA", "debate",
                "2025-10-15", "16:30", "Sala de Usos Múltiples", 40);
        Evento e10 = new Evento("E110", "Feria de Emprendimiento", "feria",
                "2025-10-20", "10:00", "Patio Central", 300);

        // Asignar recursos a eventos
        e1.agregarRecurso(recursos.get(0));
        e1.agregarRecurso(recursos.get(3));
        e2.agregarRecurso(recursos.get(1));
        e2.agregarRecurso(recursos.get(4));
        e3.agregarRecurso(recursos.get(2));
        e3.agregarRecurso(recursos.get(3));
        e4.agregarRecurso(recursos.get(5));
        e4.agregarRecurso(recursos.get(8));
        e4.agregarRecurso(recursos.get(9));
        e5.agregarRecurso(recursos.get(8));
        e6.agregarRecurso(recursos.get(3));
        e6.agregarRecurso(recursos.get(6));
        e6.agregarRecurso(recursos.get(7));
        e7.agregarRecurso(recursos.get(0));
        e7.agregarRecurso(recursos.get(2));
        e7.agregarRecurso(recursos.get(9));
        e8.agregarRecurso(recursos.get(5));
        e8.agregarRecurso(recursos.get(8));
        e8.agregarRecurso(recursos.get(9));
        e9.agregarRecurso(recursos.get(2));
        e9.agregarRecurso(recursos.get(4));
        e10.agregarRecurso(recursos.get(5));
        e10.agregarRecurso(recursos.get(8));
        e10.agregarRecurso(recursos.get(9));

        // Asignar asistentes iniciales
        e1.agregarAsistente(p1);
        e1.agregarAsistente(p2);
        e2.agregarAsistente(p3);
        e3.agregarAsistente(p4);
        e4.agregarAsistente(p5);
        e5.agregarAsistente(p6);
        e6.agregarAsistente(p7);
        e7.agregarAsistente(p8);
        e8.agregarAsistente(p9);
        e9.agregarAsistente(p10);

        // Agregar eventos al sistema
        addEvento(e1);
        addEvento(e2);
        addEvento(e3);
        addEvento(e4);
        addEvento(e5);
        addEvento(e6);
        addEvento(e7);
        addEvento(e8);
        addEvento(e9);
        addEvento(e10);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase();
    }

    public void addEvento(Evento e) {
        if (e == null) return;
        eventos.add(e);
        indexPorId.put(normalizarTexto(e.getIdEvento()), e);
    }

    public List<Evento> getEventos() {
        return new ArrayList<>(eventos);
    }

    public void setEventos(ArrayList<Evento> eventos) {
        this.eventos.clear();
        this.eventos.addAll(eventos);
        // Reconstruir el índice para mantener consistencia
        this.indexPorId.clear();
        for (Evento evento : eventos) {
            indexPorId.put(normalizarTexto(evento.getIdEvento()), evento);
        }
    }

    public Map<String, Evento> getIndexPorId() {
        return new HashMap<>(indexPorId);
    }

    public void setIndexPorId(Map<String, Evento> indexPorId) {
        this.indexPorId.clear();
        this.indexPorId.putAll(indexPorId);
    }

    public List<Recurso> getRecursosDisponibles() {
        return new ArrayList<>(recursosDisponibles);
    }

    public void setRecursosDisponibles(List<Recurso> recursosDisponibles) {
        this.recursosDisponibles.clear();
        this.recursosDisponibles.addAll(recursosDisponibles);
    }

    public List<Evento> listarEventosConCupo() {
        ArrayList<Evento> res = new ArrayList<>();
        for (Evento e : eventos) {
            if (e.hayCupos()) res.add(e);
        }
        return res;
    }
    
    public String getNextEventId() {
        int maxId = 0;
        for (Evento evento : eventos) {
            String id = evento.getIdEvento();
            if (id.startsWith("E")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException e) {
                    // Ignorar si no es número
                }
            }
        }
        return "E" + (maxId + 1);
    }

    public List<Evento> buscarPorId(String id) {
        List<Evento> result = new ArrayList<>();
        Evento e = indexPorId.get(normalizarTexto(id));
        if (e != null) result.add(e);
        return result;
    }

    public List<Evento> buscarPorNombre(String nombre) {
        ArrayList<Evento> result = new ArrayList<>();
        String nombreNormalizado = normalizarTexto(nombre);

        for (Evento e : eventos) {
            if (normalizarTexto(e.getNombre()).contains(nombreNormalizado)) {
                result.add(e);
            }
        }
        return result;
    }
    
    public Evento buscarEventoPorId(String id) {
        return indexPorId.get(normalizarTexto(id));
    }
    
    public List<Evento> buscarPorTipo(String tipo) {
        ArrayList<Evento> result = new ArrayList<>();
        String tipoNormalizado = normalizarTexto(tipo);

        for (Evento e : eventos) {
            if (normalizarTexto(e.getTipo()).equals(tipoNormalizado)) {
                result.add(e);
            }
        }
        return result;
    }
    
    public List<String> getTiposEvento() {
        List<String> tipos = new ArrayList<>();
        for (Evento e : eventos) {
            if (!tipos.contains(e.getTipo())) {
                tipos.add(e.getTipo());
            }
        }
        return tipos;
    }
}