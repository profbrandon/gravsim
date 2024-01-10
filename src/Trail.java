
import java.util.List;
import java.util.ArrayList;

import java.awt.Color;


class Trail {

    private Body parent;
    private List<Vec> points = new ArrayList<> ();

    private long maxLength;

    private Color color;



    public Trail (Body parent, long maxLength, double colorScale) {
        this.parent    = parent;
        this.maxLength = maxLength;
        this.color     = Coloring.scale (this.parent.getColor (), colorScale);
    }

    public void addPoint () {
        this.addPoint (this.parent.getPos ());
    }

    public void addPoint (Vec point) {
        if (this.maxLength != -1 && this.points.size () == this.maxLength) {
            this.points.remove (0);
        }

        points.add (point);
    }

    public Vec getPoint (int i) {
        if (i < 0) return Vec.ZERO;
        if (i >= this.length ()) return this.getCurrentPos ();
        else return this.points.get (i);
    }

    public Color getColor () {
        return this.color;
    }

    public Vec getCurrentPos () {
        return this.parent.getPos ();
    }

    public int length () {
        return this.points.size ();
    }
}