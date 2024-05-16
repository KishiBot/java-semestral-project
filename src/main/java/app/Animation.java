package app;

import io.github.libsdl4j.api.rect.SDL_Rect;
import io.github.libsdl4j.api.render.SDL_Texture;

/**
 * Animation
 */
public class Animation {
    // Texture tag
    private String text = null;

    // Texture positioning
    private Vi2 size = null;
    private Vi2 pos = null;

    // Data on when each frame should end
    private float[] frameData = null;

    /**
     * Sets animation texture tag
     */
    public void setText(String _text) {
        text = _text;
    }
    public String getTextName() {
        return text;
    }

    private float getLength(int frame) {
        float count = 0;
        for (int i = 0; i < frameData.length; ++i) {
            if (i != frame) {
                count += frameData[i];
                continue;
            }

            return frameData[i] + count;
        }
        return 0;
    }

    /**
     * Sets size taken from texture
     */
    public void setSize(Vi2 _size) {
        size = _size;
    }

    /**
     * Sets texture positioning
     */
    public void setPos(Vi2 _pos) {
        pos = _pos;
    }

    /**
     * Sets frame data
     */
    public void setFrameData(float[] _frameData) {
        frameData = _frameData;
    }

    /**
     * Returns texture that should be drawn based on given parameters
     */
    public SDL_Texture getText(int curFrame, SDL_Rect sRect) {
        if (text == null) return null;

        Texture curText = DrawHandler.getText(text);
        if (curText == null) {
            return null;
        }

        sRect.x = pos.x + (size.x * curFrame) % curText.getSize().x;
        sRect.y = pos.y + (size.y * (curFrame/(frameData.length))) % curText.getSize().y;
        sRect.w = size.x;
        sRect.h = size.y;
        return curText.getText();
    }

    /**
     * Returns time when animation should loop
     */
    public float lastFrameTime() {
        if (frameData.length == 0) return 0f;
        return getLength(frameData.length-1);
    }

    /**
     * Returns current animation frame based on given animation time
     */
    public int getFrame(int curFrame, float time) {
        if (getLength(curFrame) < time && curFrame < frameData.length-1) {
            return curFrame+1;
        }
        return curFrame;
    }
}
