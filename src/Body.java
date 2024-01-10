
import java.util.List;
import java.util.ArrayList;

import java.awt.Color;


class Body {

    public static final double DENSITY    = 1.0;
    public static final double CRIT_MASS  = 100000.0;
    //public static final double ELASTICITY = 1.0;
    //public static final double FRICTION   = 0.0;

    private Vec    pos;
    private Vec    vel;
    private Vec    acc;
    private double omega;
    private double angle;

    private double radius;
    private double mass;
    private Color  color;
    private String id = "";
    private Trail  trail;




    public Body (double mass, Color color, String id, Vec pos, Vec vel, double omega) {
        this.pos   = pos;
        this.vel   = vel;
        this.acc   = Vec.ZERO;
        this.omega = omega;
        this.angle = 0.0;

        this.mass   = mass;
        this.radius = Body.computeRadius (mass);
        this.color  = color;
        this.id     = id;

        this.trail  = new Trail (this, SimPanel.MAX_TRAIL_LEN, SimPanel.TRAIL_COLOR_SCALE);
    }




    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        else if (o == null) return this == null;
        else if (o instanceof Body) return this.equals ((Body) o);
        else return false;
    }

    public boolean equals (Body b) {
        if (b == null) return this == null;
        else return this.pos.equals (b.pos) && this.id.equals (b.id);
    }

    @Override
    public String toString () {
        return "[" + this.id + "]: " + String.format ("%.3f", this.mass) + " at " + this.pos;
    }

    public double getMass () {
        return this.mass;
    }

    public Trail getTrail () {
        return this.trail;
    }

    public String getId () {
        return this.id;
    }

    public Vec getMomentum () {
        return Vec.scale (this.vel, this.mass);
    }

    public void setMomentum (Vec v) {
        this.vel = Vec.scale (v, 1.0 / this.mass);
    }

    public double getAngularMom () {
        return this.mass * this.omega;
    }

    public double getRadius () {
        return this.radius;
    }

    public Vec getPos () {
        return this.pos;
    }

    public void setPos (Vec pos) {
        this.pos = pos;
    }

    public Vec getVel () {
        return this.vel;
    }

    public void setVel (Vec vel) {
        this.vel = vel;
    }

    public Vec getAcc () {
        return this.acc;
    }

    public void setAcc (Vec acc) {
        this.acc = acc;
    }

    public double getAngle () {
        return this.angle;
    }

    public double getOmega () {
        return this.omega;
    }

    public Color getColor () {
        return this.color;
    }

    public double getKineticEnergy () {
        return 0.5 * this.mass * Vec.len2 (this.vel);
    }

    public double getMOI () {
        return 0.4 * this.mass * this.radius * this.radius;
    }

    public double getRotationalEnergy () {
        return 0.5 * getMOI () * this.omega * this.omega;
    }

    public void rotate (double angle) {
        this.angle += angle;
        this.angle %= 2 * Math.PI;
    }

    public void setOmega (double omega) {
        this.omega = omega;
    }

    public void translate (Vec vec) {
        this.pos = Vec.add (this.pos, vec);
    }

    private static double computeRadius (double mass) {
        if (mass >= CRIT_MASS) {
            mass /= CRIT_MASS;
        }

        return Math.pow (0.2387324 * mass / DENSITY, 1.0 / 2.0);
        //return Math.pow (0.2387324 * mass / DENSITY, 1.0 / 3.0);
    }

    public static double getOrbitalPeriod (Body satellite, Body center, int maxLength) {
        double sum = 0.0;

        int length = Math.min (satellite.trail.length (), maxLength);

        for (int i = satellite.trail.length () - 1; i > satellite.trail.length () - length; --i) {
            Vec p1 = Vec.sub (satellite.trail.getPoint (i - 1), center.trail.getPoint (i - 1));
            Vec p2 = Vec.sub (satellite.trail.getPoint (i), center.trail.getPoint (i));

            Vec vel = Vec.scale (Vec.sub (p2, p1), 1.0 / SimPanel.TRAIL_SPACING);

            sum += Vec.cross (p1, vel) / Vec.len2 (p1);
        }

        if (length - 1 <= 0) return 0.0;
        
        double average = sum / (length - 1);

        if (average == 0.0) return 0.0;
        else return Math.abs (2.0 * Math.PI / average);
    }

    public static double getEccentricity (Body satellite, Body center) {
        Vec v = Vec.sub (satellite.getVel (), center.getVel ());
        Vec r = Vec.sub (satellite.getPos (), center.getPos ());

        Vec vxh = Vec.sub (Vec.scale (r, Vec.dot (v, v)), Vec.scale (v, Vec.dot (r, v)));

        Vec e = Vec.sub (Vec.scale (vxh, 1 / (Simulator.G_CONST * center.mass)), Vec.normal (r));
        
        return Vec.len (e);
    }



    // Collisions

    private static double posAngular (Body b) {
        return b.getAngularMom () + Vec.cross (b.pos, b.getMomentum ());
    }

    private void absorb (Body b) {
        double m = this.mass + b.mass;
        
        this.omega = (posAngular (b) + this.getAngularMom () - Vec.cross (this.pos, b.getMomentum ())) / m;

        this.vel  = Vec.scale (Vec.add (this.getMomentum (), b.getMomentum ()), 1.0 / m);
        this.mass = m;

        this.radius = Body.computeRadius (this.mass);
    }

    private static void bounce (Body b1, Body b2) {
        Vec p1 = b1.getMomentum ();
        Vec p2 = b2.getMomentum ();

        Vec r12 = Vec.normal (Vec.sub (b2.getPos (), b1.getPos ()));
        Vec r21 = Vec.scale (r12, -1.0);
        Vec o12 = Matrix.rotate (r12, Math.PI / 2.0);
        Vec o21 = Vec.scale (o12, -1.0);

        // Compute New Momenta
        Vec q1 = Vec.add (Vec.scale (r21, Vec.dot (r21, p2)), Vec.scale (o12, Vec.cross (r12, p1)));
        Vec q2 = Vec.add (Vec.scale (r12, Vec.dot (r12, p1)), Vec.scale (o21, Vec.cross (r21, p2)));

        b1.setMomentum (q1);
        b2.setMomentum (q2);
    }

    public static boolean collision (Body b1, Body b2) {
        double dist = Vec.len (Vec.sub (b2.pos, b1.pos));
        return dist <= b1.radius + b2.radius;
    }

    public static List<Body> collide (Body b1, Body b2) {
        List<Body> list = new ArrayList<> ();
        /*
        Body big = null;
        Body small = null;

        if (b1.mass >= b2.mass) {
            big = b1;
            small = b2;
        }
        else {
            big = b2;
            small = b1;
        }

        double ratio = small.mass / big.mass;

        if (big.mass <= 1 || ratio < 0.0001 || small.mass <= 0.1) {
            big.absorb (small);
            list.add (small);
            return list;
        }

        Vec dir = Vec.normal (Vec.sub (small.pos, big.pos));

        double momAxis = Vec.dot (dir, Vec.sub (small.getMomentum (), big.getMomentum ()));
        double momPerp = Vec.cross (dir, Vec.sub (small.getMomentum (), big.getMomentum ()));

        double a = 2.0 / (1 + Math.exp (- 0.1 * Math.abs (momAxis))) - 1;

        double massF  = (big.mass + small.mass) * (1 - 0.25 * a * ratio * (1 + ratio));
        double deltaM = big.mass + small.mass - massF;
        double angle  = Math.PI / 2.0 * Math.sqrt (ratio);

        Vec center = Vec.scale (Vec.add (Vec.scale (big.pos, big.mass), Vec.scale (small.pos, small.mass)), 1.0 / (big.mass + small.mass));
        double distDelta = (big.mass + small.mass) * Math.pow (big.radius + small.radius + 1.0, -1.5);
        System.out.println ("Delta Distance: " + distDelta);

        // Eject
        int counter = 0;
        Vec totalMom = Vec.ZERO;

        while (deltaM > 0.001) {
            double m = Math.max (0.001, Math.random () * deltaM / 40.0);
            double posNeg = 2 * (int) (2 * Math.random ()) - 1;

            double randFactor = Math.random ();
            double dist = Body.computeRadius (massF) + randFactor * distDelta + Body.computeRadius (m) + 0.1;
            double angleDelta = (Math.random () - 0.5) * Math.PI / (1.0 + randFactor) / 2.0;

            Vec eDir = Matrix.rotate (dir, posNeg * angle + angleDelta);
            Vec pos  = Vec.add (center, Vec.scale (eDir,dist));
            Vec vel = Vec.scale (eDir, - 10.0 * Math.sqrt (2.0 * (big.mass + small.mass) / dist));

            Body body = new Body (
                m, 
                big.color,
                big.id + "_" + Integer.toHexString (counter).toUpperCase (),
                pos,
                vel,
                0.0);

            list.add (body);
            deltaM -= m;
            ++counter;
            totalMom = Vec.add (totalMom, Vec.scale (vel, m));
        }

        Vec correction = Vec.scale (Matrix.rotate (dir, - Math.PI / 4.0), Vec.cross (dir, totalMom) - 0.1 * momPerp);

        for (Body b : list) {
            Vec temp = b.getMomentum ();
            b.setMomentum (Vec.add (temp, Vec.scale (correction, 1.0 / counter)));
        }

        list.add (small);

        // Absorb
        Vec smallMom = Vec.sub (small.getMomentum (), Vec.add (correction, totalMom));

        big.omega = (small.omega * (big.mass + small.mass - massF) + posAngular (big)
            + Vec.cross (small.pos, smallMom) 
            - Vec.cross (center, smallMom)
            - Vec.cross (center, big.getMomentum ())) / (massF + deltaM);

        big.pos  = center;
        big.vel  = Vec.scale (Vec.add (big.getMomentum (), smallMom), 1.0 / (massF + deltaM));
        big.mass = massF + deltaM;

        big.radius = Body.computeRadius (big.mass);
        */
        
        if (b1.mass >= b2.mass) {
            b1.absorb (b2);
            list.add (b2);
        }
        else {
            b2.absorb (b1);
            list.add (b1);
        }
        
        return list;
    }
}