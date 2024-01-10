
import java.awt.Color;


class Coloring {

    public static Color scale (Color c, double t) {
        if (t <= 0) return Color.BLACK;
        if (t >= 1) return Color.WHITE;
        if (t == 0.5) return c;

        int r = c.getRed ();
        int g = c.getGreen ();
        int b = c.getBlue ();
        
        if (t < 0.5) {
            t *= 2;

            return new Color ((int) (r * t), (int) (g * t), (int) (b * t));
        }
        else {
            t = t * 2 - 1;

            return new Color ((int) ((255 + r) * t + r), (int) ((255 + g) * t + g), (int) ((255 + b) * t + b));
        }
    }
}