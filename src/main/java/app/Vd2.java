package app;

/**
 * Double 2d vector
 */
public class Vd2 {

    public double x, y;

    // Constructors
    public Vd2() {
        x = 0;
        y = 0;
    }
    public Vd2(Vf2 a) {
        x = (double)a.x;
        y = (double)a.y;
    }
    public Vd2(Vd2 a) {
        x = a.x;
        y = a.y;
    }
    public Vd2(double _x, double _y) {
        x = _x;
        y = _y;
    }

    public void add(Vd2 a) {
        x += a.x;
        y += a.y;
    }
    public void sub(Vd2 a) {
        x -= a.x;
        y -= a.y;
    }
    public void mult(double n) {
        x *= n;
        y *= n;
    }
    public void mult(Vd2 a) {
        x *= a.x;
        y *= a.y;
    }
    public void div(Vd2 a) {
        x /= a.x;
        y /= a.y;
    }
    public void div(double n) {
        x /= n;
        y /= n;
    }

    public double length() {
        return (double)Math.sqrt(x*x + y*y);
    }

    public void normalize() {
        double magn = length();
        if (magn == 0) return;
        x /= magn;
        y /= magn;
    }

    public void rotate(double angle) {
        angle = Math.toRadians(-angle);
        double newX = x * Math.cos(angle) - y * Math.sin(angle);
        double newY = x * Math.sin(angle) + y * Math.cos(angle);
        x = newX;
        y = newY;
    }

    public void abs() {
        x = Math.abs(x);
        y = Math.abs(y);
    }

    /**
     * Sets both x and y to zero
     */
    public void zero() {
        x = 0;
        y = 0;
    }

    public void set(Vd2 a) {
        x = a.x;
        y = a.y;
    }

    public boolean isZero() {
        if (x == 0 && y == 0) return true;
        return false;
    }

    // Static methods
    public static Vd2 add(Vd2 a, Vd2 b) {
        return new Vd2(a.x + b.x, a.y + b.y);
    }
    public static Vd2 add(Vd2 a, double b) {
        return new Vd2(a.x + b, a.y + b);
    }
    public static Vd2 sub(Vd2 a, Vd2 b) {
        return new Vd2(a.x - b.x, a.y - b.y);
    }
    public static Vd2 sub(Vd2 a, Double b) {
        return new Vd2(a.x - b, a.y - b);
    }
    public static Vd2 div(Vd2 a, double n) {
        return new Vd2(a.x / n, a.y / n);
    }
    public static Vd2 div(Vd2 a, Vd2 b) {
        return new Vd2(a.x / b.x, a.y / b.y);
    }
    public static Vd2 mult(Vd2 a, Vd2 b) {
        return new Vd2(a.x * b.x, a.y * b.y);
    }
    public static Vd2 mult(Vd2 a, double n) {
        return new Vd2(a.x * n, a.y * n);
    }
    public static Vd2 pow(Vd2 a, double n) {
        return new Vd2(Math.pow(a.x, n), Math.pow(a.y, n));
    }
    public static Vd2 abs(Vd2 a) {
        return new Vd2(Math.abs(a.x), Math.abs(a.y));
    }
    public static Vd2 normalized(Vd2 a) {
        double magn = a.length();
        if (magn == 0) return new Vd2(a.x, a.y);
        return new Vd2(a.x/magn, a.y/magn);
    }
    public static double angle(Vd2 a, Vd2 b) {
        double ret = -Math.toDegrees(Math.atan2(b.y - a.y, b.x - a.x));
        if (ret < 0) {
            ret = 360 + ret;
        }
        return ret;
    }

    public static Vd2 lerp(Vd2 a, Vd2 b, double step) {
        double retX = MathFuncs.lerp(a.x, b.x, step);
        double retY = MathFuncs.lerp(a.y, b.y, step);
        return new Vd2(retX, retY);
    }
}
