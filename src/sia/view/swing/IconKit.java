package sia.view.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/** Mini kit de íconos dibujados en tiempo de ejecución (sin imágenes externas). */
public final class IconKit {
    private IconKit() {}

    public static Icon plus()     { return draw((g,w,h)->{
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(w/2, 5, w/2, h-5);
        g.drawLine(5, h/2, w-5, h/2);
    }); }

    public static Icon edit()     { return draw((g,w,h)->{
        g.setStroke(new BasicStroke(2f));
        Polygon p = new Polygon(new int[]{6,18,16,4}, new int[]{16,4,2,14}, 4);
        g.drawPolygon(p);
        g.drawLine(6,18, 18,6);
    }); }

    public static Icon trash()    { return draw((g,w,h)->{
        g.setStroke(new BasicStroke(2f));
        g.drawRect(6,8,12,12);
        g.drawLine(4,8,20,8);
        g.drawLine(9,11,9,18);
        g.drawLine(15,11,15,18);
        g.drawLine(9,5,15,5);
    }); }

    public static Icon users()    { return draw((g,w,h)->{
        g.setStroke(new BasicStroke(2f));
        g.drawOval(5,5,6,6);
        g.drawArc(3,11,10,8,0,-180);
        g.drawOval(13,7,5,5);
        g.drawArc(12,12,8,6,0,-180);
    }); }

    public static Icon box()      { return draw((g,w,h)->{
        g.setStroke(new BasicStroke(2f));
        g.drawRect(4,6,16,12);
        g.drawLine(4,11,20,11);
    }); }

    public static Icon save()     { return draw((g,w,h)->{
        g.setStroke(new BasicStroke(2f));
        g.drawRect(5,5,14,14);
        g.drawRect(8,6,8,5);
        g.drawLine(9,15,15,15);
    }); }

    public static Icon report()   { return draw((g,w,h)->{
        g.setStroke(new BasicStroke(2f));
        g.drawRect(6,4,12,16);
        g.drawLine(8,8,16,8);
        g.drawLine(8,12,16,12);
        g.drawLine(8,16,12,16);
    }); }

    public static Icon searchPlus(){ return draw((g,w,h)->{
        g.setStroke(new BasicStroke(2f));
        g.drawOval(4,4,10,10);
        g.drawLine(13,13,19,19);
        g.drawLine(7,9,11,9);
        g.drawLine(9,7,9,11);
    }); }

    private interface Painter { void paint(Graphics2D g, int w, int h); }

    private static Icon draw(Painter painter) {
        int w = 24, h = 24;
        BufferedImage img = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(60,60,60));
        painter.paint(g2,w,h);
        g2.dispose();
        return new ImageIcon(img);
    }
}