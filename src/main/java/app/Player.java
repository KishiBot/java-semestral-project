package app;

import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_W;

import io.github.libsdl4j.api.rect.SDL_Rect;

import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_S;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_SPACE;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_D;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_LSHIFT;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_A;

/**
 * Player
 */
public class Player extends Object {
    private int speed = 150;
    private int runSpeed = 350;
    private Vd2 lastSavePos = null;

    // Stamina
    private float maxStamina = 100f;
    private float stamina = maxStamina;
    private float stamReg = 50f;

    private float stamDel = 0f;
    private float maxStamDel = 1.2f;

    // Stamina cost
    public final float dashStamCost = 20f;
    public final float attStamCost = 35f;
    public final float runStamCost = .3f;

    // Dash
    private float inDash = 0f;
    private float dashLength = .35f;
    private float dashDelay = -0.50f;
    private int dashSpeed = 550;

    private float iFrame = .75f;
    private float dashIFrame = .5f;

    private Vd2 mov = null;
    private float movBlockTimer = 0f;

    // Attack
    public final float attBlockDel = .45f;
    public final float attSpd = 10f;

    // Reset after death
    private float deathTimer = 0;
    private float deathLen = 10f;
    private float fadeOutDel = 4f;

    // Coins
    private int coins = 0;

    // Level
    private int level = 0;
    public static final int maxLevel = 9;
    public static final int[] levelCost = {10, 20, 60, 200, 500, 1200, 3000, 6000, 10000, 20000};
    public static final int[] levelDmg = {10, 15, 20, 40, 100, 200, 400, 600, 800, 1000};

    public Player(String _tag, Vd2 _pos, Vd2 _size, int _hp) {
        super(_tag, _pos, _size, _hp, true);
        setIFrames(iFrame);
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int _level) {
        level = _level;
    }

    public void setLastSavePos(Vd2 _lastSavePos) {
        lastSavePos = _lastSavePos;
    }
    public Vd2 getLastSavePos() {
        return new Vd2(lastSavePos);
    }

    /**
     * Lowers player's stamina and sets delay timaer<br>
     * based on whether delTimer has been set true or false
     */
    public void takeStam(float x, boolean delTimer) {
        if (stamina < x) {
            stamina = 0;
            stamDel = maxStamDel * ((delTimer) ? 2 : 1);
        } else {
            stamina -= x;
            stamDel = (delTimer) ? maxStamDel : (stamDel <= 0) ? .1f: stamDel;
        }
    }

    public void giveCoin(int _amount) {
        coins += _amount;
    }
    public int getCoinNum() {
        return coins;
    }
    public void setCoinNum(int _coins) {
        coins = _coins;
    }

    /**
     * Returns, whether player has stamina
     */
    public boolean hasStam() {
        return (stamina > 0) ? true : false;
    }
    /**
     * Returns the amount of stamina player has
     */
    public float getStam() {
        return stamina;
    }
    /**
     * Returns max stamina player can have
     */
    public float getMaxStam() {
        return maxStamina;
    }

    /**
     * Blocks player from moving for x amount of seconds
     */
    public void blockMov(float x) {
        movBlockTimer = x;
    }
    /**
     * Pushes player in dir direction
     */
    public void push(Vd2 dir) {
        mov = dir;
    }

    @Override
    public void update() {
        if (!alive) {
            deathTimer += Time.DeltaTime();
            if (deathTimer >= deathLen && !ObjectHandler.isSaving()) {
                ObjectHandler.saveState(false);
            }

            // Fadeobj
            if (deathTimer >= fadeOutDel && DrawHandler.getFadeOut() == 0) {
                DrawHandler.setFadeOut(100);
            }
            return;
        }
        deathTimer = 0;

        if (stamDel >= 0) {
            stamDel -= Time.DeltaTime();
        } else {
            if (stamina < maxStamina) {
                stamina += stamReg * Time.DeltaTime();
            }
        }

        if (movBlockTimer <= 0) {
            if (inDash > 0f) {
                inDash += Time.DeltaTime();
                if (inDash >= dashLength) {
                    inDash = -Time.DeltaTime();
                }

                mov.normalize();
                mov.mult(dashSpeed);
            } else {
                mov = new Vd2();
                if (Input.getKey(SDLK_W)) {
                    mov.y -= 1;
                }
                if (Input.getKey(SDLK_S)) {
                    mov.y += 1;
                }
                if (Input.getKey(SDLK_A)) {
                    mov.x -= 1;
                }
                if (Input.getKey(SDLK_D)) {
                    mov.x += 1;
                }
                mov.normalize();

                if (Input.getKey(SDLK_LSHIFT) && hasStam() && !mov.isZero()) {
                    mov.mult(runSpeed);
                    takeStam(runStamCost, false);
                } else {
                    mov.mult(speed);
                }

                if (inDash <= dashDelay) {
                    inDash = 0f;
                } else if (inDash == 0f) {
                    if (Input.getKey(SDLK_SPACE) && !mov.isZero()) {
                        if (hasStam()) {
                            startIFrame(dashIFrame);
                            getAnim().playAnim("player_dash", false);
                            inDash += Time.DeltaTime();

                            takeStam(dashStamCost, true);
                        }
                    } 
                } else {
                    inDash -= Time.DeltaTime();
                }

            }
            if (inDash <= 0) {
                if (mov.y > 0) {
                    getAnim().playAnim("player_up", true);
                    setFlip(false);
                } else if (mov.y < 0) {
                    getAnim().playAnim("player_down", true);
                    setFlip(false);
                } else if (mov.x > 0) {
                    getAnim().playAnim("player_right", true);
                    setFlip(false);
                } else if (mov.x < 0) {
                    getAnim().playAnim("player_right", true);
                    setFlip(true);
                } else {
                    getAnim().playAnim("player_idle", true);
                    setFlip(false);
                }
            }

            mov.mult(Time.DeltaTime());
        } else {
            movBlockTimer -= Time.DeltaTime();
            mov.mult(0.9f);
        }


        setVelocity(mov);
    }

    public static void dropCoin(Vd2 pos, float dist, int cost, String tag) {
        Vd2 dir = new Vd2(Math.random()*2 - 1, Math.random()*2 - 1);

        // Create coin
        Coin obj = new Coin(tag, pos, new Vd2(16, 16), 1, 1f, (float)(dist*Math.random()), dir, 0, cost);
        SDL_Rect a = new SDL_Rect();
        a.y = 0;
        a.w = 16;
        a.h = 16;

        String text = null;
        if (cost < 10) {
            a.x = 32;
            text = "copper";
        } else if (cost < 50) {
            a.x = 16;
            text = "silver";
        } else {
            a.x = 0;
            text = "gold";
        }


        Sprite coin = new Sprite("coin", a);
        SDL_Rect b = new SDL_Rect();
        b.x = 0;
        b.y = 0;
        b.w = 16;
        b.h = 16;
        Sprite shadow = new Sprite("shade", b);
        obj.setSpr(text, "shade", coin, shadow);

        // Create coin collider
        Collider col = new Collider(new Vd2(-7, -7), new Vd2(15, 15));
        col.setParent(obj);
        obj.setCollider(col);
        col.setTrigger(true);

        ObjectHandler.createObject(obj);

    }

    @Override
    public void onDeath() {
        setAlive(false);
        deathTimer += Time.DeltaTime();
        getAnim().playAnim("death", false);
        ObjectHandler.removeCoins();

        int gold = coins / 50;
        int silver = (coins - gold * 50) / 10;
        int copper = coins - silver * 10 - gold * 50;
        for (int i = 0; i < gold; ++i) {
            dropCoin(Vd2.sub(getCol().getCenter(), new Vd2(32, 32)), 100, 50, "coin");
        }
        for (int i = 0; i < silver; ++i) {
            dropCoin(Vd2.sub(getCol().getCenter(), new Vd2(32, 32)), 100, 10, "coin");
        }
        for (int i = 0; i < copper; ++i) {
            dropCoin(Vd2.sub(getCol().getCenter(), new Vd2(32, 32)), 100, 1, "coin");
        }
        coins = 0;
    }
}
