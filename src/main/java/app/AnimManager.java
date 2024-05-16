package app;

import io.github.libsdl4j.api.rect.SDL_Rect;
import io.github.libsdl4j.api.render.SDL_Texture;

/**
 * AnimManager
 */
public class AnimManager {
    private String curAnim = null;
    private boolean loop = true;
    private boolean randLoop = false;

    // Animation timing
    private float time = 0;
    private int curFrame = 0;

    /**
     * Sets animation to play and resets animation timings
     */
    public void playAnim(String tag, boolean _loop) {
        if (tag == null) return;
        if (tag == curAnim && loop) return;
        if (tag != null && randLoop) {
            time = (float)Math.random() * (DrawHandler.getAnim(tag).lastFrameTime());
        } else {
            time = 0;
        }
        curFrame = 0;
        curAnim = tag;
        loop = _loop;
    }

    /**
     * Set the animation to last frame
     */
    public void jumpEnd() {
        if (curAnim != null) {
            time = DrawHandler.getAnim(curAnim).lastFrameTime();
            curFrame = DrawHandler.getAnim(curAnim).getFrame(curFrame, time);
        }
    }

    /**
     * Returns tag of current playing animation
     */
    public String getAnimTag() {
        return curAnim;
    }

    /**
     * Set whether animations should start at random frame
     */
    public void setRandLoop(boolean _randLoop) {
        randLoop = _randLoop;
    }


    /**
     * Returns texture that should be displayed based on current animation timings
     */
    public SDL_Texture getText(SDL_Rect sRect) {
        Animation anim = DrawHandler.getAnim(curAnim);
        if (anim == null) return null;

        time += Time.DeltaTime();
        if (loop && time > anim.lastFrameTime()) {
            time = 0;
            curFrame = 0;
        }

        curFrame = anim.getFrame(curFrame, time);
        return anim.getText(curFrame, sRect);
    }
}
