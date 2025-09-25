package sia.view.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Botón tipo "card": redondeado, con icono y texto, look moderno sin librerías. */
public class ActionCardButton extends JButton {
    private Color bg = AppStyle.CARD_BG;
    private Color fgText = AppStyle.TEXT_PRIMARY;
    private Color outline = AppStyle.BORDER;

    public ActionCardButton(String text, Icon icon) {
        super(text, icon);
        setHorizontalAlignment(LEFT);
        setIconTextGap(10);
        setMargin(new Insets(10,12,10,12));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(AppStyle.FONT_BUTTON);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = AppStyle.BORDER_RADIUS * 2; // 16
        Shape r = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1f, getHeight()-1f, arc, arc);

        // elevación leve
        if (getModel().isRollover()) {
            g2.setColor(bg.brighter());
        } else {
            g2.setColor(bg);
        }
        g2.fill(r);
        g2.setColor(outline);
        g2.draw(r);

        // delega icono/texto
        super.paintComponent(g);

        g2.dispose();
    }

    @Override
    public Insets getInsets() {
        Insets i = super.getInsets();
        return new Insets(i.top+2, i.left+2, i.bottom+2, i.right+2);
    }
}