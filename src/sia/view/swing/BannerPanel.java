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
        
        // Usar colores de AppStyle
        Color c1 = AppStyle.PRIMARY;
        Color c2 = AppStyle.SECONDARY;
        g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Usar fuentes de AppStyle
        Font fTitle = AppStyle.FONT_TITLE;
        Font fSub = AppStyle.FONT_SUBTITLE;
        
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