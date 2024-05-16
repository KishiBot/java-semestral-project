package app;

/**
 * Object
 */
public class Object {
    private int id;
    private String tag = null;
    private boolean entity = false;
    protected boolean alive = true;

    // Position
    private Vd2 pos = null;
    private Vd2 velocity = null;
    protected Vd2 defPos = null;

    // Draw parameters
    private double rotation = 0f;
    private boolean flip = false;

    // Components
    private Vd2 size = null;
    private Collider col = null;
    private Collider attCol = null;
    private String sprite = null;
    private AnimManager anim = new AnimManager();

    // Hp
    private float iFrame = .25f;
    private float defIFrame = .25f;
    private float iFrameTimer = 0f;
    private int maxHp = 0;
    private int hp = 0;

    // QuadTree
    private int gridIndex = -1;

    public Object(String _tag, Vd2 _pos, Vd2 _size, boolean _entity) {
        tag = _tag;
        pos = _pos;
        size = _size;
        velocity = new Vd2();
        entity = _entity;

        defPos = new Vd2(pos);
    }
    public Object(String _tag, Vd2 _pos, Vd2 _size, int _hp, boolean _entity) {
        tag = _tag;
        pos = _pos;
        size = _size;
        velocity = new Vd2();
        maxHp = _hp;
        hp = _hp;
        entity = _entity;

        defPos = new Vd2(pos);
    }

    public void setSize(Vd2 _size) {
        size = _size;
    }

    public void resetPos() {
        pos = new Vd2(defPos);
    }

    public void setAttCol(Collider _col) {
        attCol = _col;
        if (_col == null) return;
        attCol.setParent(this);
        attCol.setTrigger(true);
    }
    public Collider getAttCol() {
        return attCol;
    }

    public int getGridIndex() {
        return gridIndex;
    }
    public void setGridIndex(int _index) {
        gridIndex = _index;
    }

    public void setCollider(Collider _col) {
        col = _col;
        col.setParent(this);
    }

    public boolean isAlive() {
        return alive;
    }
    public void setAlive(boolean _alive) {
        alive = _alive;
    }

    public int getMaxHp() {
        return maxHp;
    }
    public void setMaxHp(int _hp) {
        maxHp = _hp;
        hp = _hp;
    }
    public int getHp() {
        return hp;
    }
    public void setHp(int _hp) {
        hp = _hp;
    }

    /**
     * Sets iframe length and starts its timer
     */
    public void startIFrame(float _iFrame) {
        iFrameTimer = _iFrame;
        iFrameTimer = Time.DeltaTime();
    }

    /**
     * Changes default iframes
     */
    public void setIFrames(float _iFrame) {
        iFrame = _iFrame;
        defIFrame = _iFrame;
    }

    public boolean isEntity() {
        return (entity) ? true : false;
    }

    public AnimManager getAnim() {
        return anim;
    }

    public void rotate(double angle) {
        rotation = -angle;
    }

    public double getRotation() {
        return rotation;
    }
    public boolean getFlip() {
        return flip;
    }
    public void setFlip(boolean _flip) {
        flip = _flip;
    }

    /**
     * Should only be used by the object handler class
     */
    public void setId(int _id) {
        id = _id;
    }
    public int getId() {
        return id;
    }

    public String getTag() {
        return new String(tag);
    }
    public void setTag(String _tag) {
        tag = _tag;
    }

    public Vd2 getPos() {
        return new Vd2(pos.x, pos.y);
    }
    public void setPos(Vd2 _pos) {
        pos = _pos;
    }

    public Vd2 getSize() {
        return new Vd2(size.x, size.y);
    }

    /**
     * Is called every frame once
     */
    public void update() {}

    /**
     * Is called when collider triggers
     */
    public void onTrigger(Object obj) {}

    /**
     * Is called when attack collider triggers
     */
    public void onAttTrigger(Object obj) {}

    /**
     * Is called when hit is called
     */
    public void onHit(Vd2 pos, Object attacker) {}

    /**
     * Called when object's hp gets to 0
     */
    public void onDeath() {
        alive = false;
    }

    /**
     * Lower object's health and move them in direction away from dir
     */
    public void hit(int dmg, Vd2 dir, Object attacker) {
        if (entity && iFrameTimer == 0f) {
            hp -= dmg;
            iFrameTimer += Time.DeltaTime();
            onHit(dir, attacker);
        }
    }

    public void setVelocity(Vd2 _velocity) {
        velocity.x = _velocity.x;
        velocity.y = _velocity.y;
    }
    public Vd2 getVelocity() {
        return new Vd2(velocity.x, velocity.y);
    }

    public Collider getCol() {
        return col;
    }

    public String getSprite() {
        return sprite;
    }
    public void setSprite(String spr) {
        sprite = spr;
    }

    /**
     * Dynamic collision check<br>
     * Resolves collisions with target collider<br>
     * Should be called for all possible collisions<br><br>
     *
     * Does not alter target collider
     */
    public boolean dColCheck(Collider tarCol, boolean entity) {
        Vd2 normal = new Vd2();
        if (!entity) {
            double colTime = col.projAABC(tarCol, normal);
            if (colTime != 0) {
                velocity.add(Vd2.mult(Vd2.mult(Vd2.abs(velocity), normal), 1f-colTime+1E-3f));
                return true;
            }
        } else {
            double len = col.entityAABC(tarCol, normal);
            if (len != 0) {
                velocity.add(Vd2.mult(normal, (1f/normal.length())*3));
                return true;
            }
        }
        return false;
    }

    /**
     * Dynamic update<br>
     * Updates object's position using velocity vector<br>
     * Should be called once per frame<br><br>
     *
     * Alters this.velocity
     */
    public void dUpdate() {
        if (iFrameTimer > 0 && iFrameTimer < iFrame) {
            iFrameTimer += Time.DeltaTime();
        } else if (iFrameTimer >= iFrame){
            iFrameTimer = 0f;
            iFrame = defIFrame;
        }

        pos.add(velocity);
    }
}
