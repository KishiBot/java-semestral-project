package app;

/**
 * Float 2d vector
 */
public class Vf2 {

    public float x, y;

    // Constructors
    public Vf2() {
        x = 0;
        y = 0;
    }
    public Vf2(Vf2 a) {
        x = a.x;
        y = a.y;
    }
    public Vf2(Vd2 a) {
        x = (float)a.x;
        y = (float)a.y;
    }
    public Vf2(float _x, float _y) {
        x = _x;
        y = _y;
    }

    public void add(Vf2 a) {
        x += a.x;
        y += a.y;
    }
    public void sub(Vf2 a) {
        x -= a.x;
        y -= a.y;
    }
    public void mult(float n) {
        x *= n;
        y *= n;
    }
    public void mult(Vf2 a) {
        x *= a.x;
        y *= a.y;
    }
    public void div(Vf2 a) {
        x /= a.x;
        y /= a.y;
    }
    public void div(float n) {
        x /= n;
        y /= n;
    }

    public float length() {
        return (float)Math.sqrt(x*x + y*y);
    }

    public void normalize() {
        float magn = length();
        if (magn == 0) return;
        x /= magn;
        y /= magn;
    }

    public void abs() {
        x = Math.abs(x);
        y = Math.abs(y);
    }

    public void zero() {
        x = 0;
        y = 0;
    }

    public boolean isZero() {
        if (x == 0 && y == 0) return true;
        return false;
    }

    // Static methods
    public static Vf2 add(Vf2 a, Vf2 b) {
        return new Vf2(a.x + b.x, a.y + b.y);
    }
    public static Vf2 sub(Vf2 a, Vf2 b) {
        return new Vf2(a.x - b.x, a.y - b.y);
    }
    public static Vf2 div(Vf2 a, float n) {
        return new Vf2(a.x / n, a.y / n);
    }
    public static Vf2 div(Vf2 a, Vf2 b) {
        return new Vf2(a.x / b.x, a.y / b.y);
    }
    public static Vf2 mult(Vf2 a, Vf2 b) {
        return new Vf2(a.x * b.x, a.y * b.y);
    }
    public static Vf2 mult(Vf2 a, float n) {
        return new Vf2(a.x * n, a.y * n);
    }
    public static Vf2 abs(Vf2 a) {
        return new Vf2(Math.abs(a.x), Math.abs(a.y));
    }
    public static Vf2 normalized(Vf2 a) {
        float magn = a.length();
        if (magn == 0) return new Vf2(a.x, a.y);
        return new Vf2(a.x/magn, a.y/magn);
    }
}
