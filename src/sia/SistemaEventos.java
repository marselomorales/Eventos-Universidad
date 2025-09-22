package sia;

import java.util.*;

public class SistemaEventos {

    private final Map<String, Evento> indexPorId = new HashMap<>();
    private List<Evento> eventos = new ArrayList<>();
    private List<Recurso> recursosDisponibles = new ArrayList<>();

    /* ======== Acceso usado por CsvStorage y la GUI ======== */
    public List<Evento> getEventos() { return eventos; }
    public void setEventos(List<Evento> evs) {
        this.eventos = (evs != null) ? evs : new ArrayList<>();
        rebuildIndex();
    }

    public List<Recurso> getRecursosDisponibles() { return recursosDisponibles; }
    public void setRecursosDisponibles(List<Recurso> recs) {
        this.recursosDisponibles = (recs != null) ? recs : new ArrayList<>();
    }

    public Evento buscarEvento(String idEvento) {
        return indexPorId.get(idEvento);
    }

    public void agregarEvento(Evento e) {
        if (e == null || e.getIdEvento() == null) return;
        // si ya existe, reemplaza
        eliminarEvento(e.getIdEvento());
        eventos.add(e);
        indexPorId.put(e.getIdEvento(), e);
    }

    public boolean eliminarEvento(String idEvento) {
        if (idEvento == null) return false;
        Evento prev = indexPorId.remove(idEvento);
        if (prev == null) return false;
        return eventos.removeIf(x -> idEvento.equals(x.getIdEvento()));
    }

    public boolean modificarEvento(Evento nuevo) {
        if (nuevo == null || nuevo.getIdEvento() == null) return false;
        String id = nuevo.getIdEvento();
        for (int i = 0; i < eventos.size(); i++) {
            if (id.equals(eventos.get(i).getIdEvento())) {
                eventos.set(i, nuevo);
                indexPorId.put(id, nuevo);
                return true;
            }
        }
        return false;
    }

    /* ======== Utilidades ======== */
    private void rebuildIndex() {
        indexPorId.clear();
        for (Evento e : eventos) {
            if (e.getIdEvento() != null) indexPorId.put(e.getIdEvento(), e);
        }
    }
    
    // === NEGOCIO (excepciones propias y utilidades) ===
    public List<Evento> filtrarEventos(String q) {
        if (q == null || q.trim().isEmpty()) return new ArrayList<>(eventos);
        String x = q.trim().toLowerCase(Locale.ROOT);
        List<Evento> out = new ArrayList<>();
        for (Evento e : eventos) {
            if ((e.getIdEvento() != null && e.getIdEvento().toLowerCase().contains(x))
             || (e.getNombre()   != null && e.getNombre().toLowerCase().contains(x))
             || (e.getTipo()     != null && e.getTipo().toLowerCase().contains(x))
             || (e.getFecha()    != null && e.getFecha().toLowerCase().contains(x))
             || (e.getHora()     != null && e.getHora().toLowerCase().contains(x))
            ) out.add(e);
        }
        return out;
    }

    /** Agrega un asistente respetando la capacidad. Lanza CapacidadLlenaException. */
    public void agregarAsistenteOrThrow(Evento e, Persona p) throws sia.exceptions.CapacidadLlenaException {
        if (e == null || p == null) return;
        if (e.getCapacidad() > 0 && e.getAsistentes().size() >= e.getCapacidad()) {
            throw new sia.exceptions.CapacidadLlenaException(
                "La capacidad del evento (" + e.getCapacidad() + ") ya está completa."
            );
        }
        // evita duplicados por id
        for (Persona ex : e.getAsistentes()) if (Objects.equals(ex.getId(), p.getId())) return;
        e.getAsistentes().add(p);
    }

    /** Asocia un recurso a un evento evitando doble reserva en misma fecha/hora. */
    public void asociarRecursoOrThrow(Evento e, Recurso r) throws sia.exceptions.RecursoOcupadoException {
        if (e == null || r == null) return;
        String f = e.getFecha() == null ? "" : e.getFecha().trim();
        String h = e.getHora()  == null ? "" : e.getHora().trim();

        // ¿ya está en el evento?
        for (Recurso x : e.getRecursos()) if (Objects.equals(x.getId(), r.getId())) return;

        // Conflicto: mismo recurso reservado en otro evento con misma fecha/hora
        for (Evento otro : eventos) {
            if (otro == e) continue;
            boolean mismaFecha = Objects.equals(f, (otro.getFecha()==null?"":otro.getFecha().trim()));
            boolean mismaHora  = Objects.equals(h, (otro.getHora()==null ?"":otro.getHora().trim()));
            if (mismaFecha && mismaHora) {
                for (Recurso rx : otro.getRecursos()) {
                    if (Objects.equals(rx.getId(), r.getId())) {
                        throw new sia.exceptions.RecursoOcupadoException(
                            "El recurso " + r.getId() + " ya está asociado al evento " +
                            otro.getIdEvento() + " en " + f + " " + h + "."
                        );
                    }
                }
            }
        }
        e.getRecursos().add(r);
    }

    /** Quita un recurso del evento por id. */
    public boolean desasociarRecurso(Evento e, String idRecurso) {
        if (e == null || idRecurso == null) return false;
        return e.getRecursos().removeIf(x -> Objects.equals(x.getId(), idRecurso));
    }
    
    // ========== MÉTODOS NUEVOS PARA GESTIÓN DE RECURSOS ==========
    private static final Map<String, List<String>> SUGERENCIAS_POR_TIPO = new HashMap<String, List<String>>() {{
        put("Charla", Arrays.asList("Proyector", "Sala"));
        put("Seminario", Arrays.asList("Proyector", "Sala"));
        put("Taller", Arrays.asList("Notebook", "Sala"));
        put("Laboratorio", Arrays.asList("Notebook", "Sala"));
        put("Evaluación", Arrays.asList("Sala"));
        put("Reunión", Arrays.asList("Sala"));
        put("Ceremonia", Arrays.asList("Sala", "Amplificación"));
        put("Defensa", Arrays.asList("Proyector", "Sala"));
    }};

    public boolean isRecursoDisponible(Recurso recurso, String fecha, String hora) {
        for (Evento e : eventos) {
            if (e.getRecursos().contains(recurso) && 
                Objects.equals(e.getFecha(), fecha) && 
                Objects.equals(e.getHora(), hora)) {
                return false;
            }
        }
        return true;
    }

    public List<Recurso> listarRecursosOrdenadosPara(Evento evento, String filtroTexto, String estadoFiltro) {
        List<Recurso> recursosFiltrados = new ArrayList<>();
        
        for (Recurso r : recursosDisponibles) {
            if (filterRecurso(r, filtroTexto, estadoFiltro, evento)) {
                recursosFiltrados.add(r);
            }
        }
        
        // Ordenar
        Collections.sort(recursosFiltrados, new Comparator<Recurso>() {
            @Override
            public int compare(Recurso r1, Recurso r2) {
                // Disponibles primero
                boolean disp1 = isRecursoDisponible(r1, evento.getFecha(), evento.getHora());
                boolean disp2 = isRecursoDisponible(r2, evento.getFecha(), evento.getHora());
                
                if (disp1 != disp2) {
                    return disp1 ? -1 : 1;
                }
                
                // Luego por score
                int score1 = calculateScore(r1, evento);
                int score2 = calculateScore(r2, evento);
                
                if (score1 != score2) {
                    return Integer.compare(score2, score1); // Mayor score primero
                }
                
                // Finalmente por nombre
                return r1.getNombre().compareTo(r2.getNombre());
            }
        });
        
        return recursosFiltrados;
    }

    private boolean filterRecurso(Recurso r, String filterText, String estado, Evento evento) {
        // Filtro de texto
        if (filterText != null && !filterText.isEmpty()) {
            String lowerFilter = filterText.toLowerCase();
            if (!(r.getNombre().toLowerCase().contains(lowerFilter) ||
                 (r instanceof Sala && ((Sala) r).getUbicacion().toLowerCase().contains(lowerFilter)) ||
                 (r instanceof Equipo && ((Equipo) r).getTipoEquipo().toLowerCase().contains(lowerFilter)))) {
                return false;
            }
        }

        // Filtro de disponibilidad
        boolean disponible = isRecursoDisponible(r, evento.getFecha(), evento.getHora());
        if ("Disponibles".equals(estado) && !disponible) return false;
        if ("Ocupados".equals(estado) && disponible) return false;
        
        return true;
    }

    private int calculateScore(Recurso r, Evento evento) {
        int score = 0;
        String tipoEvento = evento.getTipo();
        List<String> sugerencias = SUGERENCIAS_POR_TIPO.getOrDefault(tipoEvento, Arrays.asList());

        if (r instanceof Sala) {
            score += 2;
            Sala s = (Sala) r;
            if (s.getCapacidad() >= evento.getCapacidad()) {
                score += 2;
            }
        } else if (r instanceof Equipo) {
            Equipo e = (Equipo) r;
            if (sugerencias.contains(e.getTipoEquipo())) {
                score += 3;
            }
        }
        return score;
    }
    
    public List<Persona> listarPersonasCatalogo(String filtroTexto, String rolFiltro) {
        List<Persona> todasPersonas = new ArrayList<>();

        // Recopilar todas las personas de los eventos y recursos disponibles
        for (Evento e : eventos) {
            todasPersonas.addAll(e.getAsistentes());
        }

        // Filtrar por texto
        List<Persona> resultado = new ArrayList<>();
        for (Persona p : todasPersonas) {
            if (filtroTexto == null || filtroTexto.isEmpty() || 
                p.getNombre().toLowerCase().contains(filtroTexto.toLowerCase()) ||
                p.getEmail().toLowerCase().contains(filtroTexto.toLowerCase())) {

                // Filtrar por rol
                if ("TODOS".equals(rolFiltro) || 
                    ("ESTUDIANTES".equals(rolFiltro) && p instanceof Estudiante) ||
                    ("PROFESORES".equals(rolFiltro) && p instanceof Profesor)) {
                    resultado.add(p);
                }
            }
        }

        return resultado;
    }

    public boolean yaInscrito(Evento e, Persona p) {
        for (Persona asistente : e.getAsistentes()) {
            if (asistente.getId().equals(p.getId())) {
                return true;
            }
        }
        return false;
    }

    public int contarInscritos(Evento e) {
        return e.getAsistentes().size();
    }

    public boolean puedeInscribir(Evento e) {
        return e.getCapacidad() <= 0 || contarInscritos(e) < e.getCapacidad();
    }
    
}