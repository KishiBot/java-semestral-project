package app;

import static io.github.libsdl4j.api.render.SdlRender.SDL_CreateTexture;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderClear;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderCopy;
import static io.github.libsdl4j.api.render.SdlRender.SDL_SetRenderDrawBlendMode;
import static io.github.libsdl4j.api.render.SdlRender.SDL_SetRenderDrawColor;
import static io.github.libsdl4j.api.render.SdlRender.SDL_SetRenderTarget;
import static io.github.libsdl4j.api.render.SdlRender.SDL_SetTextureBlendMode;

import app.Logger.Severity;
import io.github.libsdl4j.api.blendmode.SDL_BlendMode;
import io.github.libsdl4j.api.pixels.SDL_PixelFormatEnum;
import io.github.libsdl4j.api.rect.SDL_Rect;
import io.github.libsdl4j.api.render.SDL_Renderer;
import io.github.libsdl4j.api.render.SDL_Texture;
import io.github.libsdl4j.api.render.SDL_TextureAccess;

/**
 * Grid
 */
public class Grid {
    
    private Vi2 dstTileSize = null;
    private Vi2 sTileSize = null;
    private Vi2 size = null;
    private Vd2 pos = null;
    private SDL_Texture text = null;

    public Grid(Vi2 _size, Vi2 _dstTileSize, Vi2 _sTileSize, Vd2 _pos) {
        dstTileSize = _dstTileSize;
        sTileSize = _sTileSize;
        size = _size;
        pos = _pos;
    }

    public Vd2 getPos() {
        return new Vd2(pos.x, pos.y);
    }

    public Vi2 getSize() {
        return new Vi2(size);
    }

    public SDL_Texture getText() {
        return text;
    }

    /**
     * Bakes new grid texture
     */
    public void setTexture(Texture sheet, Vi2[] data, SDL_Renderer renderer) {
        if (renderer == null) {
            Logger.log(Severity.WARNING, "Grid", "Renderer is null");
            return;
        }

        text = SDL_CreateTexture(renderer,
                SDL_PixelFormatEnum.SDL_PIXELFORMAT_RGBA8888,
                SDL_TextureAccess.SDL_TEXTUREACCESS_TARGET,
                size.x,
                size.y);
        if (text == null) {
            Logger.log(Severity.WARNING, "Grid", "Could not create texture!");
            return;
        }

        SDL_SetRenderTarget(renderer, text);
        SDL_SetTextureBlendMode(text, SDL_BlendMode.SDL_BLENDMODE_BLEND);
        SDL_SetRenderDrawColor(renderer, (byte)0, (byte)0, (byte)0, (byte)0);
        SDL_RenderClear(renderer);

        SDL_Rect sRect = new SDL_Rect();
        sRect.x = 0;
        sRect.y = 0;
        sRect.w = sTileSize.x;
        sRect.h = sTileSize.y;

        SDL_Rect dstRect = new SDL_Rect();
        dstRect.x = 0;
        dstRect.y = 0;
        dstRect.w = dstTileSize.x;
        dstRect.h = dstTileSize.y;

        int tileCount = size.x / dstTileSize.x;
        for (int i = 0; i < data.length; ++i) {
            dstRect.x = dstTileSize.x * (i % tileCount);
            dstRect.y = dstTileSize.y * (i / tileCount);
            if (dstRect.y > size.y) {
                Logger.log(Severity.WARNING, "Grid", "Attempt to create more tiles then in grid");
                break;
            }

            sRect.x = data[i].x;
            sRect.y = data[i].y;
            SDL_RenderCopy(renderer, sheet.getText(), sRect, dstRect);
        }

        SDL_SetRenderTarget(renderer, null);
    }
}
