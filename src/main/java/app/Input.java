package app;

import static io.github.libsdl4j.api.keycode.SDL_Keycode.*;

import java.util.HashMap;
import java.util.Map;

import io.github.libsdl4j.api.keyboard.SDL_Keysym;
import io.github.libsdl4j.api.mouse.SDL_Button;

/**
* Input manager
*/
public class Input {
    private static Map<Integer, Boolean> keyboard = new HashMap<Integer, Boolean>() {{
        put(SDLK_W, false);
        put(SDLK_S, false);
        put(SDLK_A, false);
        put(SDLK_D, false);
    }};
    
    /**
     * Holds information about mouse's left button's state
     */
    public static MouseButton leftButton = new MouseButton();

    /**
     * Holds information about mouse's right button's state
     */
    public static MouseButton rightButton = new MouseButton();

    private static Vd2 mousePos = new Vd2();

    public static void setKeyDown(SDL_Keysym sym) {
        keyboard.put(sym.sym, true);
    }
    public static void setKeyUp(SDL_Keysym sym) {
        keyboard.put(sym.sym, false);
    }

    /**
     * Returns whether key is held down
     */
    public static boolean getKey(int key) {
        if (!keyboard.containsKey(key)) {
            keyboard.put(key, false);
        }
        return keyboard.get(key);
    }

    /**
     * Sets mouse button as pressed<br>
     * Should only be used in accordance to SDL events
     */
    public static void setMouseDown(int button) {
        if (button == SDL_Button.SDL_BUTTON_LEFT) {
            leftButton.setState(true);
        } else if (button == SDL_Button.SDL_BUTTON_RIGHT) {
            rightButton.setState(true);
        }
    }

    /**
     * Sets mouse button as released<br>
     * Should only be used in accordance to SDL events
     */
    public static void setMouseUp(int button) {
        if (button == SDL_Button.SDL_BUTTON_LEFT) {
            leftButton.setState(false);
        } else if (button == SDL_Button.SDL_BUTTON_RIGHT) {
            rightButton.setState(false);
        }
    }

    /**
     * Sets mouse position<br>
     * Should only be used in accordance to SDL events
     */
    public static void setMousePos(Vd2 pos) {
        mousePos = new Vd2(
            pos.x * (DrawHandler.getWinSize().x / Engine.getWinSize().x),
            pos.y * (DrawHandler.getWinSize().y / Engine.getWinSize().y));
    }
    public static Vd2 getMousePos() {
        return new Vd2(mousePos.x, mousePos.y);
    }
}

class MouseButton {
    private boolean down = false;
    private float timePressed = 0f;
    private static final float pressLimit = .15f;

    public boolean hold() {
        return down;
    }
    public boolean press() {
        if (down && Time.CurTime() - timePressed < pressLimit) return true;
        return false;
    }
    public boolean release() {
        if (Time.CurTime() < 1f) return false;
        if (!down && Time.CurTime() - timePressed < pressLimit) {
            timePressed = 0;
            return true;
        }
        return false;
    }
    public void setState(boolean press) {
        timePressed = Time.CurTime();
        down = press;
    }
}
