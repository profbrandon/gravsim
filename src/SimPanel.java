
import java.util.List;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;


class SimPanel extends JPanel implements KeyListener {

    private final Simulator sim;
    
    public static final int    TRAIL_SPACING     = 100;
    public static final int    MAX_TRAIL_LEN     = 10000;
    public static final int    WIDTH             = 601;
    public static final int    HEIGHT            = 601;
    public static final double TRAIL_COLOR_SCALE = 0.2;

    private double scale   = 1.0;
    private int    counter = 0;

    private int    trailLength = 0;
    private double gridSize    = 100;

    private boolean trailsEnabled  = false;
    private boolean labelsEnabled  = false;
    private boolean dataEnabled    = true;
    private boolean gridEnabled    = false;
    private boolean vectorsEnabled = true;

    private Body ref      = null;
    private Body selected = null;
    private Vec  center   = Vec.ZERO;




    public SimPanel (Simulator sim) {
        this.setPreferredSize (new Dimension (WIDTH, HEIGHT));
        this.setDoubleBuffered (true);
        this.setBackground (Color.BLACK);

        this.sim = sim;
    }

    
    @Override
    public void paint (Graphics g) {
        super.paint (g);

        g.setFont (new Font ("Consolas", Font.PLAIN, 10));

        if (this.selected != null) {
            this.center = this.selected.getPos ();
        }
        else if (this.ref != null) {
            this.center = this.ref.getPos ();
        }
        
        List<Body> alive = this.sim.getAlive ();

        // Print Grid
        if (this.gridEnabled) {
            int totalVertical = (int) (this.getWidth () * scale / gridSize);
            //int totalHorizontal = (int) (this.getHeight () * scale / gridSize);

            for (int i = 0; i < totalVertical; ++i) {
                
            }
        }

        // Add New Trail Data
        if (counter % TRAIL_SPACING == 0) {
            for (Body b : alive) {
                b.getTrail ().addPoint ();
                counter = 0;
            }
        }

        // Print Trails
        double angle = 0.0;

        boolean adjust = false; //this.selected != this.ref && this.ref != null;

        if (adjust) {
            Vec r = Vec.normal (Vec.sub (this.selected.getPos (), this.ref.getPos ()));
            angle = - Vec.angle (r, Vec.UNIT_X);
        }

        if (this.trailsEnabled) {
            for (Body b : alive) {
                if (b.getTrail ().length () != 0) {
                    paintTrail (g, b.getTrail (), adjust);
                }
            }
        }
        else if (this.selected != null) {
            if (selected.getTrail ().length () != 0) {
                paintTrail (g, selected.getTrail (), adjust);
            }
        }

        int onScreen = 0;

        for (Body b : alive) {
            if (paintBody (g, b, adjust)) ++onScreen;
        }

        if (this.vectorsEnabled && this.selected != null && !this.selected.equals (this.ref)) {
            Vec vel = (this.ref == null) ? this.selected.getVel () : Vec.sub (this.selected.getVel (), this.ref.getVel ());
            Vec acc = (this.ref == null) ? this.selected.getAcc () : Vec.sub (this.selected.getAcc (), this.ref.getAcc ());

            Vec.scale (Matrix.rotate (acc, angle), 10 * scale).paint (g, Color.RED, new Vec (WIDTH/2, HEIGHT/2));
            Vec.scale (Matrix.rotate (vel, angle), 10 * scale).paint (g, Color.GREEN, new Vec (WIDTH/2, HEIGHT/2));
        }

        // Center of Mass Crosshair
        Vec temp = Matrix.rotate (this.center, angle);
        int x = WIDTH/2 - (int) Math.round (scale * temp.x);
        int y = HEIGHT/2 + (int) Math.round (scale * temp.y);

        g.setColor (Color.MAGENTA);
        g.drawLine (x - 2,y, x + 2,y);
        g.drawLine (x,y - 2, x,y + 2);


        // Data
        if (this.dataEnabled) {
            g.setColor (Color.WHITE);

            // Print Background
            g.setXORMode (new Color (240,240,240));

            if (this.ref != null && this.selected != null) {
                g.fillRect (3,3, 183,192);

                if (!this.ref.equals (this.selected)) {
                    g.fillRect (3,195, 183, 12);
                }
            }
            else if (this.ref != null || this.selected != null) g.fillRect (3,3, 183,122);
            else g.fillRect (3,3,183,60);

            g.setXORMode (Color.BLACK);

            // Print Body Count and Tick
            g.drawString (String.format ("BODY COUNT: %18s", onScreen + "/" + this.sim.getAlive ().size ()), 5, 12);
            g.drawString (String.format ("TICK: %24s", this.sim.getTick ()), 5, 24);
            g.drawString (String.format ("CENTER: %22s", this.center), 5, 36);
            g.drawString (String.format ("TOTAL ENERGY: %15.3f", this.sim.totalKinetic () + this.sim.totalPotential ()), 5, 48);
            g.drawString (String.format ("TOTAL MASS: %18.3f", this.sim.totalMass ()), 5, 60);

            // Print Body Data
            int verticalOffset = 0;

            if (this.selected != null || this.ref != null) g.drawLine (4,62, 184,62);// Here

            if (this.selected != null && !this.selected.equals (this.ref)) {
                g.drawString (String.format ("SELECTED: %20s", "[" + this.selected.getId () + "]"), 5, 72);
                drawBodyData (g, this.selected, 5, 84, this.ref != null);

                verticalOffset = 72;

                if (this.ref != null && !this.ref.equals (this.selected)) {
                    verticalOffset += 12;

                    //double orbitalPeriod = Body.getOrbitalPeriod (this.selected, this.ref, this.trailLength);
                    double eccentricity = Body.getEccentricity (this.selected, this.ref);

                    //g.drawString (String.format ("ORBITAL PERIOD: %10.0f tks", orbitalPeriod), 5, 132);
                    g.drawString (String.format ("ECCENTRICITY: %6.10f", eccentricity), 5, 132);
                }
            }

            if (this.ref != null) {
                g.drawString (String.format ("RELATIVE TO: %17s", "[" + this.ref.getId () + "]"), 5, 72 + verticalOffset);
                drawBodyData (g, this.ref, 5, 84 + verticalOffset, false);
            }
        }

        ++counter;
    }

    private boolean paintBody (Graphics g, Body b, boolean rotate) {
        Color temp = g.getColor ();

        // Compute Data
        int radius = (int) Math.round (b.getRadius () * scale);
        int width  = (int) Math.round (2 * radius + 1);

        double angle = 0.0;

        if (rotate) {
            angle = Vec.angle (Vec.sub (this.ref.getPos (), this.selected.getPos ()), Vec.UNIT_X);
        }

        Vec relpos = Matrix.rotate (Vec.sub (b.getPos (), this.center), angle);

        Vec p = Vec.scale (relpos, scale);

        int x = WIDTH/2 + (int) Math.round (p.x);
        int y = HEIGHT/2 - (int) Math.round (p.y);

        int startAngle = (int) (180.0 * b.getAngle () / Math.PI);

        // Handle Out-of-Bounds 
        if (x < -radius || y < -radius || x > WIDTH + radius || y > HEIGHT + radius) return false;

        // Draw Body at x y
        if (radius < 1) {
            if (startAngle >= 0 && startAngle < 180.0)
                g.setColor (b.getColor ());
            else
                g.setColor (Coloring.scale (b.getColor (), 0.3));

            g.drawLine (x, y, x, y);

            if (this.labelsEnabled) {
                g.setColor (b.getColor ());
                g.drawString ("[" + b.getId () + "]", x + 5, y + 5);
            }
        }
        else {
            x -= radius + 1;
            y -= radius + 1;

            g.setColor (b.getColor ());
            g.fillArc (x, y, width, width, startAngle, 90);
            g.fillArc (x, y, width, width, startAngle + 180, 90);

            g.setColor (Coloring.scale (b.getColor (), 0.4));
            g.fillArc (x, y, width, width, startAngle + 90, 90);
            g.fillArc (x, y, width, width, startAngle + 270, 90);

            if (this.labelsEnabled) {
                g.drawString ("[" + b.getId () + "]", x + 2 * radius + 5, y + radius + 5);
            }
        }

        g.setColor (temp);

        return true;
    }

    private void paintTrail (Graphics g, Trail t, boolean rotate) {
        Color temp = g.getColor ();
        g.setColor (t.getColor ());

        int length = Math.min (t.length (), this.trailLength);

        Trail center = (this.ref == null) ? null : this.ref.getTrail ();

        for (int i = t.length () - 1; i > t.length () - length; --i) {
            Vec offset1 = Vec.ZERO;
            Vec offset2 = Vec.ZERO;

            if (this.ref != null) {
                offset1 = Vec.sub (center.getPoint (i - 1), this.ref.getPos ());
                offset2 = Vec.sub (center.getPoint (i), this.ref.getPos ());
            }

            Vec pos1 = Vec.sub (t.getPoint (i - 1), offset1);
            Vec pos2 = Vec.sub (t.getPoint (i), offset2);

            if (rotate) {
                double angle1 = Vec.angle (Vec.scale (offset1, -1), Vec.UNIT_X);
                double angle2 = Vec.angle (Vec.scale (offset2, -1), Vec.UNIT_X);

                pos1 = Matrix.rotate (pos1, angle1);
                pos2 = Matrix.rotate (pos2, angle2);
            }

            drawLine (g, pos1, pos2);
        }

        int last = t.length () - 1;

        if (last != -1 && length != 0) {
            Vec offset1 = Vec.ZERO;
            Vec offset2 = Vec.ZERO;

            if (this.ref != null) {
                offset1 = Vec.sub (center.getPoint (last), this.ref.getPos ());
                offset2 = Vec.sub (center.getCurrentPos (), this.ref.getPos ());
            }

            Vec pos1 = Vec.sub (t.getPoint (last), offset1);
            Vec pos2 = Vec.sub (t.getCurrentPos (), offset2);

            drawLine (g, pos1, pos2);
        }
        

        g.setColor (temp);
    }

    private void drawLine (Graphics g, Vec pos1, Vec pos2) {
        Vec p1 = Vec.scale (Vec.sub (pos1, this.center), scale);
        Vec p2 = Vec.scale (Vec.sub (pos2, this.center), scale);

        int x1 = WIDTH/2 + (int) Math.round (p1.x);
        int y1 = HEIGHT/2 - (int) Math.round (p1.y);
        int x2 = WIDTH/2 + (int) Math.round (p2.x);
        int y2 = HEIGHT/2 - (int) Math.round (p2.y);

        boolean outOfBounds1 = x1 < 0 || y1 < 0 || x1 > WIDTH || y1 > HEIGHT;
        boolean outOfBounds2 = x2 < 0 || y2 < 0 || x2 > WIDTH || y2 > HEIGHT;

        if (outOfBounds1 && outOfBounds2) return;

        g.drawLine (x1,y1, x2,y2);
    }

    private void drawBodyData (Graphics g, Body b, int x, int y, boolean relative) {
        Vec pOffset = Vec.ZERO;
        Vec vOffset = Vec.ZERO;

        if (relative && this.ref != null) {
            pOffset = this.ref.getPos ();
            vOffset = this.ref.getVel ();
        }

        g.drawString (String.format ("MASS:     %20.3f", b.getMass ()), x, y);
        g.drawString (String.format ("POSITION: %20s", Vec.sub (b.getPos (), pOffset)), x, y + 12);
        g.drawString (String.format ("VELOCITY: %20s", Vec.sub (b.getVel (), vOffset)), x, y + 24);
        g.drawString (String.format ("OMEGA:    %20.3f", b.getOmega ()), x, y + 36);
    }



    @Override
    public void keyPressed (KeyEvent e) {
        Vec delta = Vec.ZERO;

        double mag = 10.0 / scale;

        switch (e.getKeyCode ()) {
            case KeyEvent.VK_UP:
                delta = new Vec (0.0, mag);
                break;
            case KeyEvent.VK_DOWN:
                delta = new Vec (0.0, -mag);
                break;
            case KeyEvent.VK_LEFT:
                delta = new Vec (-mag, 0.0);
                break;
            case KeyEvent.VK_RIGHT:
                delta = new Vec (mag, 0.0);
                break;
        }

        this.center = Vec.add (this.center, delta);
    }

    @Override
    public void keyReleased (KeyEvent e) {

    }

    @Override
    public void keyTyped (KeyEvent e) {

    }




    public void toggleTrails () {
        this.trailsEnabled = !this.trailsEnabled;
    }

    public void toggleLabels () {
        this.labelsEnabled = !this.labelsEnabled;
    }

    public void toggleData () {
        this.dataEnabled = !this.dataEnabled;
    }

    public void toggleGrid () {
        this.gridEnabled = !this.gridEnabled;
    }

    public void changeTrailLength (int value) {
        if (value == -1) {
            this.trailLength = MAX_TRAIL_LEN;
        }
        else {
            this.trailLength = value;
        }
    }

    public void setRef () {
        this.ref = this.selected;
    }

    public void setSelected (Body b) {
        this.selected = b;

        if (this.selected != null) {
            this.center = b.getPos ();
        }
    }

    public void translateCenter (Vec offset) {
        this.center = Vec.add (this.center, offset);
    }

    public void setScale (double mag) {
        this.scale = Math.pow (10, mag);
    }
}