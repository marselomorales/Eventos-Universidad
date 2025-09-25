package sia.view.swing;

import javax.swing.*;
import java.awt.*;

public class BannerPanel extends JPanel {
    private final String title;
    private final String subtitle;

    public BannerPanel(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        setPreferredSize(new Dimension(1000, 100));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Gradiente moderno en azules profesionales
        Color c1 = new Color(41, 128, 185);
        Color c2 = new Color(52, 152, 219);
        g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Títulos con tipografía moderna
        g2.setColor(Color.WHITE);
        Font fTitle = new Font("Segoe UI", Font.BOLD, 28);
        Font fSub = new Font("Segoe UI", Font.PLAIN, 14);
        
        int y = 40;
        
        // Sombra sutil para profundidad
        g2.setColor(new Color(0, 0, 0, 40));
        g2.setFont(fTitle);
        g2.drawString(title, 32, y + 2);
        
        // Texto principal
        g2.setColor(Color.WHITE);
        g2.setFont(fTitle);
        g2.drawString(title, 30, y);
        
        // Subtítulo
        g2.setFont(fSub);
        g2.drawString(subtitle, 30, y + 35);
        
        g2.dispose();
        super.paintComponent(g);
    }
}