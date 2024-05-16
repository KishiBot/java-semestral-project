package app;

/**
 * Math
 */
public class MathFuncs {

    public static float norm(float x, float max) {
        return x / max;
    }

    public static float easeOutSin(float x) {
        return (float)Math.sin((x*Math.PI)/2);
    }
    public static double easeOutSin(double x) {
        return Math.sin((x*Math.PI)/2);
    }

    public static double easeOutCubic(double x) {
        return 1 - Math.pow(1 - x, 3);
    }
    public static double test(double x) {
        return Math.sin((x/2)*Math.PI);
    }

    public static double lerp(double a, double b, double step) {
        return a * (1 - step) + b * step;
    }

    public static int randInt(int max) {
        return (int)(Math.random()*max);
    }
}
