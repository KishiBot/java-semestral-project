package app;

import io.github.libsdl4j.api.rect.SDL_Rect;

/**
 * Ui
 */
public class Ui {

    private static Player player = null;
    private static final String text = "UI";
    private static Object boss = null;

    // Button sprites
    public final static String buttonPressed = "buttonPressed";
    public final static String buttonHover = "buttonHover";
    public final static String button = "button";
    public final static String tip = "tip";
    public final static String dialogBanner = "dialogBanner";

    // Dialog timing
    private static int dialogIndex = 0;
    private static float indexTimer = 0f;
    private static final float charLength = .05f;
    private static final float indexStart = -1f;
    private static float messageTimer = 0f;
    private static final float messageDelay = 2f;

    // Dialog
    private static MessageType type = MessageType.DIALOG;
    private static Object messageObj = null;
    private static String dialog = null;
    private static String name = null;
    private static Vd2 dialogPos = null;
    private static Vd2 dialogSize = new Vd2(DrawHandler.getWinSize().x,
                    DrawHandler.getWinSize().y * .6f);
    private static boolean dialogPresent = false;

    private static final Vd2 startDialogPos = new Vd2(0, DrawHandler.getWinSize().y + dialogSize.y);

    private static final Vd2 endDialogPos = new Vd2(
        startDialogPos.x,
        DrawHandler.getWinSize().y * .5f);

    public static void Init() {
        DrawHandler.loadSprite(text,
                tip,
                new Vi2(128, 0),
                new Vi2(64, 64));
        DrawHandler.loadSprite(text,
                buttonPressed,
                new Vi2(256, 0),
                new Vi2(64, 64));
        DrawHandler.loadSprite(text,
            buttonHover,
            new Vi2(192, 0),
            new Vi2(64, 64));
        DrawHandler.loadSprite(text,
            button,
            new Vi2(64, 0),
            new Vi2(64, 64));
        DrawHandler.loadSprite(text,
                dialogBanner,
                new Vi2(0, 64),
                new Vi2(192, 192));
    }

    public static void setBoss(Object obj) {
        boss = obj;
    }

    public static void reset() {
        boss = null;
        dialog = null;
        player = null;
    }

    public static void draw() {
        if (dialog != null) {
            if (type == MessageType.DIALOG) {
                updateDialog();
            } else if (type == MessageType.MESSAGE) {
                updateMessage();
            }
        }

        if (player == null) {
            player = (Player)ObjectHandler.getObject("player");
        }
        if (player != null) {

            // Draw you died when player is dead
            if (!player.isAlive()) {
                TextHandler.alignTop();
                TextHandler.alignCenter();
                TextHandler.drawText("You died", new Vd2(Camera.cameraPos(new Vd2(player.getPos().x + player.getSize().x * .5f, player.getPos().y+20))), 10);
            }

            TextHandler.alignTop();
            TextHandler.alignRight();
            TextHandler.drawText(
                    "Coins: " + player.getCoinNum(),
                    new Vd2(DrawHandler.getWinSize().x - 10, 10), 4);


            DrawHandler.setColor(DrawHandler.Color.GREY);
            DrawHandler.drawRect(
                new Vd2(10, 10),
                new Vd2(200f, 20),
                true);
            DrawHandler.setColor(DrawHandler.Color.RED);
            if (player.getHp() > 0) {
                DrawHandler.drawRect(
                    new Vd2(10, 10),
                    new Vd2((200f / player.getMaxHp()) * player.getHp(), 20),
                    true);
            }

            DrawHandler.setColor(DrawHandler.Color.GREY);
            DrawHandler.drawRect(
                new Vd2(10, 40),
                new Vd2(150f, 20),
                true);
            DrawHandler.setColor(DrawHandler.Color.GREEN);
            DrawHandler.drawRect(
                new Vd2(10, 40),
                new Vd2((150f / player.getMaxStam()) * player.getStam(), 20),
                true);
        }

        if (boss != null) {
            DrawHandler.setColor(DrawHandler.Color.GREY);
            DrawHandler.drawRect(
                new Vd2(200, DrawHandler.getWinSize().y - 30),
                new Vd2(DrawHandler.getWinSize().x - 400, 20),
                true);
            DrawHandler.setColor(DrawHandler.Color.RED);
            if (player.getHp() > 0) {
                DrawHandler.drawRect(
                    new Vd2(200, DrawHandler.getWinSize().y - 30),
                    new Vd2(((DrawHandler.getWinSize().x - 400) / boss.getMaxHp()) * boss.getHp(), 20),
                    true);
            }

            TextHandler.alignBottom();
            TextHandler.alignLeft();
            TextHandler.drawText(((Npc)boss).getName(),
                    new Vd2(200, DrawHandler.getWinSize().y - 35), 5);
        }
    }

    /**
     * Draws press E suggestion above npcs
     */
    public static void drawNpcE(Vd2 pos) {
        Vd2 camPos = Camera.cameraPos(pos);

        SDL_Rect dstRect = new SDL_Rect();
        dstRect.w = 50;
        dstRect.h = 50;
        dstRect.x = (int)Math.round(camPos.x-dstRect.w/2f);
        dstRect.y = (int)Math.round(camPos.y-dstRect.h/4f);
        DrawHandler.drawSprite(tip, dstRect, 0, false);

        TextHandler.alignTop();
        TextHandler.alignLeft();
        TextHandler.drawText("E", new Vd2(dstRect.x+dstRect.w/3f, dstRect.y+dstRect.w/3.25f), 3);
    }

    /**
     * Starts printing dialog
     */
    public static void drawDialog(String _dialog, String _name) {
        type = MessageType.DIALOG;
        name = _name;
        dialog = _dialog;
        if (dialog == null) {
            dialogPresent = false;
            dialogPos = null;
            return;
        }
        if (!dialogPresent) {
            dialogPresent = true;
            dialogPos = new Vd2(startDialogPos);
            indexTimer = indexStart;
        } else {
            indexTimer = 0;
        }

        dialogIndex = 0;
    }

    /**
     * Skips dialog to its end and returns false<br>
     * or returns true meaning the dialog has already ended
     */
    public static boolean skipDialog() {
        if (dialogIndex == dialog.length()) return true;
        dialogIndex = dialog.length();
        return false;
    }

    private static void generalUpdate() {
        // Text index
        if (indexTimer >= charLength) {
            indexTimer = 0;
            dialogIndex = (dialog.length() > dialogIndex) ? dialogIndex+1 : dialogIndex;
        } else {
            indexTimer += Time.DeltaTime();
        }
    }

    private static void updateDialog() {
        // Move dialog up
        if (dialogPos == null) return;
        dialogPos.y -= 1500*Time.DeltaTime()*MathFuncs.easeOutSin((dialogPos.y - endDialogPos.y) / (startDialogPos.y - endDialogPos.y));

        // Draw banner
        SDL_Rect dstRect=  new SDL_Rect();
        dstRect.x = (int)Math.round(dialogPos.x);
        dstRect.y = (int)Math.round(dialogPos.y);
        dstRect.w = (int)Math.round(dialogSize.x);
        dstRect.h = (int)Math.round(dialogSize.y);
        DrawHandler.drawSprite(dialogBanner, dstRect, 0, false);

        // Draw name
        if (name != null) {
            TextHandler.alignLeft();
            TextHandler.alignBottom();
            TextHandler.drawText(name,
                new Vd2(dialogPos.x + DrawHandler.getWinSize().x * .3f,
                dialogPos.y + DrawHandler.getWinSize().y * .14f),
                5);
        }

        // Draw text
        TextHandler.alignLeft();
        TextHandler.alignTop();
        TextHandler.setLimit(dialogIndex);
        TextHandler.drawText(dialog,
                new Vd2(dialogPos.x + DrawHandler.getWinSize().x * .3f,
                    dialogPos.y + DrawHandler.getWinSize().y * .2f),
                5);
        TextHandler.setLimit(-1);

        generalUpdate();
    }

    private static void updateMessage() {
        dialogPos = Camera.cameraPos(new Vd2(
            messageObj.getCol().getCenter().x,
            messageObj.getCol().getPos().y - 40f
        ));
        generalUpdate();

        // Draw text
        TextHandler.alignCenter();
        TextHandler.alignTop();
        TextHandler.setLimit(dialogIndex);
        TextHandler.drawText(dialog, dialogPos, 3);
        TextHandler.setLimit(-1);

        if (dialogIndex != dialog.length()) return;

        messageTimer -= Time.DeltaTime();
        if (messageTimer <= 0) {
            dialog = null;
            dialogPos = null;
        }
    }

    public static void drawMessage(String message, Object obj) {
        type = MessageType.MESSAGE;
        messageObj = obj;
        dialog = message;
        dialogIndex = 0;
        messageTimer = messageDelay;
    }

    private enum MessageType {
        DIALOG,
        MESSAGE
    }
}

