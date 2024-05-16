package app;

import io.github.libsdl4j.api.rect.SDL_Rect;

/**
 * Destructible
 */
public class Destructible extends Object {
    private Object destObj = null;

    private Vd2 defPos = null;
    private Vd2 defColOff = null;

    private float hitTimer = 0f;
    private final float hitConst = 10f;

    public Destructible(String _tag, Vd2 _pos, Vd2 _size, int _hp) {
        super(_tag, _pos, _size, _hp, true);

        defPos = _pos;
    }

    /**
     * Sets which object will be created after this is destroyed
     */
    public void setDestObj(Object _obj) {
        destObj = _obj;
    }

    @Override
    public void update() {
        if (defColOff == null) {
            defColOff = getCol().getOffset();
        }

        if (hitTimer > 0) {
            hitTimer -= Time.DeltaTime()*20f;
            double change = (hitTimer)*Math.sin(hitTimer*Math.PI);
            setPos(Vd2.add(defPos, change));
            getCol().setOffset(Vd2.sub(defColOff, change));
        }
    }

    @Override
    public void onHit(Vd2 dir, Object attacker) {
        hitTimer = hitConst;
    }

    @Override
    public void onDeath() {
        alive = false;
        getCol().setTrigger(true);

        for (int i = 0; i < MathFuncs.randInt(4)+1; ++i) {
            Vd2 dir = new Vd2(Math.random()*2 - 1, Math.random()*2 - 1);

            Throwable obj = new Throwable("throwable", Vd2.sub(getCol().getCenter(), new Vd2(32, 32)), new Vd2(32, 32), 1, 1f, (float)(100*Math.random()), dir, 0);
            obj.setMaxHeight(100);
            SDL_Rect a = new SDL_Rect();
            a.x = 48;
            a.y = 0;
            a.w = 16;
            a.h = 16;

            Sprite plank = new Sprite("coin", a);
            SDL_Rect b = new SDL_Rect();
            b.x = 0;
            b.y = 0;
            b.w = 16;
            b.h = 16;
            Sprite shadow = new Sprite("shade", b);
            obj.setSpr("plank", "shade", plank, shadow);

            // Create coin collider
            Collider col = new Collider(new Vd2(-7, -7), new Vd2(15, 15));
            col.setParent(obj);
            obj.setCollider(col);
            col.setTrigger(true);

            ObjectHandler.createObject(obj);
        }

        if (destObj != null) {
            ObjectHandler.createObject(destObj);
        }
    }
}
