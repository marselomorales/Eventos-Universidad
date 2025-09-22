package sia.view.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D; 

/** Botón tipo "card": redondeado, con icono y texto, look moderno sin librerías. */
public class ActionCardButton extends JButton {
    private Color bg = new Color(248,248,248);
    private Color fgText = new Color(33,33,33);
    private Color outline = new Color(220,220,220);

    public ActionCardButton(String text, Icon icon) {
        super(text, icon);
        setHorizontalAlignment(LEFT);
        setIconTextGap(10);
        setMargin(new Insets(10,12,10,12));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(getFont().deriveFont(Font.BOLD, 12f));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 16;
        Shape r = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1f, getHeight()-1f, arc, arc);

        // elevación leve
        if (getModel().isRollover()) {
            g2.setColor(new Color(245,245,245));
        } else {
            g2.setColor(bg);
        }
        g2.fill((java.awt.Shape) r);
        g2.setColor(outline);
        g2.draw((java.awt.Shape) r);

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
