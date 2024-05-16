package app;

import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.render.SdlRender.SDL_CreateTexture;
import static io.github.libsdl4j.api.render.SdlRender.SDL_CreateTextureFromSurface;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderClear;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderCopy;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderCopyEx;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderDrawLineF;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderDrawRect;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderFillRect;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderPresent;
import static io.github.libsdl4j.api.render.SdlRender.SDL_SetRenderDrawBlendMode;
import static io.github.libsdl4j.api.render.SdlRender.SDL_SetRenderDrawColor;
import static io.github.libsdl4j.api.render.SdlRender.SDL_SetRenderTarget;
import static io.github.libsdl4j.api.render.SdlRender.SDL_SetTextureBlendMode;
import static io.github.libsdl4j.api.surface.SdlSurface.SDL_LoadBMP;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import app.Logger.Severity;
import io.github.libsdl4j.api.blendmode.SDL_BlendMode;
import io.github.libsdl4j.api.pixels.SDL_Color;
import io.github.libsdl4j.api.rect.SDL_Rect;
import io.github.libsdl4j.api.render.SDL_Renderer;
import io.github.libsdl4j.api.render.SDL_RendererFlip;
import io.github.libsdl4j.api.render.SDL_Texture;
import io.github.libsdl4j.api.render.SDL_TextureAccess;
import io.github.libsdl4j.api.surface.SDL_Surface;
import io.github.libsdl4j.api.pixels.SDL_PixelFormatEnum;

/**
 * DrawHandler
 */
public class DrawHandler {

    private static SDL_Renderer renderer;

    private static Vf2 winSize = new Vf2(1920, 1080);
    private static SDL_Texture drawText = null;
    private static SDL_Rect rect = new SDL_Rect();

    // Sprites
    private static Map<String, Sprite> sprites = new HashMap<String, Sprite>();
    private static Map<String, Animation> animations = new HashMap<String, Animation>();
    private static Map<String, Texture> textures = new HashMap<String, Texture>();
    private static ArrayList<Grid> grids = new ArrayList<Grid>();

    // Paths
    private static final String animDataPath = "data/anim.data";
    private static final String textDataPath = "data/text.data";
    private static final String textResPath = "resources/bmp/";

    // Fade in out
    private static float fadeValue = 0f;
    private static float fadeIntensity = 1f;
    private static boolean fadeIn = false;
    private static boolean fadeOut = false;

    public static final class Color {
        /*
         * Returns SDL_Color with ints converted to bytes
         */
        public static SDL_Color color(int r, int g, int b, int a) {
            return new SDL_Color((byte)r, (byte)g, (byte)b, (byte)a);
        }

        public static final SDL_Color RED = new SDL_Color((byte)0xFF, (byte)0x0, (byte)0x0, (byte)0xFF);
        public static final SDL_Color GREEN = new SDL_Color((byte)0x0, (byte)0xFF, (byte)0x0, (byte)0xFF);
        public static final SDL_Color BLUE = new SDL_Color((byte)0x0, (byte)0x0, (byte)0xFF, (byte)0xFF);
        public static final SDL_Color GREY = new SDL_Color((byte)0x120, (byte)0x120, (byte)0x120, (byte)0xFF);
    }

    /*
     * Initialize draw handler
     */
    public static void init(SDL_Renderer _renderer) {
        renderer = _renderer;
        if (renderer == null) {
            Logger.log(Severity.WARNING, "DrawHandler", "Renderer is null");
            return;
        }

        SDL_SetRenderDrawBlendMode(renderer, SDL_BlendMode.SDL_BLENDMODE_BLEND);
        drawText = SDL_CreateTexture(renderer,
                SDL_PixelFormatEnum.SDL_PIXELFORMAT_RGBA8888,
                SDL_TextureAccess.SDL_TEXTUREACCESS_TARGET,
                (int)winSize.x,
                (int)winSize.y);
    }

    /**
     * Starts fading in from black screen<br>
     * fading speed is determined by _intensity
     */
    public static void setFadeIn(float _intensity) {
        fadeIntensity = _intensity;
        fadeValue -= Time.DeltaTime() * _intensity;
        fadeIn = true;
    }
    /**
     * Starts fading out to black screen<br>
     * fading speed is determined by _intensity
     */
    public static void setFadeOut(float _intensity) {
        fadeIntensity = _intensity;
        fadeValue += Time.DeltaTime() * _intensity;
        fadeOut = true;
    }
    /**
     * Returns current level of fade in / out
     */
    public static float getFadeOut() {
        return fadeValue;
    }

    /**
     * Loads grid from str
     */
    public static void loadGrid(String[] str) {
        Texture text = getText(str[1]);
        if (text == null) {
            Logger.log(Severity.WARNING, "DrawHandler", "Could not load grid. Texture :" + str[1] + " does not exist");
            return;
        }

        Grid grid = null;
        try {
            grid = new Grid(
                new Vi2(Integer.valueOf(str[2]), Integer.valueOf(str[3])), 
                new Vi2(Integer.valueOf(str[4]), Integer.valueOf(str[5])),
                new Vi2(Integer.valueOf(str[6]), Integer.valueOf(str[7])),
                new Vd2(Double.valueOf(str[8]), Double.valueOf(str[9])));
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "DrawHandler", "Could not load grid sizes: " + e);
            return;
        }

        Vi2[] data = new Vi2[(str.length - 10) / 2];
        try {
            for (int i = 0; i < data.length; ++i) {
                data[i] = new Vi2(Integer.valueOf(str[10 + 2*i]), Integer.valueOf(str[10 + 2*i+1]));
            }
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "DrawHandler", "Could not load grid data:" + e);
            return;
        }

        grid.setTexture(text, data, renderer);
        grids.add(grid);
    }

    /**
     * Loads textures and animations
     */
    public static void load() {
        loadTextures();
        loadAnim();
    }

    /**
     * Loads texture from file
     */
    public static void loadTexture(String name, String fname, Vi2 size) {
        Texture text = new Texture();

        SDL_Surface surf = SDL_LoadBMP(textResPath + fname);
        if (surf == null) {
            Logger.log(Severity.WARNING, "DrawHandler", "Could not load SDL_Surface " + (textResPath + fname) + ": " + SDL_GetError());
            return;
        }

        text.setText(SDL_CreateTextureFromSurface(renderer, surf));
        SDL_SetTextureBlendMode(text.getText(), SDL_BlendMode.SDL_BLENDMODE_BLEND);
        text.setSize(size);

        textures.put(name, text);

    }

    private static void loadTextures() {
        File file = new File(textDataPath);
        if (!file.exists()) {
            Logger.log(Severity.WARNING, "DrawHandler", "Texture data could not be loaded!");
            return;
        }

        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "DrawHandler", "Could not create scanner: " + e);
            sc.close();
            return;
        }

        String line = null;
        String[] str = null;

        while(true) {
            try {
                Texture text = new Texture();

                // Get splitted line from texture data file
                line = sc.nextLine();
                str = line.split("\\s+");

                Logger.log(Severity.INFO, "DrawHandler", "Loading texture, length: " + str.length);

                SDL_Surface surf = SDL_LoadBMP(textResPath + str[1]);
                if (surf == null) {
                    Logger.log(Severity.WARNING, "DrawHandler", "Could not load SDL_Surface " + (textResPath + str[1]) + ": " + SDL_GetError());
                    continue;
                }

                text.setText(SDL_CreateTextureFromSurface(renderer, surf));
                text.setSize(new Vi2(Integer.valueOf(str[2]), Integer.valueOf(str[3])));

                textures.put(str[0], text);
            } catch (NoSuchElementException e) {
                    break;
            } catch (Exception e) {
                Logger.log(Severity.WARNING, "DrawHandler", "Could not load texture: " + e);
            }
        }

        sc.close();
    }

    private static void loadAnim() {
        File file = new File(animDataPath);
        if (!file.exists()) {
            Logger.log(Severity.WARNING, "DrawHandler", "Animation data could not be loaded!");
            return;
        }

        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (Exception e) {
            Logger.log(Severity.WARNING, "DrawHandler", "Could not create scanner: " + e);
            sc.close();
            return;
        }

        String line = null;
        String[] str = null;

        while(true) {
            try {
                Animation anim = new Animation();

                // Get splitted line from anim data file
                line = sc.nextLine();
                if (line.isEmpty()) break;
                str = line.split("\\s+");

                // Ignore comments
                if (str[0].charAt(0) == '#') {
                    continue;
                }

                Logger.log(Severity.INFO, "DrawHandler", "Loading anim: " + str[0] + ", length: " + (str.length-6));

                float[] animData = new float[str.length - 6];

                anim.setText(str[1]);

                anim.setSize(new Vi2(Integer.valueOf(str[2]), Integer.valueOf(str[3])));
                anim.setPos(new Vi2(Integer.valueOf(str[4]), Integer.valueOf(str[5])));

                for (int i = 6; i < str.length; ++i) {
                    animData[i-6] = Float.valueOf(str[i]);
                }

                anim.setFrameData(animData);

                animations.put(str[0], anim);
            } catch (NoSuchElementException e) {
                    break;
            } catch (Exception e) {
                Logger.log(Severity.WARNING, "DrawHandler", "Could not load animation: " + e);
            }
        }

        sc.close();
    }

    /**
     * Creats new sprite
     */
    public static void loadSprite(String text, String sprite, Vi2 pos, Vi2 size) {
        SDL_Rect sRect = new SDL_Rect();
        sRect.x = pos.x;
        sRect.y = pos.y;
        sRect.w = size.x;
        sRect.h = size.y;

        sprites.put(sprite, new Sprite(text, sRect));
    }

    /**
     * Returns reference to animation<br>
     * Returns null, if tag doesn't exist
     */
    public static Animation getAnim(String tag) {
        if (!animations.containsKey(tag)) {
            return null;
        }
        return animations.get(tag);
    }

    /**
     * Returns reference to texture<br>
     * Returns null, if tag doesn't exist
     */
    public static Texture getText(String text) {
        if (!textures.containsKey(text)) return null;
        return textures.get(text);
    }

    /**
     * Returns reference to sprite<br>
     * Returns null, if tag doesn't exist
     */
    public static Sprite getSpr(String spr) {
        if (!sprites.containsKey(spr)) return null;
        return sprites.get(spr);
    }

    /**
     * Draws sprite on to the screen
     */
    public static void drawSprite(String sprite, SDL_Rect dstRect, double rot, boolean flip) {
        if (!sprites.containsKey(sprite)) {
            Logger.log(Severity.WARNING, "DrawHandler", "Sprite not loaded: " + sprite);
            return;
        }
        Sprite spr = sprites.get(sprite);
        if (!textures.containsKey(spr.getText())) {
            Logger.log(Severity.WARNING, "DrawHandler", "Texture not loaded: " + spr.getText());
            return;
        }

        SDL_Texture temp = textures.get(spr.getText()).getText();

        int doFlip = (flip) ?
            SDL_RendererFlip.SDL_FLIP_HORIZONTAL :
            SDL_RendererFlip.SDL_FLIP_NONE;

        if (temp != null) {
            if (rot != 0 || flip) {
                SDL_RenderCopyEx(renderer, temp, spr.getSRect(), dstRect, rot, null, doFlip);
            } else {
                SDL_RenderCopy(renderer, temp, spr.getSRect(), dstRect);
            }
        } else {
            Logger.log(Severity.WARNING, "DrawHandler", "Texture is null: " + spr.getText());
        }
    }

    private static boolean drawAnim(AnimManager anim, SDL_Rect dstRect, boolean flip) {
        SDL_Rect sRect = new SDL_Rect();
        SDL_Texture text = anim.getText(sRect);
        if (text == null) {
            return false;
        }

        int doFlip = (flip) ?
            SDL_RendererFlip.SDL_FLIP_HORIZONTAL :
            SDL_RendererFlip.SDL_FLIP_NONE;
        if (flip) {
            SDL_RenderCopyEx(renderer, text, sRect, dstRect, 0, null, doFlip);
        } else {
            SDL_RenderCopy(renderer, text, sRect, dstRect);
        }

        return true;
    }

    private static void drawColGrid() {
        ColGrid grid = ObjectHandler.getGrid();
        for (int y = 0; y < grid.getWidth(); ++y) {
            for (int x = 0; x < grid.getWidth(); ++x) {
                Vd2 pos = new Vd2(grid.getPos().x + x*grid.getCellSize(),
                grid.getPos().y + y*grid.getCellSize());
                Vd2 size = new Vd2(grid.getCellSize(), grid.getCellSize());

                if (x+y*grid.getWidth() == ObjectHandler.getObject("player").getGridIndex()) {
                    setColor(Color.color(200, 0, 0, 255));
                } else {
                    setColor(Color.color(200, 200, 200, 255));
                }
                drawRect(Camera.cameraPos(pos), size, false);
                drawRect(Camera.cameraPos(new Vd2(pos.x+1, pos.y+1)), new Vd2(size.x-2, size.y-2), false);
            }
        }
    }

    /**
     * Draws grids, objects and fade in / out
     */
    public static void draw() {
        if (renderer == null) {
            Logger.log(Severity.WARNING, "DrawHandler", "Renderer is null");
            return;
        }
        if (drawText == null) {
            Logger.log(Severity.WARNING, "DrawHandler", "Draw texture is null");
            return;
        }

        SDL_SetRenderTarget(renderer, drawText);
        SDL_SetRenderDrawColor(renderer, (byte)0x22, (byte)0x22, (byte)0x22, (byte)0xFF);
        SDL_RenderClear(renderer);

        drawGrids();
        drawObjects();

        if (Debug.drawGridDebug) {
            setColor(Color.color(200, 200, 200, 255));
            drawColGrid();
        }

        if (fadeValue != 0) {
            if (fadeIn) {
                fadeValue -= fadeIntensity* Time.DeltaTime();
                if (fadeValue < 0) {
                    fadeValue = 0;
                    fadeIn = false;
                }
            }
            if (fadeOut) {
                fadeValue += fadeIntensity * Time.DeltaTime();
                if (fadeValue > 255) {
                    fadeValue = 255;
                    fadeOut = false;
                }
            }

            setColor(DrawHandler.Color.color(0, 0, 0, (int)fadeValue));
            drawRect(new Vd2(), new Vd2(DrawHandler.getWinSize()), true);
        }
    }

    private static void drawGrids() {
        for (int i = 0; i < grids.size(); ++i) {
            Grid grid = grids.get(i);
            Vd2 pos = Camera.cameraPos(grid.getPos());
            rect.x = (int)Math.round(pos.x);
            rect.y = (int)Math.round(pos.y);
            rect.w = (int)Math.round(grid.getSize().x);
            rect.h = (int)Math.round(grid.getSize().y);
            SDL_RenderCopy(renderer, grid.getText(), null, rect);
        }
    }

    static class SortY implements Comparator<Object> {
        public int compare(Object a, Object b) {
                return (int)(
                a.getCol().getPos().y + a.getCol().getSize().y -
            (b.getCol().getPos().y + b.getCol().getSize().y));
        }
    }



    private static void drawObjects() {
        ArrayList<Object> objects = ObjectHandler.getGrid().getArea(Camera.getPos(), new Vd2(winSize));
        Collections.sort(objects, new SortY());

        for (Object obj : objects) {
            if (obj instanceof Destructible && !obj.isAlive()) continue;

            rect.x = (int)Math.round(Camera.cameraPos(obj.getPos()).x);
            rect.y = (int)Math.round(Camera.cameraPos(obj.getPos()).y);
            rect.w = (int)Math.round(obj.getSize().x);
            rect.h = (int)Math.round(obj.getSize().y);

            boolean drawn = false;
            if (obj.getTag().equals("throwable") || obj.getTag().equals("coin")) {
                drawn = true;
                Throwable _obj = (Throwable)obj;
                SDL_SetRenderDrawBlendMode(renderer, SDL_BlendMode.SDL_BLENDMODE_BLEND);
                drawSprite(_obj.getShadowSpr(), rect, 0, obj.getFlip());
                rect.y -= _obj.getHeight();
                drawSprite(_obj.getObjSpr(), rect, 0, obj.getFlip());
            }
            
            if (!drawn && obj.getAnim() != null) {
                drawn = drawAnim(obj.getAnim(), rect, obj.getFlip());
            } 
            
            if (!drawn && obj.getSprite() != null) {
                drawSprite(obj.getSprite(), rect, obj.getRotation(), obj.getFlip());
            } else if (!drawn) {
                SDL_SetRenderDrawColor(renderer, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0xFF);

                SDL_RenderFillRect(renderer, rect);
            }

            if (Camera.getMode() == Camera.Mode.doubleTarget) {
                if (Camera.getTarget() == obj.getId()) {
                    rect.w = 24;
                    rect.h = 24;
                    rect.x += obj.getSize().x / 2 - rect.w / 2;
                    rect.y += obj.getSize().x / 2 - rect.w / 2;
                    SDL_Rect sRect = new SDL_Rect();
                    sRect.x = 0;
                    sRect.y = 0;
                    sRect.w = 64;
                    sRect.h = 64;
                    SDL_RenderCopy(renderer, getText("UI").getText(), sRect, rect);
                }
            }
        }
    }

    /**
     * Draw rectangle<br>
     * Color can be set using setColor
     */
    public static void drawRect(Vd2 pos, Vd2 size, boolean fill) {
        if (renderer == null) {
            Logger.log(Severity.WARNING, "DrawHandler", "Renderer is null");
            return;
        }

        SDL_Rect rect = new SDL_Rect();
        rect.x = (int)Math.round(pos.x);
        rect.y = (int)Math.round(pos.y);
        rect.w = (int)Math.round(size.x);
        rect.h = (int)Math.round(size.y);
        if (fill) {
            SDL_RenderFillRect(renderer, rect);
        } else {
            SDL_RenderDrawRect(renderer, rect);
        }
    }

    /**
     * Draw line<br>
     * Color can be set using setColor
     */
    public static void drawLine(Vf2 origin, Vf2 dest) {
        if (renderer == null) {
            Logger.log(Severity.WARNING, "DrawHandler", "Renderer is null");
            return;
        }

        SDL_RenderDrawLineF(renderer, origin.x, origin.y, dest.x, dest.y);
    }

    /**
     * Sets drawing color
     */
    public static void setColor(SDL_Color col) {
        if (renderer == null) {
            Logger.log(Severity.WARNING, "DrawHandler", "Renderer is null");
            return;
        }

        SDL_SetRenderDrawColor(renderer, col.r, col.g, col.b, col.a);
    }

    /**
     * Presents buffer to screen
     */
    public static void present() {
        if (renderer == null) {
            Logger.log(Severity.WARNING, "DrawHandler", "Renderer is null");
            return;
        }

        SDL_SetRenderTarget(renderer, null);
        SDL_RenderCopy(renderer, drawText, null, null);
        SDL_RenderPresent(renderer);
    }

    public static Vf2 getWinSize() {
        return new Vf2(winSize);
    }
}
