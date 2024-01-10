
import java.awt.Graphics;
import java.awt.Color;


class Vec {

    public static final Vec ZERO   = new Vec (0, 0);
    public static final Vec UNIT_X = new Vec (1, 0);
    public static final Vec UNIT_Y = new Vec (0, 1);

    public final double x, y;



    public Vec (double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString () {
        return String.format ("<%.2f,%.2f>", this.x, this.y);
    }

    public boolean equals (Vec v) {
        return this.x == v.x && this.y == v.y;
    }


    // Drawing 
    public void paint (Graphics g, Color color, Vec pos) {
        Color temp = g.getColor ();

        g.setColor (color);

        Vec target = new Vec (this.x, -this.y);

        int x1 = (int) Math.round (pos.x);
        int y1 = (int) Math.round (pos.y);
        
        Vec end = Vec.add (target, pos);

        int x2 = (int) Math.round (end.x);
        int y2 = (int) Math.round (end.y);

        g.drawLine (x1,y1, x2,y2);

        double headLength = Math.min (10.0, Vec.len (target) / 5.0);

        Vec head1 = Vec.add (Vec.scale (Vec.normal (Matrix.rotate (Vec.scale (target, -1.0), - Math.PI / 8.0)), headLength), end);
        Vec head2 = Vec.add (Vec.scale (Vec.normal (Matrix.rotate (Vec.scale (target, -1.0), Math.PI / 8.0)), headLength), end);

        int x3 = (int) Math.round (head1.x);
        int y3 = (int) Math.round (head1.y);
        
        int x4 = (int) Math.round (head2.x);
        int y4 = (int) Math.round (head2.y);

        g.fillPolygon (new int[] {x2, x3, x4}, new int[] {y2, y3, y4}, 3);


        g.setColor (temp);
    }


    // Linearity

    public static Vec add (Vec u, Vec v) {
        return new Vec (u.x + v.x, u.y + v.y);
    }

    public static Vec sub (Vec u, Vec v) {
        return new Vec (u.x - v.x, u.y - v.y);
    }

    public static Vec scale (Vec v, double s) {
        return new Vec (v.x * s, v.y * s);
    }


    // Inner Product Space

    public static double dot (Vec u, Vec v) {
        return u.x * v.x + u.y * v.y;
    }

    public static double angle (Vec u, Vec v) {
        return Math.acos (Vec.dot (Vec.normal (u), Vec.normal (v)));
    }

    public static double cross (Vec u, Vec v) {
        return u.x * v.y - u.y * v.x;
    }

    public static double len2 (Vec v) {
        return dot (v, v);
    }

    public static double len (Vec v) {
        return Math.sqrt (dot (v, v));
    }

    public static Vec normal (Vec v) {
        double len = len (v);
        
        return (len == 0) ? ZERO : scale (v, 1.0 / len (v));
    }
}