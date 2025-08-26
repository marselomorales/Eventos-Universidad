package sia;

import java.util.List;
import java.util.Scanner;

public class App {
    // Estilos para consola (fondo oscuro y texto claro)
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String CYAN  = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String WHITE = "\u001B[97m";  // Blanco brillante
    private static final String BG_BLACK = "\u001B[40m"; // Fondo negro
    private static final String FG_WHITE = "\u001B[37m"; // Texto blanco

    private static final String HEADER = BG_BLACK + WHITE; // Para los títulos y encabezados
    private static final String RESET_STYLE = RESET;  // Reset para todo el texto posterior

    private static final SistemaEventos sistema = new SistemaEventos();

    private static void banner() {
        String title = "S I A  —  Sistema de Eventos";
        System.out.println(HEADER + "###########################################" + RESET_STYLE);
        System.out.println(HEADER + "# " + BOLD + WHITE + title + RESET_STYLE +
                HEADER + " ".repeat(40 - title.length()) + "#" + RESET_STYLE);
        System.out.println(HEADER + "###########################################" + RESET_STYLE);
    }

    private static void menu() {
        System.out.println(GREEN + "[1]" + RESET_STYLE + " Agregar asistente a evento");
        System.out.println(GREEN + "[2]" + RESET_STYLE + " Mostrar asistentes de un evento");
        System.out.println(GREEN + "[3]" + RESET_STYLE + " Buscar evento por ID/nombre");
        System.out.println(GREEN + "[4]" + RESET_STYLE + " Reservar recurso");
        System.out.println(GREEN + "[5]" + RESET_STYLE + " Ver eventos disponibles (con cupos)");
        System.out.println(CYAN + "[0] Salir" + RESET_STYLE);
        System.out.print("> ");
    }

    private static void imprimirTablaEventos(List<Evento> lista) {
        if (lista == null || lista.isEmpty()) {
            System.out.println("No hay eventos para mostrar.");
            return;
        }
        String sep = "#".repeat(78);
        System.out.println(CYAN + sep + RESET_STYLE);
        System.out.printf(BOLD + "%-7s %-26s %-10s %-6s %-12s %8s %8s%n" + RESET_STYLE,
                "ID", "Nombre", "Fecha", "Hora", "Sala", "Capac.", "Cupos");
        System.out.println(CYAN + sep + RESET_STYLE);
        for (Evento e : lista) {
            System.out.printf("%-7s %-26s %-10s %-6s %-12s %8d %8d%n",
                    e.getIdEvento(),
                    recortar(e.getNombre(), 26),
                    e.getFecha(),
                    e.getHora(),
                    recortar(e.getSala(), 12),
                    e.getCapacidad(),
                    e.getCuposDisponibles());
        }
        System.out.println(CYAN + sep + RESET_STYLE);
    }

    private static String recortar(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    public static void main(String[] args) {
        sistema.seed();
        banner();
        Scanner sc = new Scanner(System.in);
        while (true) {
            menu();
            String op = sc.nextLine().trim();
            switch (op) {
                case "1":
                    System.out.println("→ (Se implementa en la siguiente fase: agregar asistente)");
                    break;
                case "2":
                    System.out.println("→ (Se implementa en la siguiente fase: listar asistentes de un evento)");
                    break;
                case "3":
                    System.out.println("→ (Se implementa en la siguiente fase: búsquedas por ID/nombre)");
                    break;
                case "4":
                    System.out.println("→ (Se implementa más adelante: reservar recurso)");
                    break;
                case "5":
                    imprimirTablaEventos(sistema.listarEventosConCupo());
                    break;
                case "0":
                    System.out.println("Hasta luego.");
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }
}
