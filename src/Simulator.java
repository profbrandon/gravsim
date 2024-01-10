
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import java.awt.Color;


class Simulator {

    public static double DELTA_T   = 0.05;
    public static double G_CONST   = 0.1;
    public static double DENSITY   = 1.0;
    public static double CRIT_MASS = 100000.0;

    private boolean sortEnabled = true;

    private Vec    com = Vec.ZERO;
    private Vec    tm  = Vec.ZERO;
    private double tam = 0.0;

    private long tick = 0l;

    private List<Body> alive = new ArrayList<> ();
    private List<Body> dead  = new ArrayList<> ();

    public Simulator () {

    }

    public void addBody (Body body) {
        this.alive.add (body);
    }

    public void addRandomBody (double maxMass, double minMass, String id) {
        double mass = Math.pow (Math.random (), 10) * (maxMass - minMass) + minMass;
        
        double radius = 1990 * Math.pow (Math.random (), 2.0) + 10;

        Vec pos = Matrix.rotate (new Vec (radius, 0.0), Math.random () * 2 * Math.PI);

        Vec vel = Matrix.rotate (Vec.scale (Vec.normal (pos), Math.pow (G_CONST * 20.0 / radius, 0.5)), Math.PI / 2);

        vel = Vec.add (vel, Vec.scale (new Vec (Math.random (), Math.random ()), 0.0005));

        //if (pos.x * pos.y > 10.0) mass *= 2.0;

        double omega = 0.0;

        int r = (int) (Math.random () * 192) + 64;
        int g = (int) (Math.random () * 192) + 64;
        int b = (int) (Math.random () * 192) + 64;

        this.addBody (new Body (mass, new Color (r,g,b), id, pos, vel, omega));
    }

    /*
    public void makeRandomDist (double maxMass, double minMass, Function<Vec,boolean> pred, int count) {
        for (int i = 0; i < count; ++i) {
            double mass = Math.random () * (maxMass - minMass) + minMass;
        
            Vec vel = new Vec (Math.random () - 0.5, Math.random () - 0.5);
            Vec pos = new Vec (1000 * Math.random () - 500, 1000 * Math.random () - 500);

            if (!pred.apply (pos)) continue;

            double omega = 0.0;

            int r = (int) (Math.random () * 192) + 64;
            int g = (int) (Math.random () * 192) + 64;
            int b = (int) (Math.random () * 192) + 64;

            this.addBody (new Body (mass, new Color (r,g,b), id, pos, vel, omega));
        }
    }
    */

    public void init () {
        this.com = this.centerOfMass ();
        this.tm  = this.totalMomentum ();
        this.tam = this.totalAngularMom (this.com);

        double totalMass = this.totalMass ();

        for (Body b : this.alive) {
            Vec correction = Vec.scale (this.tm, b.getMass () / totalMass);

            b.setPos (Vec.sub (b.getPos (), this.com));
            b.setMomentum (Vec.sub (b.getMomentum (), correction));
        }

        this.com = this.centerOfMass ();
        this.tm  = this.totalMomentum ();
        this.tam = this.totalAngularMom (this.com);

        this.sort ();

        System.out.println ("Total Angular Momentum: " + this.tam);
        System.out.println ("Total Mass: " + this.totalMass ());
    }

    public void sort () {
        this.alive.sort (new Comparator<Body>() {
            @Override
            public int compare (Body b1, Body b2) {
                if (b1.getMass () < b2.getMass ()) return 1;
                else if (b1.getMass () > b2.getMass ()) return -1;
                else return 0;
            }
        });
    }

    public void update () {

        // Check for Collisions
        boolean collisions = false;

        List<Body> toAdd = new ArrayList<> ();

        for (int i = 0; i < this.alive.size () - 1; ++i) {

            List<Body> toRemove = new ArrayList<> ();

            for (int j = i + 1; j < this.alive.size (); ++j) {
                Body b1 = this.alive.get (i);
                Body b2 = this.alive.get (j);

                if (Body.collision (b1, b2)) {
                    toRemove.addAll (Body.collide (b1, b2));
                    collisions = true;
                    break;
                }
            }

            for (Body b : toRemove) {
                if (this.alive.contains (b)) {
                    this.alive.remove (b);
                    this.dead.add (b);
                }
                else {
                    toAdd.add (b);
                }
            }
        }

        if (collisions && this.sortEnabled) {
            this.sort ();
        }

        // Update Physical System
        int bodyCount = this.alive.size ();

        List<Vec> velocities = new ArrayList<> ();
        List<Vec> positions  = new ArrayList<> ();

        for (int i = 0; i < bodyCount; ++i) {
            Body b  = this.alive.get (i);
            Vec acc = Vec.ZERO;

            //positions.add (Vec.scale (b.getVel (), DELTA_T));
            //velocities.add (Vec.add (b.getVel (), Vec.scale (b.getAcc (), DELTA_T)));

            for (int j = 0; j < bodyCount; ++j) {
                if (i != j) {
                    Body d = this.alive.get (j);
                    Vec  r = Vec.sub (d.getPos (), b.getPos ());

                    acc = Vec.add (acc, Vec.scale (Vec.normal (r), G_CONST * d.getMass () / Vec.dot (r, r)));
                }
            }

            b.setAcc (acc);

            // Why is it not 1/2 a t^2?
            positions.add (Vec.add (Vec.scale (b.getVel (), DELTA_T), Vec.scale (acc, DELTA_T * DELTA_T)));
            velocities.add (Vec.add (b.getVel (), Vec.scale (acc, DELTA_T)));
        }

        for (int i = 0; i < bodyCount; ++i) {
            Body b = this.alive.get (i);

            b.setVel (velocities.get (i));
            b.translate (positions.get (i));
            b.rotate (b.getOmega () * DELTA_T);
        }

        for (Body b : toAdd) this.alive.add (b);

        ++tick;
    }

    public List<Body> getAlive () {
        return this.alive;
    }

    public long getTick () {
        return this.tick;
    }

    public Vec centerOfMass () {
        Vec    sum  = Vec.ZERO;
        double mass = 0;
        
        for (Body b : this.alive) {
            sum = Vec.add (sum, Vec.scale (b.getPos (), b.getMass ()));
            mass += b.getMass ();
        }

        if (mass == 0) return sum;
        else return Vec.scale (sum, 1.0 / mass);
    }

    public Vec totalMomentum () {
        Vec sum = Vec.ZERO;

        for (Body b : this.alive) {
            sum = Vec.add (sum, b.getMomentum ());
        }

        return sum;
    }

    public double totalAngularMom (Vec center) {
        double sum = 0;

        for (Body b : this.alive) {
            sum += b.getAngularMom ();
            sum += Vec.cross (Vec.sub (b.getPos (), center), b.getMomentum ());
        }

        return sum;
    }

    public double totalMass () {
        double sum = 0.0;

        for (Body b : this.alive) {
            sum += b.getMass ();
        }

        return sum;
    }

    public double totalKinetic () {
        double sum = 0;

        for (Body b : this.alive) {
            sum += b.getKineticEnergy ();
            sum += b.getRotationalEnergy ();
        }

        return sum;
    }

    public double totalPotential () {
        double sum = 0;
        
        for (int i = 0; i < this.alive.size (); ++i) {
            Body   b1 = this.alive.get (i);
            double m1 = b1.getMass ();
            Vec    p1 = b1.getPos ();

            for (int j = i + 1; j < this.alive.size (); ++j) {
                Body   b2 = this.alive.get (j);
                double m2 = b2.getMass ();
                Vec    p2 = b2.getPos ();

                sum -= G_CONST * m1 * m2 / Vec.len (Vec.sub (p2, p1));
            }
        }

        return sum;
    }

    public void setSortEnabled (boolean b) {
        this.sortEnabled = b;
    }
}