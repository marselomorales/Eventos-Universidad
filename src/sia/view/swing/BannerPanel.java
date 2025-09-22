package sia.view.swing;

import javax.swing.*;
import java.awt.*;

/** Banner simple con gradiente y títulos (JDK11, sin libs externas). */
public class BannerPanel extends JPanel {
    private final String title;
    private final String subtitle;

    public BannerPanel(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        setPreferredSize(new Dimension(1000, 84));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Gradiente suave
        Color c1 = new Color(66, 133, 244);
        Color c2 = new Color(30, 136, 229);
        g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Títulos
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        Font fTitle = getFont().deriveFont(Font.BOLD, 22f);
        Font fSub   = getFont().deriveFont(Font.PLAIN, 13f);

        int y = 34;
        g2.setFont(fTitle);
        g2.drawString(title, 20, y);
        g2.setFont(fSub);
        g2.drawString(subtitle, 20, y + 22);

        g2.dispose();
        super.paintComponent(g);
    }
}