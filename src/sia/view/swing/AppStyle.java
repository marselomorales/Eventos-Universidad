package sia.view.swing;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

public final class AppStyle {
    private AppStyle() {}
    
    // === PALETA DE COLORES ===
    public static final Color PRIMARY = new Color(41, 128, 185);
    public static final Color PRIMARY_DARK = new Color(31, 97, 141);
    public static final Color SECONDARY = new Color(52, 152, 219);
    public static final Color SUCCESS = new Color(39, 174, 96);
    public static final Color WARNING = new Color(243, 156, 18);
    public static final Color DANGER = new Color(231, 76, 60);
    public static final Color LIGHT_GRAY = new Color(248, 249, 250);
    public static final Color CARD_BG = new Color(252, 252, 252);
    public static final Color BORDER = new Color(220, 220, 220);
    public static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    
    // === TIPOGRAFÍA ===
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    
    // === TAMAÑOS Y ESPACIADO ===
    public static final int BORDER_RADIUS = 12;
    public static final int BUTTON_HEIGHT = 40;
    public static final int INPUT_HEIGHT = 38;
    public static final Insets MARGIN_REGULAR = new Insets(12, 16, 12, 16);
    public static final Insets MARGIN_COMPACT = new Insets(8, 12, 8, 12);
    
    // === MÉTODOS DE AYUDA ===
    public static Border createBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );
    }
    
    public static Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );
    }
}