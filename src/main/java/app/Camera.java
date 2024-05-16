package app;

/**
 * Camera
 */
public class Camera {

    public static enum Mode {
        singleTarget,
        doubleTarget
    }
    private static Mode mode = Mode.singleTarget;

    // Target id
    private static int target = -1;
    private static int secondTarget = -1;

    private static Vd2 pos = new Vd2();
    private static Vf2 deadZone = new Vf2(700, 400);
    private static boolean activeLerp = false;

    // SingleTarget pos tracking
    private static Vd2 tarPos = new Vd2();
    private static Vd2 curOff = null;

    public static Vd2 getPos() {
        return pos;
    }

    /**
     * Takes camera position and returns world position
     */
    public static Vd2 worldPos(Vd2 objPos) {
        return Vd2.add(new Vd2(pos), objPos);
    }

    /**
     * Takes world position and returns camera position
     */
    public static Vd2 cameraPos(Vd2 objPos) {
        return Vd2.sub(objPos, pos);
    }

    /**
     * Returns second target's id
     */
    public static int getTarget() {
        return secondTarget;
    }

    /**
     * Sets camera's primary target<br>
     * Takes in object's id
     */
    public static void setTarget(int id) {
        target = id;
    }

    /**
     * Jumps to primary target without any smoothing
     */
    public static void jumpTarget() {
        Object targetObj = ObjectHandler.getObject(target);
        if (targetObj == null) return;

        Vd2 newPos = new Vd2(targetObj.getCol().getCenter());
        pos = Vd2.sub(newPos, Vd2.mult(new Vd2(DrawHandler.getWinSize()), .5f));
        tarPos = newPos;
        curOff = newPos;
    }

    /**
     * Sets either camera's primary target or secondary target<br>
     * Parameter second being true means setting secondary target<br>
     * Takes in object's id
     */
    public static void setTarget(int id, boolean second) {
        if (second) {
            secondTarget = id;
        }
    }

    /**
     * Sets camera's target mode
     */
    public static void setMode(Mode _mode) {
        mode = _mode;
    }

    public static Mode getMode() {
        return mode;
    }

    private static void updateSingleMode(Object targetObj) {
        if (targetObj == null) {
            return;
        }

        Vd2 newPos = new Vd2(targetObj.getCol().getCenter());
        Vd2 checkPos = Camera.cameraPos(newPos);

        // Calculate dead zone and target object position difference
        if (checkPos.x < deadZone.x ||
        checkPos.x > DrawHandler.getWinSize().x - deadZone.x ||
        checkPos.y < deadZone.y ||
        checkPos.y > DrawHandler.getWinSize().y - deadZone.y) {
            activeLerp = true;
        }

        Vd2 diff = Vd2.sub(newPos, tarPos);
        Vd2 dest = Vd2.add(newPos, Vd2.mult(Vd2.normalized(diff), 200)); 

        if (activeLerp) {
            if (diff.isZero()) {
                activeLerp = false;
            }

            curOff = Vd2.lerp(curOff, dest, 2.5f * Time.DeltaTime());

            if (Debug.drawCamLookAheadPoint) {
                DrawHandler.setColor(DrawHandler.Color.BLUE);
                DrawHandler.drawRect(Vd2.sub(Camera.cameraPos(curOff), new Vd2(10, 10)), new Vd2(20, 20), true);
            }


            pos = Vd2.sub(curOff, Vd2.mult(new Vd2(DrawHandler.getWinSize()), .5f));
        }


        tarPos = newPos;
    }

    private static void updateDoubleMode() {
        Object tar1 = ObjectHandler.getObject(target);
        Object tar2 = ObjectHandler.getObject(secondTarget);

        // Check whether objects exist
        if (tar1 == null && tar2 != null) {
            updateSingleMode(tar2);
            setMode(Mode.singleTarget);
            return;
        } else if (tar1 != null && tar2 == null) {
            updateSingleMode(tar1);
            setMode(Mode.singleTarget);
            return;
        } else if (tar1 == null && tar2 == null) {
            setMode(Mode.singleTarget);
            return;
        } else if (tar1 != null && !tar2.alive) {
            setMode(Mode.singleTarget);
            return;
        }

        // Calculate targets' center
        Vd2 pos1 = tar1.getCol().getCenter();
        Vd2 pos2 = tar2.getCol().getCenter();

        // Calculate new position
        Vd2 objPos = Vd2.add(Vd2.div(Vd2.sub(pos1, pos2), 2), pos2);
        curOff = Vd2.lerp(curOff, objPos, 1.6f * Time.DeltaTime());
        pos = Vd2.sub(curOff, Vd2.div(new Vd2(DrawHandler.getWinSize()), 2));

        if (Debug.drawCameraDoubleModeLine) {
            DrawHandler.setColor(DrawHandler.Color.GREEN);
            DrawHandler.drawLine(new Vf2(cameraPos(Vd2.add(tar1.getPos(), Vd2.div(tar1.getSize(), 2)))),
                    new Vf2(cameraPos(Vd2.add(tar2.getPos(), Vd2.div(tar2.getSize(), 2)))));
        }
    }

    /**
     * Update camera depending on set mode
     */
    public static void update() {
        if (Menu.getStartCam()) {
            pos.x = -300;
            pos.y = 50;
            return;
        }

        // Debug
        if (Debug.drawCenterLines) {
            DrawHandler.setColor(DrawHandler.Color.GREEN);
            DrawHandler.drawLine(new Vf2(DrawHandler.getWinSize().x / 2, 0), new Vf2(DrawHandler.getWinSize().x / 2, DrawHandler.getWinSize().y));
            DrawHandler.drawLine(new Vf2(0, DrawHandler.getWinSize().y/2), new Vf2(DrawHandler.getWinSize().x, DrawHandler.getWinSize().y/2));
        }
        if (Debug.drawDeadZone) {
            DrawHandler.setColor(DrawHandler.Color.color(255, 0, 0, 75));
            DrawHandler.drawRect(new Vd2(), new Vd2(DrawHandler.getWinSize().x, deadZone.y), true);
            DrawHandler.drawRect(new Vd2(0, deadZone.y), new Vd2(deadZone.x, DrawHandler.getWinSize().y), true);
            DrawHandler.drawRect(new Vd2(DrawHandler.getWinSize().x - deadZone.x, deadZone.y), new Vd2(deadZone.x, DrawHandler.getWinSize().y), true);
            DrawHandler.drawRect(new Vd2(deadZone.x, DrawHandler.getWinSize().y - deadZone.y),
                    new Vd2(DrawHandler.getWinSize().x - deadZone.x*2, deadZone.y), true);
        }

        if (mode == Mode.singleTarget) {
            updateSingleMode(ObjectHandler.getObject(target));
        } else if (mode == Mode.doubleTarget) {
            updateDoubleMode();
        }
    }
}
