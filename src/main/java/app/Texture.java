package app;

import io.github.libsdl4j.api.render.SDL_Texture;

/**
 * Texture
 */
public class Texture {

    private SDL_Texture text = null;
    private Vi2 size = null;

    public void setText(SDL_Texture _text) {
        text = _text;
    }
    public void setSize(Vi2 _size) {
        size = _size;
    }

    public Vi2 getSize() {
        return new Vi2(size);
    }

    public SDL_Texture getText() {
        return text;
    }
}
