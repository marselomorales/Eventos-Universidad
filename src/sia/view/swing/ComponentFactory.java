package sia.view.swing;

import javax.swing.*;
import java.awt.*;

public final class ComponentFactory {
    
    public static JButton createPrimaryButton(String text) {
        return createButton(text, AppStyle.PRIMARY, Color.WHITE);
    }
    
    public static JButton createSecondaryButton(String text) {
        return createButton(text, Color.WHITE, AppStyle.PRIMARY);
    }
    
    public static JButton createSuccessButton(String text) {
        return createButton(text, AppStyle.SUCCESS, Color.WHITE);
    }
    
    public static JButton createDangerButton(String text) {
        return createButton(text, AppStyle.DANGER, Color.WHITE);
    }
    
    public static JButton createWarningButton(String text) {
        return createButton(text, AppStyle.WARNING, Color.WHITE);
    }
    
    private static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(AppStyle.FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        Insets m = AppStyle.MARGIN_REGULAR;
        btn.setBorder(BorderFactory.createEmptyBorder(m.top, m.left, m.bottom, m.right));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Efecto hover moderno
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        
        return btn;
    }
    
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(AppStyle.FONT_INPUT);
        tf.setBorder(AppStyle.createInputBorder());
        return tf;
    }
    
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(AppStyle.CARD_BG);
        panel.setBorder(AppStyle.createBorder());
        return panel;
    }
    
    // Nuevos métodos para la mejora
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppStyle.FONT_LABEL);
        label.setForeground(AppStyle.TEXT_PRIMARY);
        return label;
    }
    
    public static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(AppStyle.FONT_INPUT);
        combo.setBackground(Color.WHITE);
        combo.setFocusable(false);
        return combo;
    }
}