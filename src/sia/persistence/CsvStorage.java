package sia.persistence;

import sia.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Persistencia simple en CSV/TXT (UTF-8, separador ';').
 * Carga todo al iniciar y guarda todo al salir.
 * Compatible con JDK 11 y modelo con herencia:
 *  - Persona -> Estudiante / Profesor
 *  - Recurso -> Sala / Equipo
 */
public final class CsvStorage {

    private static final String SEP = ";";

    private CsvStorage() {}

    /* ============================ API PÚBLICA ============================ */

    public static void loadAll(SistemaEventos sistema) throws IOException {
        Path dir = Paths.get("data");

        // Si no existe 'data/' o eventos.csv está vacío/casi vacío, crea ejemplo
        if (!Files.exists(dir)) {
            crearDatosEjemplo();
        } else {
            Path ev = dir.resolve("eventos.csv");
            Path rec = dir.resolve("recursos.csv");
            if (fileIsEmptyOrHeaderOnly(ev) || fileIsEmptyOrHeaderOnly(rec)) {
                crearDatosEjemplo();
            }
        }
        
        Map<String, Persona> personas = loadPersonas(dir);
        Map<String, Recurso> recursos = loadRecursos(dir);
        Map<String, Evento> eventos  = loadEventos(dir);

        // Relaciones: asistentes y reservas (asociación Evento<->Recurso)
        loadAsistentes(dir, eventos, personas);
        loadReservas(dir, eventos, recursos);

        // Volcar al sistema
        sistema.setEventos(new ArrayList<>(eventos.values()));
        sistema.setRecursosDisponibles(new ArrayList<>(recursos.values()));
    }

    public static void saveAll(SistemaEventos sistema) throws IOException {
        Path dir = Paths.get("data");
        ensureDir(dir);

        // Construir catálogos a partir del estado del sistema
        List<Evento> eventos = sistema.getEventos();
        Map<String, Persona> personas = new LinkedHashMap<>();
        Map<String, Recurso> recursos = new LinkedHashMap<>();

        for (Evento e : eventos) {
            for (Persona p : e.getAsistentes()) personas.putIfAbsent(p.getId(), p);
            for (Recurso r : e.getRecursos())   recursos.putIfAbsent(r.getId(), r);
        }
        for (Recurso r : sistema.getRecursosDisponibles()) recursos.putIfAbsent(r.getId(), r);

        saveEventos(dir, eventos);
        savePersonas(dir, personas.values());
        saveRecursos(dir, recursos.values());
        saveAsistentes(dir, eventos);
        saveReservas(dir, eventos);
    }

    /* ============================ HELPERS E/S ============================ */

    private static void ensureDir(Path dir) throws IOException {
        if (!Files.exists(dir)) Files.createDirectories(dir);
    }

    /* ------------------------------- PERSONAS ------------------------------- */

    // Soporta:
    // A) Antiguo: id;nombre;rol
    // B) Nuevo:  tipo;id;nombre;email;carrera;nivel;departamento;categoria
    private static Map<String, Persona> loadPersonas(Path dir) throws IOException {
        Map<String, Persona> map = new LinkedHashMap<>();
        Path f = dir.resolve("personas.csv");
        if (!Files.exists(f)) {
            try (BufferedWriter w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
                w.write("tipo;id;nombre;email;carrera;nivel;departamento;categoria\n");
            }
            return map;
        }

        try (BufferedReader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            String header = r.readLine();
            if (header == null) return map;
            String[] h = split(header);
            Map<String, Integer> idx = indexOf(h);

            boolean nuevo = idx.containsKey("tipo"); // si no existe, asumimos esquema antiguo
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] t = split(line);

                if (!nuevo) {
                    // Antiguo: id;nombre;rol
                    if (t.length < 3) continue;
                    String id = nt(t[0]), nombre = nt(t[1]), rol = nt(t[2]).toLowerCase();
                    if (rol.startsWith("alum")) {
                        map.put(id, new Estudiante(id, nombre, "", "", 0));
                    } else { // "docente"/"profesor"
                        map.put(id, new Profesor(id, nombre, "", "", ""));
                    }
                } else {
                    // Nuevo: tipo;id;nombre;email;carrera;nivel;departamento;categoria
                    String tipo = get(t, idx, "tipo", "Estudiante");
                    String id   = get(t, idx, "id", "");
                    String nombre = get(t, idx, "nombre", "");
                    String email  = get(t, idx, "email", "");

                    if ("Estudiante".equalsIgnoreCase(tipo)) {
                        String carrera = get(t, idx, "carrera", "");
                        int nivel = parseIntSafe(get(t, idx, "nivel", ""), 0);
                        map.put(id, new Estudiante(id, nombre, email, carrera, nivel));
                    } else if ("Profesor".equalsIgnoreCase(tipo)) {
                        String depto = get(t, idx, "departamento", "");
                        String categoria = get(t, idx, "categoria", "");
                        map.put(id, new Profesor(id, nombre, email, depto, categoria));
                    } else {
                        // Desconocido -> default Estudiante
                        map.put(id, new Estudiante(id, nombre, email, "", 0));
                    }
                }
            }
        }
        return map;
    }

    private static void savePersonas(Path dir, Collection<Persona> personas) throws IOException {
        Path f = dir.resolve("personas.csv");
        try (BufferedWriter w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
            w.write("tipo;id;nombre;email;carrera;nivel;departamento;categoria\n");
            for (Persona p : personas) {
                if (p instanceof Estudiante) {
                    Estudiante e = (Estudiante) p;
                    w.write(String.join(SEP,
                            "Estudiante",
                            nz(e.getId()),
                            nz(e.getNombre()),
                            nz(e.getEmail()),
                            nz(e.getCarrera()),
                            Integer.toString(e.getNivel()),
                            "", ""   // columnas de Profesor vacías
                    ));
                } else if (p instanceof Profesor) {
                    Profesor pr = (Profesor) p;
                    w.write(String.join(SEP,
                            "Profesor",
                            nz(pr.getId()),
                            nz(pr.getNombre()),
                            nz(pr.getEmail()),
                            "", "",   // columnas de Estudiante vacías
                            nz(pr.getDepartamento()),
                            nz(pr.getCategoria())
                    ));
                } else {
                    // Fallback genérico
                    w.write(String.join(SEP,
                            nz(p.getTipo()),
                            nz(p.getId()),
                            nz(p.getNombre()),
                            (p instanceof Estudiante) ? ((Estudiante) p).getEmail() :
                            (p instanceof Profesor)   ? ((Profesor) p).getEmail() : "",
                            "", "", "", ""
                    ));
                }
                w.write("\n");
            }
        }
    }

    /* ------------------------------- RECURSOS ------------------------------- */

    // Soporta:
    // tipo;id;nombre;capacidad;ubicacion;tipoEquipo;disponible
    private static Map<String, Recurso> loadRecursos(Path dir) throws IOException {
        Map<String, Recurso> map = new LinkedHashMap<>();
        Path f = dir.resolve("recursos.csv");
        if (!Files.exists(f)) {
            try (BufferedWriter w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
                w.write("tipo;id;nombre;capacidad;ubicacion;tipoEquipo;disponible\n");
            }
            return map;
        }

        try (BufferedReader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            String header = r.readLine();
            if (header == null) return map;
            String[] h = split(header);
            Map<String, Integer> idx = indexOf(h);

            boolean nuevo = idx.containsKey("tipo") && (idx.containsKey("capacidad") || idx.containsKey("tipoequipo"));

            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] t = split(line);

                if (!nuevo) {
                    // Antiguo: id;nombre;tipo;disponible;fechaReservada
                    if (t.length < 3) continue;
                    String id = nt(t[0]), nombre = nt(t[1]), tipo = nt(t[2]);
                    boolean disp = (t.length >= 4) ? !"false".equalsIgnoreCase(nt(t[3])) : true;

                    if ("Sala".equalsIgnoreCase(tipo)) {
                        // Sala sin datos extra en formato antiguo
                        Sala s = new Sala(id, nombre, 0, "");
                        map.put(id, s);
                    } else {
                        // Cualquier otro tipo -> Equipo con tipoEquipo = tipo
                        Equipo e = new Equipo(id, nombre, tipo, disp);
                        map.put(id, e);
                    }
                    // fechaReservada (t[4]) se ignora en el nuevo modelo (la reserva la maneja Evento)
                } else {
                    // Nuevo: tipo;id;nombre;capacidad;ubicacion;tipoEquipo;disponible
                    String tipo = get(t, idx, "tipo", "Sala");
                    String id   = get(t, idx, "id", "");
                    String nombre = get(t, idx, "nombre", "");

                    if ("Sala".equalsIgnoreCase(tipo)) {
                        int cap = parseIntSafe(get(t, idx, "capacidad", ""), 0);
                        String ubic = get(t, idx, "ubicacion", "");
                        map.put(id, new Sala(id, nombre, cap, ubic));
                    } else if ("Equipo".equalsIgnoreCase(tipo)) {
                        String teq = get(t, idx, "tipoequipo", get(t, idx, "tipoEquipo", ""));
                        boolean disp = parseBooleanSafe(get(t, idx, "disponible", "true"), true);
                        map.put(id, new Equipo(id, nombre, teq, disp));
                    } else {
                        // Tipo desconocido -> Equipo genérico
                        map.put(id, new Equipo(id, nombre, tipo, true));
                    }
                }
            }
        }
        return map;
    }

    private static void saveRecursos(Path dir, Collection<Recurso> recursos) throws IOException {
        Path f = dir.resolve("recursos.csv");
        try (BufferedWriter w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
            w.write("tipo;id;nombre;capacidad;ubicacion;tipoEquipo;disponible\n");
            for (Recurso r : recursos) {
                if (r instanceof Sala) {
                    Sala s = (Sala) r;
                    w.write(String.join(SEP,
                            "Sala",
                            nz(s.getId()),
                            nz(s.getNombre()),
                            Integer.toString(s.getCapacidad()),
                            nz(s.getUbicacion()),
                            "", // tipoEquipo
                            "true" // disponible (no aplica a Sala, dejamos true)
                    ));
                } else if (r instanceof Equipo) {
                    Equipo e = (Equipo) r;
                    w.write(String.join(SEP,
                            "Equipo",
                            nz(e.getId()),
                            nz(e.getNombre()),
                            "", "", // capacidad; ubicacion
                            nz(e.getTipoEquipo()),
                            Boolean.toString(e.isDisponible())
                    ));
                } else {
                    // Fallback genérico
                    w.write(String.join(SEP,
                            nz(r.getTipo()),
                            nz(r.getId()),
                            nz(r.getNombre()),
                            "", "", "", "true"
                    ));
                }
                w.write("\n");
            }
        }
    }

    /* ------------------------------- EVENTOS ------------------------------- */

    // Mantiene el formato existente: id;nombre;tipo;fecha;hora;sala;capacidad
    private static Map<String, Evento> loadEventos(Path dir) throws IOException {
        Map<String, Evento> map = new LinkedHashMap<>();
        Path f = dir.resolve("eventos.csv");
        if (!Files.exists(f)) {
            try (BufferedWriter w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
                w.write("id;nombre;tipo;fecha;hora;sala;capacidad\n");
            }
            return map;
        }
        try (BufferedReader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            String line; boolean first = true;
            while ((line = r.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                String[] t = split(line);
                if (t.length < 7) continue;
                int cap = parseIntSafe(t[6], 0);
                Evento e = new Evento(nt(t[0]), nt(t[1]), nt(t[2]), nt(t[3]), nt(t[4]), nt(t[5]), cap);
                map.put(e.getIdEvento(), e);
            }
        }
        return map;
    }

    private static void saveEventos(Path dir, List<Evento> eventos) throws IOException {
        Path f = dir.resolve("eventos.csv");
        try (BufferedWriter w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
            w.write("id;nombre;tipo;fecha;hora;sala;capacidad\n");
            for (Evento e : eventos) {
                w.write(String.join(SEP,
                        nz(e.getIdEvento()),
                        nz(e.getNombre()),
                        nz(e.getTipo()),
                        nz(e.getFecha()),
                        nz(e.getHora()),
                        nz(e.getSala()),
                        Integer.toString(e.getCapacidad())
                ));
                w.write("\n");
            }
        }
    }

    /* -------------------------- RELACIONES (JOIN) -------------------------- */

    private static void loadAsistentes(Path dir, Map<String, Evento> eventos, Map<String, Persona> personas) throws IOException {
        Path f = dir.resolve("asistentes.csv");
        if (!Files.exists(f)) {
            try (BufferedWriter w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
                w.write("idEvento;idPersona\n");
            }
            return;
        }
        try (BufferedReader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            String line; boolean first = true;
            while ((line = r.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                String[] t = split(line);
                if (t.length < 2) continue;
                Evento e = eventos.get(nt(t[0]));
                Persona p = personas.get(nt(t[1]));
                if (e != null && p != null) {
                    boolean ya = false;
                    for (Persona ex : e.getAsistentes()) {
                        if (ex.getId().equalsIgnoreCase(p.getId())) { ya = true; break; }
                    }
                    if (!ya) e.agregarAsistente(p);
                }
            }
        }
    }

    // Mantiene archivo reservas.csv como vínculo Evento<->Recurso; NO llama a reservar() en Recurso
    private static void loadReservas(Path dir, Map<String, Evento> eventos, Map<String, Recurso> recursos) throws IOException {
        Path f = dir.resolve("reservas.csv");
        if (!Files.exists(f)) {
            try (BufferedWriter w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
                w.write("idEvento;idRecurso;fecha;hora\n");
            }
            return;
        }
        try (BufferedReader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            String line; boolean first = true;
            while ((line = r.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                String[] t = split(line);
                if (t.length < 2) continue;
                Evento e = eventos.get(nt(t[0]));
                Recurso rec = recursos.get(nt(t[1]));
                if (e != null && rec != null) {
                    boolean ya = false;
                    for (Recurso ex : e.getRecursos()) {
                        if (ex.getId().equalsIgnoreCase(rec.getId())) { ya = true; break; }
                    }
                    if (!ya) e.agregarRecurso(rec);
                    // fecha/hora quedan registradas en el CSV; no mutamos el recurso
                }
            }
        }
    }

    private static void saveAsistentes(Path dir, List<Evento> eventos) throws IOException {
        Path f = dir.resolve("asistentes.csv");
        try (BufferedWriter w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
            w.write("idEvento;idPersona\n");
            for (Evento e : eventos) {
                for (Persona p : e.getAsistentes()) {
                    w.write(String.join(SEP, nz(e.getIdEvento()), nz(p.getId())));
                    w.write("\n");
                }
            }
        }
    }

    private static void saveReservas(Path dir, List<Evento> eventos) throws IOException {
        Path f = dir.resolve("reservas.csv");
        try (BufferedWriter w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
            w.write("idEvento;idRecurso;fecha;hora\n");
            for (Evento e : eventos) {
                String fecha = nz(e.getFecha());
                String hora  = nz(e.getHora());
                for (Recurso r : e.getRecursos()) {
                    w.write(String.join(SEP, nz(e.getIdEvento()), nz(r.getId()), fecha, hora));
                    w.write("\n");
                }
            }
        }
    }

    /* =============================== UTIL ================================= */

    private static String[] split(String line) {
        // mantiene campos vacíos
        return line.split(SEP, -1);
    }

    private static Map<String,Integer> indexOf(String[] header) {
        Map<String,Integer> idx = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            idx.put(header[i].trim().toLowerCase(), i);
        }
        return idx;
    }

    private static String get(String[] t, Map<String,Integer> idx, String key, String def) {
        Integer i = idx.get(key);
        if (i == null || i < 0 || i >= t.length) return def;
        String v = t[i];
        return (v == null) ? def : v.trim();
    }

    private static String nt(String s) { return (s == null) ? "" : s.trim(); }

    private static String nz(String s) { return (s == null) ? "" : s; }

    private static int parseIntSafe(String s, int def) {
        try { return (s == null || s.trim().isEmpty()) ? def : Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }

    private static boolean parseBooleanSafe(String s, boolean def) {
        if (s == null || s.trim().isEmpty()) return def;
        String x = s.trim().toLowerCase();
        if ("true".equals(x) || "1".equals(x) || "yes".equals(x) || "si".equals(x)) return true;
        if ("false".equals(x) || "0".equals(x) || "no".equals(x)) return false;
        return def;
    }
    
    /**
    * Genera un reporte completo de todos los eventos
    * @param sistema Sistema de eventos
    * @param filename Nombre del archivo de reporte
    */
   public static void exportReport(SistemaEventos sistema, String filename) throws IOException {
       Path dir = Paths.get("reports");
       ensureDir(dir);

       Path file = dir.resolve(filename);
       try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
           // Encabezado del reporte
           writer.write("Reporte de Eventos Universitarios - " + new Date() + "\n\n");
           writer.write("ID;Nombre;Tipo;Fecha;Hora;Sala;Capacidad;Asistentes;Recursos\n");

           // Datos de cada evento
           for (Evento evento : sistema.getEventos()) {
               writer.write(String.join(SEP,
                   nz(evento.getIdEvento()),
                   nz(evento.getNombre()),
                   nz(evento.getTipo()),
                   nz(evento.getFecha()),
                   nz(evento.getHora()),
                   nz(evento.getSala()),
                   Integer.toString(evento.getCapacidad()),
                   Integer.toString(evento.getAsistentes().size()),
                   Integer.toString(evento.getRecursos().size())
               ));
               writer.write("\n");
           }

           writer.write("\n\nTotal de eventos: " + sistema.getEventos().size());
       }
   }
   
   private static void crearDatosEjemplo() throws IOException {
        Path dir = Paths.get("data");
        ensureDir(dir);

        // Crear eventos de ejemplo más variados
        List<String> eventos = Arrays.asList(
            "id;nombre;tipo;fecha;hora;sala;capacidad",
            "E101;Charla de Inteligencia Artificial;charla;2025-09-05;11:00;B-201;50",
            "E102;Taller de Git y GitHub;taller;2025-09-07;16:00;Lab-3;25",
            "E103;Seminario de Visualización de Datos;seminario;2025-09-12;10:00;Auditorio;120",
            "E104;Concierto de Música Clásica;cultural;2025-09-15;19:00;Auditorio;200",
            "E105;Exposición de Arte Contemporáneo;cultural;2025-09-20;15:00;Galería;80",
            "E106;Workshop de Robótica;taller;2025-09-25;14:00;Lab-4;30",
            "E107;Conferencia de Cybersecurity;conferencia;2025-10-02;09:00;A-101;100",
            "E108;Presentación de Proyectos Finales;exposicion;2025-10-10;13:00;Hall Principal;150",
            "E109;Debate sobre Ética en IA;debate;2025-10-15;16:30;Sala de Usos Múltiples;40",
            "E110;Feria de Emprendimiento;feria;2025-10-20;10:00;Patio Central;300",
            "E111;Ceremonia de Graduación;ceremonia;2025-11-05;18:00;Auditorio Principal;500",
            "E112;Maratón de Programación;competencia;2025-11-12;09:00;Lab-5;50",
            "E113;Charla de Innovación Tecnológica;charla;2025-11-18;15:00;B-302;60",
            "E114;Festival de Cine;cultural;2025-11-25;19:00;Sala de Proyecciones;80",
            "E115;Simposio de Investigación;academico;2025-12-02;10:00;Auditorio;200"
        );
        Files.write(dir.resolve("eventos.csv"), eventos, StandardCharsets.UTF_8);

        // Crear personas de ejemplo más diversas
        List<String> personas = Arrays.asList(
            "tipo;id;nombre;email;carrera;nivel;departamento;categoria",
            "Estudiante;A101;Ana Díaz;ana.diaz@universidad.edu;Ingeniería Civil Informática;3;;",
            "Estudiante;A102;Benjamín Soto;benjamin.soto@universidad.edu;Artes;2;;",
            "Estudiante;A103;Carla Pérez;carla.perez@universidad.edu;Medicina;4;;",
            "Profesor;A104;Diego López;diego.lopez@universidad.edu;;;Matemáticas;Titular",
            "Estudiante;A105;Elena Castro;elena.castro@universidad.edu;Derecho;1;;",
            "Profesor;A106;Fernando Martínez;fernando.martinez@universidad.edu;;;Física;Asociado",
            "Estudiante;A107;Gabriela Silva;gabriela.silva@universidad.edu;Economía;2;;",
            "Estudiante;A108;Héctor Rodríguez;hector.rodriguez@universidad.edu;Biología;3;;",
            "Profesor;A109;Isabel González;isabel.gonzalez@universidad.edu;;;Literatura;Titular",
            "Estudiante;A110;Javier Méndez;javier.mendez@universidad.edu;Psicología;2;;",
            "Estudiante;A111;Laura Ramírez;laura.ramirez@universidad.edu;Ingeniería Civil;3;;",
            "Estudiante;A112;Miguel Ángel Reyes;miguel.reyes@universidad.edu;Arquitectura;4;;",
            "Profesor;A113;Natalia Vargas;natalia.vargas@universidad.edu;;;Química;Asistente",
            "Estudiante;A114;Óscar Torres;oscar.torres@universidad.edu;Administración;2;;",
            "Profesor;A115;Patricia Ruiz;patricia.ruiz@universidad.edu;;;Historia;Titular"
        );
        Files.write(dir.resolve("personas.csv"), personas, StandardCharsets.UTF_8);

        // Crear recursos de ejemplo
        List<String> recursos = Arrays.asList(
            "tipo;id;nombre;capacidad;ubicacion;tipoEquipo;disponible",
            "Sala;S001;Auditorio Principal;500;Edificio A;;true",
            "Sala;S002;Sala de Conferencias;100;Edificio B;;true",
            "Sala;S003;Laboratorio de Computación;30;Edificio C;;true",
            "Sala;S004;Aula Magna;200;Edificio D;;true",
            "Sala;S005;Sala de Usos Múltiples;50;Edificio E;;true",
            "Equipo;E001;Proyector 4K;;;;Audiovisual;true",
            "Equipo;E002;Micrófono Inalámbrico;;;;Audio;true",
            "Equipo;E003;Notebook i7;;;;Tecnología;true",
            "Equipo;E004;Pizarra Interactiva;;;;Mobiliario;true",
            "Equipo;E005;Sistema de Sonido;;;;Audio;true",
            "Equipo;E006;Tabletas Gráficas;;;;Tecnología;true",
            "Equipo;E007;Cámaras de Video;;;;Audiovisual;true",
            "Equipo;E008;Pantalla LED;;;;Audiovisual;true"
        );
        Files.write(dir.resolve("recursos.csv"), recursos, StandardCharsets.UTF_8);

        // Crear archivos de relaciones con asignaciones más realistas
        List<String> asistentes = Arrays.asList(
            "idEvento;idPersona",
            "E101;A101",
            "E101;A102",
            "E101;A103",
            "E102;A104",
            "E102;A105",
            "E103;A106",
            "E103;A107",
            "E104;A108",
            "E104;A109",
            "E105;A110",
            "E105;A111",
            "E106;A112",
            "E106;A113",
            "E107;A114",
            "E107;A115",
            "E108;A101",
            "E108;A104",
            "E109;A107",
            "E109;A110",
            "E110;A112",
            "E110;A115",
            "E111;A101",
            "E111;A102",
            "E111;A103",
            "E111;A104",
            "E111;A105",
            "E112;A106",
            "E112;A107",
            "E112;A108",
            "E113;A109",
            "E113;A110",
            "E114;A111",
            "E114;A112",
            "E115;A113",
            "E115;A114",
            "E115;A115"
        );
        Files.write(dir.resolve("asistentes.csv"), asistentes, StandardCharsets.UTF_8);

        List<String> reservas = Arrays.asList(
            "idEvento;idRecurso;fecha;hora",
            "E101;S003;2025-09-05;11:00",
            "E101;E001;2025-09-05;11:00",
            "E101;E003;2025-09-05;11:00",
            "E102;S004;2025-09-07;16:00",
            "E102;E007;2025-09-07;16:00",
            "E102;E009;2025-09-07;16:00",
            "E103;S001;2025-09-12;10:00",
            "E103;E001;2025-09-12;10:00",
            "E103;E005;2025-09-12;10:00",
            "E104;S001;2025-09-15;19:00",
            "E104;E005;2025-09-15;19:00",
            "E104;E013;2025-09-15;19:00",
            "E105;S007;2025-09-20;15:00",
            "E105;E013;2025-09-20;15:00",
            "E106;S005;2025-09-25;14:00",
            "E106;E007;2025-09-25;14:00",
            "E106;E012;2025-09-25;14:00",
            "E107;S002;2025-10-02;09:00",
            "E107;E001;2025-10-02;09:00",
            "E107;E003;2025-10-02;09:00",
            "E108;S008;2025-10-10;13:00",
            "E108;E005;2025-10-10;13:00",
            "E108;E013;2025-10-10;13:00",
            "E109;S006;2025-10-15;16:30",
            "E109;E003;2025-10-15;16:30",
            "E109;E009;2025-10-15;16:30",
            "E110;S008;2025-10-20;10:00",
            "E110;E005;2025-10-20;10:00",
            "E110;E013;2025-10-20;10:00",
            "E111;S001;2025-11-05;18:00",
            "E111;E001;2025-11-05;18:00",
            "E111;E005;2025-11-05;18:00",
            "E111;E013;2025-11-05;18:00",
            "E112;S004;2025-11-12;09:00",
            "E112;E007;2025-11-12;09:00",
            "E113;S003;2025-11-18;15:00",
            "E113;E001;2025-11-18;15:00",
            "E113;E003;2025-11-18;15:00",
            "E114;S009;2025-11-25;19:00",
            "E114;E001;2025-11-25;19:00",
            "E114;E005;2025-11-25;19:00",
            "E115;S001;2025-12-02;10:00",
            "E115;E001;2025-12-02;10:00",
            "E115;E003;2025-12-02;10:00"
        );
        Files.write(dir.resolve("reservas.csv"), reservas, StandardCharsets.UTF_8);
    }
   
   // Devuelve true si el archivo no existe, está vacío o sólo tiene encabezado.
    private static boolean fileIsEmptyOrHeaderOnly(java.nio.file.Path f) throws java.io.IOException {
        if (!java.nio.file.Files.exists(f)) return true;
        try (java.io.BufferedReader r = java.nio.file.Files.newBufferedReader(f, java.nio.charset.StandardCharsets.UTF_8)) {
            String first = r.readLine(); // header
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.trim().isEmpty()) return false; // hay datos reales
            }
            return true; // vacío o sólo encabezado
        }
    }
   
}