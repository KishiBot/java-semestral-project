package app;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

import app.Camera.Mode;
import app.Logger.Severity;
import io.github.libsdl4j.api.keycode.SDL_Keycode;

/**
 * ObjectHandler
 */
public class ObjectHandler {

    private static ArrayList<Object> objects = new ArrayList<Object>();
    private static ArrayList<Integer> remList = new ArrayList<Integer>();
    private static ArrayList<Object> createList = new ArrayList<>();
    private static ColGrid grid = new ColGrid(new Vd2(-6656, -4096), 18944, 512);

    private static int new_id = 0x0000FFFF;
    private static boolean getLock = false;
    private static boolean getReset = false;
    private static String fileName = null;

    private static boolean toSave = false;
    private static boolean post = false;

    public static boolean isSaving() {
        return toSave;
    }
    public static void removeCoins() {
        for (Object obj : objects) {
            if (obj.getTag().equals("coin")) {
                removeObject(obj.getId());
            }
        }
    }

    public static ColGrid getGrid() {
        return grid;
    }

    /*
     * Call to check, whether ObjectHandler should reset
     */
    public static void resetUpdate() {
        if (getReset && fileName != null) {
            objects.clear();
            remList.clear();
            createList.clear();
            grid.reset();
            Ui.reset();
            Camera.setMode(Mode.singleTarget);
            loadObjects(fileName);
            DrawHandler.setFadeIn(100f);

            getReset = false;
        }
    }

    /**
     * Resets the game to savefile state<br>
     * Use when player has died<br>
     * Call resetUpdate after this
     */
    public static void reset() {
        getReset = true;
    }

    /**
     * Takes new object and appends it to objects list
     */
    public static void createObject(Object obj) {
        obj.setId(new_id++);
        createList.add(obj);
    }

    /**
     * Adds created objects to objects list
     */
    public static void addCreatedObjects() {
        for (Object obj : createList) {
            objects.add(obj);
        }
        createList.clear();
    }

    /**
     * Takes id and removes object on next remove update
     */
    public static void removeObject(int id) {
        remList.add(id);
    }

    /**
     * Should only really be used for hotswap and NOTHING else
     */
    public static void cleanObjects() {
        objects.clear();
    }

    /**
     * Does general object update (calls each objects' update)<br>
     * as well as dynamic (collision) update
     */
    public static void update() {

        // Save game state
        if (toSave && DrawHandler.getFadeOut() == 255) {
            save();
            DrawHandler.setFadeIn(300);
        }

        Vd2 mousePos = null;
        Object closest = null;
        Camera.Mode mode = Camera.getMode();

        if (Input.getKey(SDL_Keycode.SDLK_Q) && !getLock) {
            getLock = true;
            mousePos = Camera.worldPos(Input.getMousePos());
        } else if (!Input.getKey(SDL_Keycode.SDLK_Q) && getLock) {
            getLock = false;
        }

        for (Object obj : objects) {
            obj.setVelocity(new Vd2());

            // Die
            if (obj.isEntity() && obj.getHp() <= 0 && obj.isAlive()) {
                obj.onDeath();
            }

            // General update
            obj.update();

            if (obj.getGridIndex() == -1) {
                grid.add(obj);
            }

            if (obj.getCol() != null && obj.getCol().isTrigger()) {
                // Used for trigger collision checking
                for (Object target : grid.getObjs(obj)) {
                    if (target.getCol() == null) continue;
                    if (obj.getCol().simpleAABC(target.getCol())) {
                        obj.onTrigger(target);
                    }
                }
            } else if (obj.getCol() != null) {
                // Dynamic collision check for moving objects
                // Check with entities
                if  (!obj.getVelocity().isZero() || (obj.getAttCol() != null && !obj.getAttCol().isTrigger())) {
                    for (Object target : grid.getObjs(obj)) {
                        if (!target.isEntity()) continue;
                        if (!target.isAlive()) continue;
                        if (target.getCol() == null) continue;
                        if (target.getCol().isTrigger()) continue;
                        if (target.getId() == obj.getId()) continue;
                        if (!obj.getVelocity().isZero()) {
                            if (obj.dColCheck(target.getCol(), target.isEntity())) {
                                obj.onTrigger(target);
                            }
                        }

                        if (obj.getAttCol() != null && !obj.getAttCol().isTrigger()) {
                            if (obj.getAttCol().simpleAABC(target.getCol())) {
                                obj.onAttTrigger(target);
                            }
                        }
                    }

                    if (!obj.getVelocity().isZero()) {
                        // Check with solid objects
                        for (Object target : grid.getObjs(obj)) {
                            if (target.isEntity()) continue;
                            if (target.getCol() == null) continue;
                            if (target.getCol().isTrigger()) continue;
                            if (target.getId() == obj.getId()) continue;
                            if (obj.dColCheck(target.getCol(), target.isEntity())) {
                                obj.onTrigger(target);
                            }
                        }
                    }

                }
            }

            // Dynamic update
            obj.dUpdate();
            grid.update(obj);

            // Find object to lock to
            if (mousePos != null && obj.getTag().equals("enemy")) {
                if (mode == Camera.Mode.singleTarget) {
                    if (closest == null) {
                        closest = obj;
                    } else if (Vd2.sub(closest.getCol().getCenter(), mousePos).length() >
                            Vd2.sub(obj.getCol().getCenter(), mousePos).length()) {
                        closest = obj;
                    }
                }
            }

            // Draw colliders debug
            if (obj.getCol() != null && Debug.drawColliders) {
                DrawHandler.setColor(DrawHandler.Color.RED);
                DrawHandler.drawRect(Camera.cameraPos(obj.getCol().getPos()), obj.getCol().getSize(), false);
            }
            if (obj.getAttCol() != null && Debug.drawColliders) {
                DrawHandler.setColor(DrawHandler.Color.RED);
                DrawHandler.drawRect(Camera.cameraPos(obj.getAttCol().getPos()), obj.getAttCol().getSize(), false);
            }
        }

        // Lock
        if (mousePos != null) {
            if (mode == Camera.Mode.singleTarget) {
                Vd2 dist = Vd2.sub(closest.getPos(), ObjectHandler.getObject("player").getPos());
                if (dist.x < DrawHandler.getWinSize().x * .5f && dist.y < DrawHandler.getWinSize().y * .5f) {
                    Camera.setTarget(closest.getId(), true);
                    Camera.setMode(Mode.doubleTarget);
                }
            } else {
                // Unlock
                Camera.setMode(Mode.singleTarget);
            }
        }
    }

    private static Object loadObject(String[] str) {
        Object obj = null;

        // Ignore comments
        if (str[0].charAt(0) == '#') return null;
        if (str[0].equals("grid")) {
            DrawHandler.loadGrid(str);
            return null;
        }

        // Load pos and size
        Vd2 pos = null;
        Vd2 size = null;
        try {
            pos = new Vd2(Double.valueOf(str[3]), Double.valueOf(str[4]));
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "ObjectHandler", "Could not load position: " + e);
            pos = new Vd2();
        }

        try {
            size = new Vd2(Double.valueOf(str[5]), Double.valueOf(str[6]));
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "ObjectHandler", "Could not load size: " + e);
            size = new Vd2();
        }

        int i = 0;

        // Call appropriate constructor
        switch(str[1]) {
            case "Object":
            obj = new Object(str[2], pos, size, false);
            i = 7;
            break;
            case "Player":
            obj = new Player(str[2], pos, size, Integer.valueOf(str[7]));
            i = 8;
            break;
            case "Enemy":
            obj = new Enemy(str[2], pos, size, Integer.valueOf(str[7]));
            i = 8;
            break;
            case "Sword":
            obj = new Sword(str[2], pos, size);
            i = 7;
            break;
            case "Destructible":
            obj = new Destructible(str[2], pos, size, Integer.valueOf(str[7]));
            i = 8;
            break;
            case "Npc":
            obj = new Npc(str[2], pos, size, Integer.valueOf(str[7]));
            ((Npc)obj).setNpcAi(str[8]);
            i = 9;
            break;
            case "TntGoblin":
            obj = new TntGoblin(str[2], pos, size, Integer.valueOf(str[7]));
            i = 8;
            break;
            case "RedKnight":
            obj = new RedKnight(str[2], pos, size, Integer.valueOf(str[7]));
            i = 8;
            break;
            default:
            Logger.log(Severity.WARNING, "ObjectHandler", "Unknown object type: " + str[1]);
            break;
        }

        // Load additional components
        while (i < str.length) {
            switch (str[i]) {
                // Collision
                case "col":
                try {
                    Vd2 offset = new Vd2(Double.valueOf(str[i+1]), Double.valueOf(str[i+2]));
                    Vd2 colSize = new Vd2(Double.valueOf(str[i+3]), Double.valueOf(str[i+4]));
                    Collider col = new Collider(offset, colSize);
                    col.setTrigger((str[i+5].equals("true") ? true : false));
                    obj.setCollider(col);
                } catch (Exception e) {
                    Logger.log(Severity.WARNING, "ObjectHandler", "Could not load collider: " + e);
                }
                i += 6;
                break;
                // Sprite
                case "spr":
                try {
                    Vi2 strPos = new Vi2(Integer.valueOf(str[i+3]), Integer.valueOf(str[i+4]));
                    Vi2 strSize = new Vi2(Integer.valueOf(str[i+5]), Integer.valueOf(str[i+6]));
                    DrawHandler.loadSprite(str[i+1], str[i+2], strPos, strSize);
                    obj.setSprite(str[i+2]);
                } catch (Exception e) {
                    Logger.log(Severity.WARNING, "ObjectHandler", "Could not load sprite: " + e);
                }
                i += 7;
                break;
                case "attCol":
                try {
                    Vd2 offset = new Vd2(Double.valueOf(str[i+1]), Double.valueOf(str[i+2]));
                    Vd2 colSize = new Vd2(Double.valueOf(str[i+3]), Double.valueOf(str[i+4]));
                    Collider col = new Collider(offset, colSize);
                    obj.setAttCol(col);
                } catch (Exception e) {
                    Logger.log(Severity.WARNING, "ObjectHandler", "Could not load collider: " + e);
                }
                i += 5;
                break;
                case "anim":
                obj.getAnim().setRandLoop(str[i+3].equals("true") ? true : false);
                obj.getAnim().playAnim(str[i+1], (str[i+2].equals("true")) ? true : false);
                i += 4;
                break;
                case "dest":
                if (!(obj instanceof Destructible)) {
                    Logger.log(Severity.WARNING, "ObjectHandler", "Dest in non destructible object");
                    return null;
                }
                ((Destructible)obj).setDestObj(loadObject(Arrays.copyOfRange(str, i+1, str.length)));
                i = str.length;
                break;
            }
        }

        // Load id
        try {
            obj.setId(Integer.valueOf(str[0]));
            // if (Integer.valueOf(str[0]) > new_id) {
            //     new_id = Integer.valueOf(str[0])+1;
            // }
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "ObjectHandler", "Could not load object id: " + e);
            return null;
        }

        return obj;
    }

    /**
     * Loads object from file given as parameter
     */
    public static void loadObjects(String fname) {
        fileName = fname;
        File file = new File(fname);
        if (!file.exists()) {
            Logger.log(Severity.WARNING, "ObjectHandler", "Object data could not be loaded!");
            return;
        }

        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "ObjectHandler", "Could not create scanner: " + e);
            sc.close();
            return;
        }

        String line = null;
        String[] str = null;

        while(true) {
            try {
                // Get splitted line from object data file
                line = sc.nextLine();
                if (line.isEmpty()) break;
                str = line.split("\\s+");
                
                Object obj = loadObject(str);
                if (obj != null) {
                    objects.add(obj);
                }

            } catch (NoSuchElementException e) {
                break;
            } catch (Exception e) {
                Logger.log(Severity.WARNING, "ObjecHandler", "Could not load object: " + e);
            }
        }

        sc.close();
    }

    /**
     * Saves game state at current post
     */
    public static void saveState(boolean _post) {
        toSave = true;
        post = _post;
        DrawHandler.setFadeOut(300f);
    }

    /**
     * Use to save througout the game without blocking the gameplay
     */
    public static void simpleSaveState(boolean savePos) {
        TextHandler.alignLeft();
        TextHandler.alignBottom();
        TextHandler.drawText("saving..", new Vd2(10, DrawHandler.getWinSize().y - 10), 4);

        try {
            File saveFile = new File("data/save");
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }

            // Save player state
            DataOutputStream os = new DataOutputStream(new FileOutputStream(saveFile));
            if (savePos) {
                os.writeDouble(getObject("player").getPos().x);
                os.writeDouble(getObject("player").getPos().y);
                ((Player)getObject("player")).setLastSavePos(getObject("player").getPos());
            } else {
                os.writeDouble(((Player)getObject("player")).getLastSavePos().x);
                os.writeDouble(((Player)getObject("player")).getLastSavePos().y);
            }
            os.writeInt(((Player)getObject("player")).getCoinNum());
            os.writeInt(((Player)getObject("player")).getLevel());

            for (Object obj : objects) {
                if (obj instanceof Npc) {
                    // Save npc state
                    os.writeChar('n');
                    os.writeInt(obj.getId());
                    os.writeInt(((Npc)obj).getDialogSet());
                    os.writeBoolean(((Npc)obj).isFriendly());
                    os.writeBoolean(obj.isAlive());
                } else if (obj.getTag().equals("coin")) {
                    // Save dropped coins
                    os.writeChar('c');
                    os.writeDouble(obj.getPos().x);
                    os.writeDouble(obj.getPos().y);
                    os.writeInt(((Coin)obj).getCost());
                }
            }

            os.close();
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "ObjecHandler", "Could not save game state" + e);
        }

    }

    private static void resetState() {
        Camera.setMode(Camera.Mode.singleTarget);
        Camera.jumpTarget();
        Ui.setBoss(null);
        for (Object obj : objects) {
            // Remove throwables and destroyed
            if (obj.getTag().equals("destroyed") || obj.getTag().equals("throwable")) {
                removeObject(obj.getId());
                continue;
            }

            // Reset player to last save pos
            if (!(obj instanceof Player)) {
                obj.resetPos();
            } else {
                obj.setPos(((Player)obj).getLastSavePos());
                obj.setHp(obj.getMaxHp());
                obj.setAlive(true);
            }

            // Reset not npcs
            if (!(obj instanceof Npc)) {
                obj.setAlive(true);
            }

            // Reset enemies
            if (obj instanceof Enemy) {
                ((Enemy)obj).reset();
                obj.setHp(obj.getMaxHp());
            }
        }
    }

    private static void save() {
        simpleSaveState(post);
        resetState();
        post = false;
    }

    /**
     * Loads last saved state
     */
    public static void loadState() {
        try {
            File saveFile = new File("data/save");
            if (!saveFile.exists()) {
                ((Player)getObject("player")).setLastSavePos(getObject("player").getPos());
                return;
            }

            // Load player state
            DataInputStream is = new DataInputStream(new FileInputStream(saveFile));
            getObject("player").setPos(new Vd2(is.readDouble(), is.readDouble()));
            ((Player)getObject("player")).setLastSavePos(getObject("player").getPos());
            ((Player)getObject("player")).setCoinNum(is.readInt());
            ((Player)getObject("player")).setLevel(is.readInt());

            while (true) {
                try {
                    char ch = is.readChar(); 
                    if (ch == 'n') {
                        // Load Npc state
                        int id = is.readInt();
                        Npc npc = (Npc)getObject(id);
                        npc.setDialogSet(is.readInt());
                        npc.setFriendly(is.readBoolean());
                        npc.setAlive(is.readBoolean());

                        if (!npc.isAlive()) {
                            npc.simulateDeath();
                        }
                        if (!npc.isFriendly()) {
                            npc.setTag("enemy");
                        }
                    } else if (ch == 'c') {
                        // Load dropped coins
                        Player.dropCoin(new Vd2(is.readDouble(), is.readDouble()), 0, is.readInt(), "coin");
                    }
                } catch (EOFException eof) {
                    // Reached eof
                    break;
                }
            }

            is.close();
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "ObjecHandler", "Could not load game state" + e);
        }
        resetState();
    }
    

    /**
     * Should only be used for passing object list to DrawHandler
     */
    public static ArrayList<Object> getObjectList() {
        return objects;
    }

    /**
     * Removes objects set to be removed from the
     * objects list <br>
     * Should be called after update is called
     */
    public static void remUpdate() {
        for (int i : remList) {
            Object obj = getObject(i);
            if (obj != null) {
                grid.remObj(obj, obj.getGridIndex());
                objects.remove(obj);
            }
        }
    }

    /**
     * Returns reference to object based on id<br>
     * If object with corresponding id does not exist,
     * null is returned;
     */
    public static Object getObject(int id) {
        for (Object obj : objects) {
            if (obj.getId() == id) return obj;
        }
        return null;
    }

    /**
     * Returns reference to object based on tag<br>
     * If object with corresponding tag does not exist,
     * null is returned;
     */
    public static Object getObject(String tag) {
        for (Object obj: objects) {
            if (obj.getTag().equals(tag)) return obj;
        }
        return null;
    }
}
