package sia;

import sia.persistence.CsvStorage;
import sia.persistence.AuthStorage;
import sia.view.swing.LoginFrame;
import sia.view.swing.SplashScreen;
import sia.AuthService;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.List;

public class App {

    public static void main(String[] args) {
        // Look & Feel: intentar Nimbus; si no, usar el del sistema
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignore) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                // fallback: look and feel por defecto
            }
        }

        final SistemaEventos[] sistemaRef = new SistemaEventos[] { new SistemaEventos() };
        final AuthService[] authServiceRef = new AuthService[] { new AuthService() };

        SplashScreen.showAndRun(() -> {
            try {
                // Cargar datos de eventos
                CsvStorage.loadAll(sistemaRef[0]);

                // Cargar usuarios
                List<Usuario> usuarios = AuthStorage.loadUsers();
                authServiceRef[0].loadUsers(usuarios);
            } catch (Exception e) {
                System.err.println("No se pudieron cargar los datos iniciales: " + e.getMessage());
            }

            // Guardado automático al salir
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    CsvStorage.saveAll(sistemaRef[0]);
                    AuthStorage.saveUsers(authServiceRef[0].getAll());
                    System.out.println("Datos guardados en 'data/' (shutdown hook).");
                } catch (Exception ex) {
                    System.err.println("Error guardando datos al salir: " + ex.getMessage());
                }
            }));
        }, () -> {
            // Crear y mostrar la ventana de login
            LoginFrame loginFrame = new LoginFrame(authServiceRef[0], sistemaRef[0]);
            loginFrame.setVisible(true);
        });
    }
}