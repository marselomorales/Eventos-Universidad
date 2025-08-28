package sia;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;


public class App {
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    
    private static final SistemaEventos sistema = new SistemaEventos();
    private static final Scanner sc = new Scanner(System.in);

    private static void banner() {
        System.out.println(CYAN + "==========================================");
        System.out.println("   ORGANIZADOR EVENTOS UNIVERSITARIO");
        System.out.println("==========================================" + RESET);
    }

    private static void menuPrincipal() {
        System.out.println("\n" + BOLD + "MENÚ PRINCIPAL" + RESET);
        System.out.println(GREEN + "[1]" + RESET + " Gestión de Eventos");
        System.out.println(GREEN + "[2]" + RESET + " Gestión de Asistentes");
        System.out.println(GREEN + "[3]" + RESET + " Gestión de Recursos");
        System.out.println(GREEN + "[4]" + RESET + " Búsquedas y Consultas");
        System.out.println(MAGENTA + "[0]" + RESET + " Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static void menuEventos() {
        System.out.println("\n" + BOLD + "GESTIÓN DE EVENTOS" + RESET);
        System.out.println(GREEN + "[1]" + RESET + " Ver todos los eventos");
        System.out.println(GREEN + "[2]" + RESET + " Ver eventos con cupo disponible");
        System.out.println(GREEN + "[3]" + RESET + " Ver eventos sin cupo");
        System.out.println(GREEN + "[4]" + RESET + " Agregar nuevo evento");
        System.out.println(YELLOW + "[9]" + RESET + " Volver al menú principal");
        System.out.print("Seleccione una opción: ");
    }

    private static void menuAsistentes() {
        System.out.println("\n" + BOLD + "GESTIÓN DE ASISTENTES" + RESET);
        System.out.println(GREEN + "[1]" + RESET + " Agregar asistente a evento");
        System.out.println(GREEN + "[2]" + RESET + " Mostrar asistentes de un evento");
        System.out.println(YELLOW + "[9]" + RESET + " Volver al menú principal");
        System.out.print("Seleccione una opción: ");
    }

    private static void menuRecursos() {
        System.out.println("\n" + BOLD + "GESTIÓN DE RECURSOS" + RESET);
        System.out.println(GREEN + "[1]" + RESET + " Reservar recurso para evento");
        System.out.println(GREEN + "[2]" + RESET + " Ver recursos disponibles");
        System.out.println(YELLOW + "[9]" + RESET + " Volver al menú principal");
        System.out.print("Seleccione una opción: ");
    }

    private static void menuBusquedas() {
        System.out.println("\n" + BOLD + "BÚSQUEDAS Y CONSULTAS" + RESET);
        System.out.println(GREEN + "[1]" + RESET + " Buscar evento por ID");
        System.out.println(GREEN + "[2]" + RESET + " Buscar evento por nombre");
        System.out.println(GREEN + "[3]" + RESET + " Buscar evento por tipo");
        System.out.println(YELLOW + "[9]" + RESET + " Volver al menú principal");
        System.out.print("Seleccione una opción: ");
    }

    public static void main(String[] args) {
        sistema.seed();
        banner();
        
        while (true) {
            menuPrincipal();
            String op = sc.nextLine().trim();
            
            switch (op) {
                case "1": menuGestionEventos(); break;
                case "2": menuGestionAsistentes(); break;
                case "3": menuGestionRecursos(); break;
                case "4": menuBusquedasConsultas(); break;
                case "0": 
                    System.out.println("¡Gracias por usar SIA-Eventos!");
                    return;
                default:
                    System.out.println(RED + "Opción inválida. Intente nuevamente." + RESET);
            }
        }
    }

    private static void menuGestionEventos() {
        while (true) {
            menuEventos();
            String op = sc.nextLine().trim();
            
            switch (op) {
                case "1":
                    System.out.println(BLUE + "\n--- TODOS LOS EVENTOS ---" + RESET);
                    imprimirTablaEventosCompleta(sistema.getEventos());
                    break;
                case "2":
                    System.out.println(BLUE + "\n--- EVENTOS CON CUPO DISPONIBLE ---" + RESET);
                    imprimirTablaEventosCompleta(sistema.listarEventosConCupo());
                    break;
                case "3":
                    System.out.println(BLUE + "\n--- EVENTOS SIN CUPO ---" + RESET);
                    List<Evento> sinCupo = new ArrayList<>();
                    for (Evento e : sistema.getEventos()) {
                        if (!e.hayCupos()) sinCupo.add(e);
                    }
                    imprimirTablaEventosCompleta(sinCupo);
                    break;
                case "4":
                    crearEventoInteractivo();
                    break;
                case "9":
                    return;
                default:
                    System.out.println(RED + "Opción inválida." + RESET);
            }
        }
    }

    private static void menuGestionAsistentes() {
        while (true) {
            menuAsistentes();
            String op = sc.nextLine().trim();
            
            switch (op) {
                case "1":
                    agregarAsistenteAEvento();
                    break;
                case "2":
                    mostrarAsistentesDeEvento();
                    break;
                case "9":
                    return;
                default:
                    System.out.println(RED + "Opción inválida." + RESET);
            }
        }
    }

    private static void menuGestionRecursos() {
        while (true) {
            menuRecursos();
            String op = sc.nextLine().trim();
            
            switch (op) {
                case "1":
                    reservarRecursoParaEvento();
                    break;
                case "2":
                    System.out.println(BLUE + "\n--- RECURSOS DISPONIBLES ---" + RESET);
                    List<Recurso> recursos = sistema.getRecursosDisponibles();
                    for (int i = 0; i < recursos.size(); i++) {
                        Recurso r = recursos.get(i);
                        System.out.println(GREEN + "[" + (i+1) + "]" + RESET + " " + r);
                    }
                    break;
                case "9":
                    return;
                default:
                    System.out.println(RED + "Opción inválida." + RESET);
            }
        }
    }

    private static void menuBusquedasConsultas() {
        while (true) {
            menuBusquedas();
            String op = sc.nextLine().trim();
            
            switch (op) {
                case "1":
                    System.out.print("Ingrese ID del evento: ");
                    String id = sc.nextLine().trim();
                    List<Evento> resultados = sistema.buscarPorId(id);
                    mostrarResultadosBusqueda(resultados);
                    break;
                case "2":
                    System.out.print("Ingrese nombre del evento: ");
                    String nombre = sc.nextLine().trim();
                    resultados = sistema.buscarPorNombre(nombre);
                    mostrarResultadosBusqueda(resultados);
                    break;
                case "3":
                    buscarEventosPorTipoInteractivo();
                    break;
                case "9":
                    return;
                default:
                    System.out.println(RED + "Opción inválida." + RESET);
            }
        }
    }

    private static void agregarAsistenteAEvento() {
        System.out.println(BLUE + "\n--- AGREGAR ASISTENTE ---" + RESET);
        
        List<Evento> eventosConCupo = sistema.listarEventosConCupo();
        if (eventosConCupo.isEmpty()) {
            System.out.println(RED + "No hay eventos con cupos disponibles." + RESET);
            return;
        }
        
        Evento evento = seleccionarEventoInteractivo(eventosConCupo);
        if (evento == null) return;
        
        System.out.print("Ingrese nombre del asistente: ");
        String nombre = sc.nextLine().trim();
        System.out.print("Ingrese rol (docente/estudiante): ");
        String rol = sc.nextLine().trim();
        
        if (evento.agregarAsistente(nombre, rol)) {
            System.out.println(GREEN + "✓ Asistente agregado correctamente." + RESET);
        } else {
            System.out.println(RED + "✗ Error: No se pudo agregar el asistente." + RESET);
        }
    }

    private static void mostrarAsistentesDeEvento() {
        System.out.println(BLUE + "\n--- MOSTRAR ASISTENTES ---" + RESET);
        
        Evento evento = seleccionarEventoInteractivo(sistema.getEventos());
        if (evento == null) return;
        
        List<Persona> asistentes = evento.getAsistentes();
        if (asistentes.isEmpty()) {
            System.out.println("Este evento no tiene asistentes registrados.");
            return;
        }
        
        System.out.println("\nAsistentes al evento '" + evento.getNombre() + "':");
        System.out.println("Total: " + asistentes.size() + " / " + evento.getCapacidad());
        System.out.println("----------------------------------------");
        
        for (Persona p : asistentes) {
            System.out.printf("- %s (%s)\n", p.getNombre(), p.getRol());
        }
    }

    private static void reservarRecursoParaEvento() {
        System.out.println(BLUE + "\n--- RESERVAR RECURSO ---" + RESET);
        
        Evento evento = seleccionarEventoInteractivo(sistema.getEventos());
        if (evento == null) return;
        
        List<Recurso> recursos = sistema.getRecursosDisponibles();
        if (recursos.isEmpty()) {
            System.out.println("No hay recursos disponibles para reservar.");
            return;
        }
        
        System.out.println("\nRecursos disponibles:");
        for (int i = 0; i < recursos.size(); i++) {
            Recurso r = recursos.get(i);
            System.out.println(GREEN + "[" + (i+1) + "]" + RESET + " " + r);
        }
        
        System.out.print("Seleccione recurso por número: ");
        try {
            int seleccion = Integer.parseInt(sc.nextLine().trim());
            if (seleccion < 1 || seleccion > recursos.size()) {
                System.out.println(RED + "Selección inválida." + RESET);
                return;
            }
            
            Recurso recurso = recursos.get(seleccion - 1);
            System.out.print("Ingrese fecha de reserva (AAAA-MM-DD): ");
            String fecha = sc.nextLine().trim();
            
            System.out.print("¿Reservar hora específica? (s/n): ");
            String conHora = sc.nextLine().trim().toLowerCase();
            
            boolean exito;
            if (conHora.equals("s")) {
                System.out.print("Ingrese hora (HH:MM): ");
                String hora = sc.nextLine().trim();
                exito = evento.reservarRecurso(recurso.getIdRecurso(), fecha, hora);
            } else {
                exito = evento.reservarRecurso(recurso.getIdRecurso(), fecha);
            }
            
            if (exito) {
                System.out.println(GREEN + "✓ Recurso reservado correctamente." + RESET);
            } else {
                System.out.println(RED + "✗ No se pudo reservar el recurso." + RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(RED + "Selección inválida." + RESET);
        }
    }

    private static void buscarEventosPorTipoInteractivo() {
        System.out.println(BLUE + "\n--- BUSCAR POR TIPO DE EVENTO ---" + RESET);
        
        List<String> tipos = sistema.getTiposEvento();
        System.out.println("Tipos de eventos disponibles:");
        for (int i = 0; i < tipos.size(); i++) {
            System.out.println(GREEN + "[" + (i+1) + "]" + RESET + " " + tipos.get(i));
        }
        
        System.out.print("Seleccione tipo por número o ingrese un tipo personalizado: ");
        String input = sc.nextLine().trim();
        
        List<Evento> resultados;
        try {
            int seleccion = Integer.parseInt(input);
            if (seleccion < 1 || seleccion > tipos.size()) {
                System.out.println(RED + "Selección inválida." + RESET);
                return;
            }
            resultados = sistema.buscarPorTipo(tipos.get(seleccion - 1));
        } catch (NumberFormatException e) {
            resultados = sistema.buscarPorTipo(input);
        }
        
        mostrarResultadosBusqueda(resultados);
    }

    private static void mostrarResultadosBusqueda(List<Evento> resultados) {
        if (resultados.isEmpty()) {
            System.out.println("No se encontraron eventos.");
            return;
        }
        
        System.out.println(BLUE + "\n--- RESULTADOS DE BÚSQUEDA ---" + RESET);
        imprimirTablaEventosCompleta(resultados);
    }

    private static Evento seleccionarEventoInteractivo(List<Evento> eventos) {
        if (eventos.isEmpty()) {
            System.out.println("No hay eventos disponibles.");
            return null;
        }
        
        System.out.println("\nSeleccione un evento:");
        for (int i = 0; i < eventos.size(); i++) {
            Evento e = eventos.get(i);
            String estado = e.hayCupos() ? 
                GREEN + "Cupos: " + e.getCuposRestantes() + RESET : 
                RED + "SIN CUPO" + RESET;
            System.out.println(GREEN + "[" + (i+1) + "]" + RESET + " " + 
                e.getNombre() + " (" + e.getTipo() + ") - " + e.getFecha() + " - " + estado);
        }
        
        System.out.print("\nIngrese número del evento o 0 para cancelar: ");
        try {
            int seleccion = Integer.parseInt(sc.nextLine().trim());
            if (seleccion == 0) return null;
            if (seleccion > 0 && seleccion <= eventos.size()) {
                return eventos.get(seleccion - 1);
            }
        } catch (NumberFormatException e) {
            // Continuar con ingreso manual de ID
        }
        
        System.out.print("Ingrese ID del evento manualmente: ");
        String id = sc.nextLine().trim();
        return sistema.buscarEventoPorId(id);
    }

    private static void imprimirTablaEventosCompleta(List<Evento> eventos) {
        if (eventos.isEmpty()) {
            System.out.println("No hay eventos para mostrar.");
            return;
        }
        
        System.out.printf("%-6s | %-30s | %-12s | %-10s | %-8s | %-10s%n",
                "ID", "Nombre", "Tipo", "Fecha", "Cupos", "Estado");
        System.out.println("--------------------------------------------------------------------------------------------");
        
        for (Evento e : eventos) {
            String estado = e.hayCupos() ? 
                GREEN + "Disponible" + RESET : 
                RED + "SIN CUPO" + RESET;
            System.out.printf("%-6s | %-30s | %-12s | %-10s | %-8s | %-10s%n",
                    e.getIdEvento(),
                    e.getNombre(),
                    e.getTipo(),
                    e.getFecha(),
                    e.getCuposRestantes() + "/" + e.getCapacidad(),
                    estado);
        }
    }
    
    private static void crearEventoInteractivo() {
        System.out.println(BLUE + "\n--- CREAR NUEVO EVENTO ---" + RESET);

        // Nombre
        System.out.print("Nombre del evento: ");
        String nombre = sc.nextLine().trim();
        if (nombre.isEmpty()) {
            System.out.println(RED + "El nombre no puede estar vacío." + RESET);
            return;
        }

        // Tipo con selección numérica
        List<String> tipos = Arrays.asList("charla", "taller", "seminario", "cultural", "conferencia", "exposicion", "debate", "feria");
        System.out.println("\nSeleccione el tipo de evento:");
        for (int i = 0; i < tipos.size(); i++) {
            System.out.println(GREEN + "[" + (i+1) + "]" + RESET + " " + tipos.get(i));
        }
        System.out.print("Ingrese el número del tipo o escriba uno personalizado: ");

        String tipo;
        String inputTipo = sc.nextLine().trim();

        try {
            int opcion = Integer.parseInt(inputTipo);
            if (opcion >= 1 && opcion <= tipos.size()) {
                tipo = tipos.get(opcion - 1);
            } else {
                tipo = inputTipo.toLowerCase();
            }
        } catch (NumberFormatException e) {
            tipo = inputTipo.toLowerCase();
        }

        if (tipo.isEmpty()) {
            System.out.println(RED + "El tipo no puede estar vacío." + RESET);
            return;
        }

        // Fecha con validación básica
        String fecha;
        while (true) {
            System.out.print("Fecha (YYYY-MM-DD): ");
            fecha = sc.nextLine().trim();
            if (fecha.matches("\\d{4}-\\d{2}-\\d{2}")) break;
            System.out.println(RED + "Formato de fecha inválido. Use YYYY-MM-DD." + RESET);
        }

        // Hora con validación básica
        String hora;
        while (true) {
            System.out.print("Hora (HH:MM) [opcional, presione Enter para omitir]: ");
            hora = sc.nextLine().trim();
            if (hora.isEmpty()) {
                hora = "00:00";
                break;
            }
            if (hora.matches("\\d{2}:\\d{2}")) break;
            System.out.println(RED + "Formato de hora inválido. Use HH:MM." + RESET);
        }

        // Sala
        System.out.print("Sala [opcional, presione Enter para omitir]: ");
        String sala = sc.nextLine().trim();
        if (sala.isEmpty()) sala = "Por definir";

        // Capacidad con validación
        int capacidad;
        while (true) {
            System.out.print("Capacidad: ");
            try {
                capacidad = Integer.parseInt(sc.nextLine().trim());
                if (capacidad > 0) break;
                System.out.println(RED + "La capacidad debe ser mayor a 0." + RESET);
            } catch (NumberFormatException e) {
                System.out.println(RED + "Por favor ingrese un número válido." + RESET);
            }
        }

        // Confirmación
        System.out.println("\n" + CYAN + "Resumen del evento:" + RESET);
        System.out.println("Nombre: " + nombre);
        System.out.println("Tipo: " + tipo);
        System.out.println("Fecha: " + fecha);
        System.out.println("Hora: " + hora);
        System.out.println("Sala: " + sala);
        System.out.println("Capacidad: " + capacidad);

        System.out.print("\n¿Confirmar creación? (s/n): ");
        String confirmacion = sc.nextLine().trim().toLowerCase();

        if (confirmacion.equals("s")) {
            String nuevoId = sistema.getNextEventId();
            Evento nuevo = new Evento(nuevoId, nombre, tipo, fecha, hora, sala, capacidad);
            sistema.addEvento(nuevo);
            System.out.println(GREEN + "✓ Evento creado exitosamente con ID: " + nuevoId + RESET);
        } else {
            System.out.println(YELLOW + "Creación de evento cancelada." + RESET);
        }
    }

}