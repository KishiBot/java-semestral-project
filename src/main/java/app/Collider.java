package app;

/**
 * Collider
 */
public class Collider {

    private Vd2 offset;
    private Vd2 size;
    private Object parent;
    private boolean trigger = false;

    public Collider() {
        offset = new Vd2();
        size = new Vd2();
    }
    public Collider(Vd2 _offset, Vd2 _size) {
        offset = _offset;
        size = _size;
    }
    public Vd2 getSize() {
        return new Vd2(size.x, size.y);
    }
    public Vd2 getOffset() {
        return new Vd2(offset.x, offset.y);
    }
    public void setOffset(Vd2 _offset) {
        offset = _offset;
    }
    public Vd2 getPos() {
        Vd2 pos = parent.getPos();
        return new Vd2(pos.x + offset.x, pos.y + offset.y);
    }
    public Vd2 getCenter() {
        Vd2 pos = parent.getPos();
        return new Vd2(pos.x + offset.x + size.x / 2, pos.y + offset.y + size.x / 2);
    }

    public void setTrigger(boolean _trigger) {
        trigger = _trigger;
    }
    public boolean isTrigger() {
        return (trigger) ? true : false;
    }
    public void setSize(Vd2 _size) {
        size = _size;
    }

    /**
     * Should be set at collider initialization as i 
     * have no idea how to assing this through constructor
     */
    public void setParent(Object _parent) {
        parent = _parent; 
    }

    /**
     * Projection axis-aligned box collision<br>
     * Uses parameter normal to return collision's normal vector<br>
     * Returns parent's velocity's 'time' of contact
     */
    public double projAABC(Collider col, Vd2 normal) {
        // quality of life vectors
        Vd2 origin = Vd2.add(getPos(), Vd2.div(size, 2));
        Vd2 velocity = parent.getVelocity();

        Vd2 colLeft = Vd2.sub(col.getPos(), Vd2.div(getSize(), 2f));
        Vd2 colRight = Vd2.add(col.getPos(), Vd2.add(col.getSize(), Vd2.div(getSize(), 2f)));


        // Near and far collision of ray and col
        Vd2 near = Vd2.div(Vd2.sub(colLeft, origin), velocity);
        Vd2 far = Vd2.div(Vd2.sub(colRight, origin), velocity);

        // Swap when near is greater then far
        double temp = 0;
        if (near.x > far.x) {
            temp = near.x;
            near.x = far.x;
            far.x = temp;
        }
        if (near.y > far.y) {
            temp = near.y;
            near.y = far.y;
            far.y = temp;
        }

        // Collision validation checks
        if (near.x > far.y || near.y > far.x) return 0;
        double hitNear = Math.max(near.x, near.y);
        double hitFar = Math.min(far.x, far.y);
        if (hitFar < 0) return 0;
        if (hitNear > 1f) return 0;

        // Creation of normal vector
        if (near.x > near.y) {
            if (velocity.x < 0) {
                normal.x = 1;
                normal.y = 0;
            } else {
                normal.x = -1;
                normal.y = 0;
            }
        } else if (near.y > near.x) {
            if (velocity.y < 0) {
                normal.x = 0;
                normal.y = 1;
            } else {
                normal.x = 0;
                normal.y = -1;
            }
        }

        return hitNear;
    }

    /**
     * Simple axis-aligned box collision
     */
    public boolean simpleAABC(Collider col) {
        Vd2 colRight = Vd2.add(col.getPos(), col.getSize());
        Vd2 colLeft = col.getPos();
        Vd2 left = getPos();
        Vd2 right = Vd2.add(left, size);

        if (colRight.x > left.x && colLeft.x < right.x && colRight.y > left.y && colLeft.y < right.y) {
            return true;
        }
        return false;
    }

    /**
     * Push away collision that uses simpleAABC to check, whether objects are colliding<br>
     * Uses norm vector to return information about collision
     */
    public double entityAABC(Collider col, Vd2 norm) {
        if (!simpleAABC(col)) return 0;
        Vd2 res = Vd2.sub(getPos(), col.getPos());
        norm.x = res.x;
        norm.y = res.y;
        double len = 1 / res.length();
        return len;
    }
}
