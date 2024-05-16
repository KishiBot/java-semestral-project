package app;

import io.github.libsdl4j.api.rect.SDL_Rect;

/**
 * Sprite
 */
public class Sprite {
    private String text = null;
    private SDL_Rect sRect = null;

    public Sprite(String _text, SDL_Rect _sRect) {
        text = _text;
        sRect = _sRect;
    }

    public String getText() {
        return text;
    }

    public SDL_Rect getSRect() {
        return sRect;
    }
}
