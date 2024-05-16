package app;

/**
 * Int 2d vector
 */
public class Vi2 {

    public int x, y;

    // Constructors
    public Vi2() {
        x = 0;
        y = 0;
    }
    public Vi2(Vf2 a) {
        x = (int)a.x;
        y = (int)a.y;
    }
    public Vi2(Vd2 a) {
        x = (int)a.x;
        y = (int)a.y;
    }
    public Vi2(Vi2 a) {
        x = a.x;
        y = a.y;
    }
    public Vi2(int _x, int _y) {
        x = _x;
        y = _y;
    }

    public void add(Vi2 a) {
        x += a.x;
        y += a.y;
    }
    public void sub(Vi2 a) {
        x -= a.x;
        y -= a.y;
    }
    public void mult(int n) {
        x *= n;
        y *= n;
    }
    public void mult(Vi2 a) {
        x *= a.x;
        y *= a.y;
    }
    public void div(Vi2 a) {
        x /= a.x;
        y /= a.y;
    }
    public void div(int n) {
        x /= n;
        y /= n;
    }

    public int length() {
        return (int)Math.sqrt(x*x + y*y);
    }

    public void normalize() {
        int magn = length();
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

    public void set(Vi2 a) {
        x = a.x;
        y = a.y;
    }

    public boolean isZero() {
        if (x == 0 && y == 0) return true;
        return false;
    }

    // Static methods
    public static Vi2 add(Vi2 a, Vi2 b) {
        return new Vi2(a.x + b.x, a.y + b.y);
    }
    public static Vi2 sub(Vi2 a, Vi2 b) {
        return new Vi2(a.x - b.x, a.y - b.y);
    }
    public static Vi2 div(Vi2 a, int n) {
        return new Vi2(a.x / n, a.y / n);
    }
    public static Vi2 div(Vi2 a, Vi2 b) {
        return new Vi2(a.x / b.x, a.y / b.y);
    }
    public static Vi2 mult(Vi2 a, Vi2 b) {
        return new Vi2(a.x * b.x, a.y * b.y);
    }
    public static Vi2 mult(Vi2 a, int n) {
        return new Vi2(a.x * n, a.y * n);
    }
    public static Vi2 pow(Vi2 a, int n) {
        return new Vi2((int)Math.pow(a.x, n), (int)Math.pow(a.y, n));
    }
    public static Vi2 abs(Vi2 a) {
        return new Vi2(Math.abs(a.x), Math.abs(a.y));
    }
    public static Vi2 normalized(Vi2 a) {
        int magn = a.length();
        if (magn == 0) return new Vi2(a.x, a.y);
        return new Vi2(a.x/magn, a.y/magn);
    }
}
