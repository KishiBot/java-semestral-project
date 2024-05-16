
package app;

/**
 * Throwable
 */
public class Throwable extends Object {

    protected float lifeTime = 0;
    protected boolean onGround = false;
    private float dmgLifeTime = 0;

    private float distance = 0;
    private float maxHeight = 200;

    private Vd2 dir = null;
    private double height = 0;
    protected float timer = 0;
    private int dmg = 0;

    private String objSpr = null;
    private String shadowSpr = null;

    private Object dest = null;

    public Throwable(String _tag, Vd2 _pos, Vd2 _size, int _hp, float _lifeTime, float _distance, Vd2 _dir, int _dmg) {
        super(_tag, _pos, _size, _hp, true);

        lifeTime = _lifeTime;
        dmgLifeTime = lifeTime;
        distance = _distance;
        dir = _dir;
        dmg = _dmg;

        if (!dir.isZero()) {
            dir = Vd2.mult(Vd2.normalized(dir), distance / Vd2.normalized(dir).length());
        }
    }

    public void setDmgLifeTime(float _time) {
        dmgLifeTime = _time;
    }

    public void setDest(Object obj) {
        dest = obj;
    }

    public void setMaxHeight(float _maxHeight) {
        maxHeight = _maxHeight;
    }

    /**
     * Set sprites for object and its shadow. <br>
     * null for ignoring one of the sprites
     */
    public void setSpr(String _objSpr, String _shadowSpr, Sprite _obj, Sprite _shadow) {
        if (_objSpr != null && _obj != null) {
            if (DrawHandler.getSpr(_objSpr) == null) {
                DrawHandler.loadSprite(_obj.getText(), _objSpr,
                        new Vi2(_obj.getSRect().x, _obj.getSRect().y),
                        new Vi2(_obj.getSRect().w, _obj.getSRect().h));
            }
            objSpr = _objSpr;
        }
        if (_shadowSpr != null && _shadow != null) {
            if (DrawHandler.getSpr(_shadowSpr) == null) {
                DrawHandler.loadSprite(_shadow.getText(), _shadowSpr,
                        new Vi2(_shadow.getSRect().x, _shadow.getSRect().y),
                        new Vi2(_shadow.getSRect().w, _shadow.getSRect().h));
            }

            if (DrawHandler.getText(_shadow.getText()) == null) {
                DrawHandler.loadTexture(_shadow.getText(), _shadowSpr.concat(".bmp"), 
                        new Vi2(16, 16));
            }

            shadowSpr = _shadowSpr;
        }
    }

    public String getObjSpr() {
        return objSpr;
    }
    public String getShadowSpr() {
        return shadowSpr;
    }
    public double getHeight() {
        return height;
    }

    @Override
    public void onDeath() {
        // Just so the objects doesn't get removed if its hp goes to 0
    }

    protected void mov() {
        if (!dir.isZero()) {
            if (timer <= lifeTime) {
                height = (-Math.pow(((2/lifeTime)*timer-1), 2)+1)*maxHeight;
                setVelocity(Vd2.mult(dir, Time.DeltaTime()));
            } else {
                setVelocity(new Vd2(0, 0));
            }
        }
    }

    @Override
    public void update() {
        if (onGround) {
            ObjectHandler.removeObject(getId());
            if (dest != null) {
                dest.setPos(Vd2.sub(getCol().getCenter(),
                            Vd2.mult(dest.getSize(), .5f)));
                dest.setVelocity(new Vd2());
                ObjectHandler.createObject(dest);
            }
            return;
        }

        timer += Time.DeltaTime();
        mov();
        if (timer >= lifeTime) {
            onGround = true;
        }
    }

    @Override
    public void onTrigger(Object obj) {
        if ((onGround || distance == 0) && timer < dmgLifeTime) {
            if (!obj.getTag().equals("player")) {
                return;
            }
            if (dmg != 0) {
                obj.hit(dmg, getCol().getCenter(), this);
            }
        }
    }
}
