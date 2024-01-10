

class Matrix {

    public final double x00, x01, x10, x11;

    public static final Matrix IDENTITY = new Matrix (1,0,0,1);

    public Matrix (double x00, double x01, double x10, double x11) {
        this.x00 = x00;
        this.x01 = x01;
        this.x10 = x10;
        this.x11 = x11;
    }
    
    public static Matrix rotation (double angle) {
        return new Matrix (
            Math.cos (angle), - Math.sin (angle),
            Math.sin (angle), Math.cos (angle)
        );
    }

    public static Vec rotate (Vec v, double angle) {
        return Matrix.apply (Matrix.rotation (angle), v);
    }

    public static Vec apply (Matrix m, Vec v) {
        return new Vec (
            m.x00 * v.x + m.x01 * v.y,
            m.x10 * v.x + m.x11 * v.y
        );
    }
}