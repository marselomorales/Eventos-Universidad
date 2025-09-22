package sia.view.swing;

import javax.swing.BorderFactory;
import javax.swing.JWindow;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.awt.Dimension;

/**
 * Splash simple para mostrar mientras se cargan los datos.
 */
public final class SplashScreen {
    private final JWindow window;

    public SplashScreen() {
        window = new JWindow();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(new Color(245, 245, 245));

        JLabel title = new JLabel("Organizador de Eventos Universitario", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel subtitle = new JLabel("Cargando datos…", SwingConstants.CENTER);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 13f));
        subtitle.setForeground(new Color(80, 80, 80));

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        panel.add(title, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.CENTER);
        panel.add(bar, BorderLayout.SOUTH);

        window.getContentPane().add(panel);
        window.setSize(new Dimension(420, 150));
        window.setLocationRelativeTo(null);
    }

    public void showSplash() {
        window.setVisible(true);
    }

    public void close() {
        Window w = window;
        if (w != null) {
            w.setVisible(false);
            w.dispose();
        }
    }

    /** Ejecuta una tarea en background mostrando el splash. */
    public static void showAndRun(Runnable work, Runnable afterUi) {
        SplashScreen splash = new SplashScreen();
        splash.showSplash();
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                if (work != null) work.run();
                return null;
            }
            @Override protected void done() {
                splash.close();
                if (afterUi != null) SwingUtilities.invokeLater(afterUi);
            }
        }.execute();
    }
}