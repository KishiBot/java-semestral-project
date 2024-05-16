package app;

import io.github.libsdl4j.api.rect.SDL_Rect;

/**
 * TntGoblin
 */
public class TntGoblin extends Enemy {
    public TntGoblin(String _tag, Vd2 _pos, Vd2 _size, int _hp) {
        super(_tag, _pos, _size, _hp);

        actionTable = new int[] {0, 4, 6, 10};
        maxAction = 10;

        createThrowable = new CreateTnt(this);

        // Set animation
        idleAnim = "goblin_dmt_idle";
        moveAnim = "goblin_dmt_mov";
        throwAnim = "goblin_dmt_att";

        attDist = 400;
        moveActionSpeed = 125;

        coinCost = 10;
        setCoin(5);
    }

    private class CreateTnt implements Runnable {
        private Object parent = null;
        public CreateTnt(Object _parent) {
            parent = _parent;
        }

        @Override
        public void run() {
            // Get target dir
            Vd2 dir = Vd2.sub(target.getCol().getCenter(), parent.getCol().getCenter());

            // Create dynamite
            Throwable obj = new Throwable("throwable", Vd2.sub(parent.getCol().getCenter(), new Vd2(32, 32)), new Vd2(64, 64), 1, 1f, (float)dir.length(), dir, 0);
            SDL_Rect a = new SDL_Rect();
            a.x = 0;
            a.y = 0;
            a.w = 64;
            a.h = 64;
            Sprite dynamite = new Sprite("dynamite", a);
            SDL_Rect b = new SDL_Rect();
            b.x = 0;
            b.y = 0;
            b.w = 16;
            b.h = 16;
            Sprite shadow = new Sprite("shade", b);
            obj.setSpr("dynamite", "shade", dynamite, shadow);

            // Create dynamite collider
            Collider col = new Collider(new Vd2(), new Vd2(30, 30));
            col.setParent(obj);
            obj.setCollider(col);
            col.setTrigger(true);

            ObjectHandler.createObject(obj);

            // Create explosion
            Throwable dest = new Throwable("enemy", new Vd2(1000, 1000), new Vd2(512, 512), 1, 2.5f, 0, new Vd2(0, 0), 50);
            dest.setDmgLifeTime(.3f);
            dest.getAnim().playAnim("explosion", false);

            // Create explosion collider
            Collider explCol = new Collider(new Vd2(175, 175), new Vd2(160, 160));
            dest.setCollider(explCol);
            explCol.setParent(dest);
            explCol.setTrigger(true);
            obj.setDest(dest);

        }
    }
}
