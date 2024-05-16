package app;

import static io.github.libsdl4j.api.render.SdlRender.SDL_CreateTextureFromSurface;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderCopy;
import static io.github.libsdl4j.api.surface.SdlSurface.SDL_LoadBMP;

import io.github.libsdl4j.api.rect.SDL_Rect;
import io.github.libsdl4j.api.render.SDL_Renderer;
import io.github.libsdl4j.api.render.SDL_Texture;
import io.github.libsdl4j.api.surface.SDL_Surface;

/**
 * TextHandler
 */
public class TextHandler {
    public static enum Alignment {
        RIGHT,
        LEFT,
        TOP,
        BOTTOM,
        CENTER
    }

    private static SDL_Texture fontText = null;
    private static SDL_Renderer renderer = null;
    private static Alignment hAlign = Alignment.LEFT;
    private static Alignment vAlign = Alignment.TOP;
    private static int textLim = -1;

    public static void init(SDL_Renderer _renderer) {
        renderer = _renderer;
        SDL_Surface surf = SDL_LoadBMP("resources/bmp/Font.bmp");
        fontText = SDL_CreateTextureFromSurface(renderer, surf);
    }

    /**
     * Sets alignment from right
     */
    public static void alignRight() {
        hAlign = Alignment.RIGHT;
    }
    /**
     * Sets alignment from left
     */
    public static void alignLeft() {
        hAlign = Alignment.LEFT;
    }

    /**
     * Sets alignment from top
     */
    public static void alignTop() {
        vAlign = Alignment.TOP;
    }
    /**
     * Sets alignment from bottom
     */
    public static void alignBottom() {
        vAlign = Alignment.BOTTOM;
    }

    public static void alignCenter() {
        hAlign = Alignment.CENTER;
    }

    /**
     * Sets limit as to how many characters of a string can
     * be written to the screen.
     */
    public static void setLimit(int limit) {
        textLim = limit;
    }

    private static boolean isChar(int ch) {
        if (ch >= 65 && ch <= 90) return true;
        return false;
    }

    private static boolean isNum(int ch) {
        if (ch >= 48 && ch <= 57) return true;
        return false;
    }

    private static int isSpecial(int ch) {
        switch (ch) {
            case (int)':':
                return 0;
            case (int)'!':
                return 1;
            case (int)'?':
                return 2;
            case (int)',':
                return 3;
            case (int)'.':
                return 4;
            default:
                return -1;
        }
    }

    public static void drawText(String txt, Vd2 pos, float scale) {
        int ch;
        txt = txt.toUpperCase();

        SDL_Rect sRect = new SDL_Rect();
        SDL_Rect dstRect = new SDL_Rect();
        dstRect.x = (int)Math.round(pos.x);
        dstRect.y = (int)Math.round(pos.y);

        int lim = (txt.length() > textLim && textLim != -1) ? textLim : txt.length();

        if (hAlign == Alignment.CENTER) {
            dstRect.x = (int)Math.round(pos.x - 5*scale*(lim/2));
        }

        for (int i = 0; i < lim; ++i) {
            if (hAlign == Alignment.LEFT || hAlign == Alignment.CENTER) {
                ch = (int)txt.charAt(i);
            } else {
                ch = (int)txt.charAt(txt.length()-i-1);
            }

            // New line
            if (ch == '\n') {
                dstRect.y += dstRect.h + dstRect.h * .5f;
                if (hAlign == Alignment.CENTER) {
                    dstRect.x = (int)Math.round(pos.x - 5*scale*(txt.length()/2));
                } else {
                    dstRect.x = (int)Math.round(pos.x);
                }
                continue;
            }

            if (isChar(ch)) {
                ch -= 65;
                sRect.x = (ch % 13) * 5;
                sRect.y = (ch / 13) * 5;
                sRect.w = 4;
                sRect.h = 5;

                dstRect.w = (int)Math.round(5 * scale);
                dstRect.h = (int)Math.round(5 * scale);
            } else if (isNum(ch)) {
                ch -= 48;
                sRect.x = ch * 4;
                sRect.y = 10;
                sRect.w = 3;
                sRect.h = 5;

                dstRect.w = (int)Math.round(4 * scale);
                dstRect.h = (int)Math.round(5 * scale);
            } else {
                ch = isSpecial(ch);
                if (ch > -1) {
                    sRect.x = ch * 3;
                    sRect.y = 15;
                    sRect.w = 2;
                    sRect.h = 5;

                    dstRect.w = (int)Math.round(5 * scale);
                    dstRect.h = (int)Math.round(5 * scale);
                } else {
                    sRect.x = 63;
                    sRect.y = 63;
                    sRect.w = 4;
                    sRect.h = 5;

                    dstRect.w = (int)Math.round(5 * scale);
                    dstRect.h = (int)Math.round(5 * scale);
                }
            }

            if (i == 0 && hAlign == Alignment.RIGHT) {
                dstRect.x -= dstRect.w;
            }
            if (i == 0 && vAlign == Alignment.BOTTOM) {
                dstRect.y -= dstRect.h;
            }

            SDL_RenderCopy(renderer, fontText, sRect, dstRect);

            if (hAlign == Alignment.LEFT || hAlign == Alignment.CENTER) {
                dstRect.x += dstRect.w * 1.2f;
            } else if (hAlign == Alignment.RIGHT) {
                dstRect.x -= dstRect.w * 1.2f;
            } else if (hAlign == Alignment.CENTER) {
                dstRect.x -= dstRect.w * .6f;
            }
        }
    }
}
