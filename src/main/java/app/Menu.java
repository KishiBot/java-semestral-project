package app;

import io.github.libsdl4j.api.rect.SDL_Rect;

/**
 * Menu
 */
public class Menu {
    private static boolean running = true;
    private static boolean startCam = true;
    
    private static int hover = 0;
    private static SDL_Rect playBtn = new SDL_Rect();
    private static SDL_Rect exitBtn = new SDL_Rect();

    public static void init() {
        playBtn.x = (int)(DrawHandler.getWinSize().x * .025f);
        playBtn.y = (int)(DrawHandler.getWinSize().y * .035f);
        playBtn.w = 215;
        playBtn.h = 60;

        exitBtn.x = (int)(DrawHandler.getWinSize().x * .025f);
        exitBtn.y = (int)(DrawHandler.getWinSize().y * .135f);
        exitBtn.w = 215;
        exitBtn.h = 60;
    }

    public static boolean isRunning() {
        return running;
    }
    public static void setState(boolean _running) {
        startCam = false;
        running = _running;
    }
    public static boolean getStartCam() {
        return startCam;
    }

    public static void draw() {
        TextHandler.alignTop();
        TextHandler.alignLeft();

        String btn;
        if (hover == 1) {
            btn = (Input.leftButton.hold()) ? Ui.buttonPressed : Ui.buttonHover;
        } else {
            btn = Ui.button;
        }

        DrawHandler.drawSprite(btn, playBtn, 0, false);
        TextHandler.drawText("Play",
            new Vd2(DrawHandler.getWinSize().x * .05f, DrawHandler.getWinSize().y * .05f),
            5);

        if (hover == 2) {
            btn = (Input.leftButton.hold()) ? Ui.buttonPressed : Ui.buttonHover;
        } else {
            btn = Ui.button;
        }
        DrawHandler.drawSprite(btn, exitBtn, 0, false);
        TextHandler.drawText("Exit",
            new Vd2(DrawHandler.getWinSize().x * .05f, DrawHandler.getWinSize().y * .15f),
            5);
    }

    public static void update() {
        Vd2 mousePos = Input.getMousePos();
        if (mousePos.x >= playBtn.x && mousePos.x <= playBtn.x + playBtn.w &&
        mousePos.y >= playBtn.y && mousePos.y <= playBtn.y + playBtn.h) {
            hover = 1;
            if (Input.leftButton.release()) {
                // Press play
                running = false;
                startCam = false;
                Camera.jumpTarget();
            }
        } else if (mousePos.x >= exitBtn.x && mousePos.x <= exitBtn.x + exitBtn.w &&
        mousePos.y >= exitBtn.y && mousePos.y <= exitBtn.y + exitBtn.h) {
            hover = 2;
            if (Input.leftButton.release()) {
                // Press exit
                Engine.exit();
            }
        } else {
            hover = 0;
        }
    }
}
