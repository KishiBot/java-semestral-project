package app;

/**
 * Keeps track of deltatime and runtime
 */
public class Time {
    private static float deltaTime = 0f;
    private static final float minDT = 1e-6f;
    private static float runtime = 0f;
    private static long prgStart = 0;
    private static long lastFrame = 0;

    // Fps
    private static float fpsCounter = 0;
    private static int curFps = 0;
    private static int fps = 0;

    private static final float targetDT = 1f/60f;

    /**
     * Returns this frame's deltatime
     */
    public static float DeltaTime() {
        if (deltaTime > .5f) return .5f;
        return deltaTime;
    }

    /**
     * Returns current runtime
     */
    public static float CurTime() {
        return runtime;
    }

    public static int getFps() {
        return curFps;
    }

    /**
     * Updates deltatime and runtime
     */
    public static void update() {
        if (prgStart == 0) {
            prgStart = System.currentTimeMillis();
        }
        long cur_frame = System.currentTimeMillis() - prgStart;
        deltaTime = (float)(cur_frame - lastFrame) / 1000f;
        if (deltaTime < minDT) deltaTime = minDT;

        while (deltaTime < targetDT) {
            cur_frame = System.currentTimeMillis() - prgStart;
            deltaTime = (float)(cur_frame - lastFrame) / 1000f;
        }

        lastFrame = cur_frame;
        runtime += deltaTime;

        if (fpsCounter < 1) {
            fpsCounter += deltaTime;
            ++fps;
        } else {
            curFps = fps;
            fps = 0;
            fpsCounter = 0;
        }
    }
}
