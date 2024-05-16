package app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * App
 */
public class ColliderTest {

    @Test
    public void simpleAABCNoCol() {
        Object a = new Object("", new Vd2(), new Vd2(100, 100), false);
        Object b = new Object("", new Vd2(200, 200), new Vd2(100, 100), false);

        Collider colA = new Collider(new Vd2(), new Vd2(100, 100));
        Collider colB = new Collider(new Vd2(), new Vd2(100, 100));

        colA.setParent(a);
        colB.setParent(b);

        assertFalse(colA.simpleAABC(colB));
    }

    @Test
    public void simpleAABCCol() {
        Object a = new Object("", new Vd2(), new Vd2(100, 100), false);
        Object b = new Object("", new Vd2(50, 50), new Vd2(100, 100), false);

        Collider colA = new Collider(new Vd2(), new Vd2(100, 100));
        Collider colB = new Collider(new Vd2(), new Vd2(100, 100));

        colA.setParent(a);
        colB.setParent(b);

        assertTrue(colA.simpleAABC(colB));
    }

    @Test
    public void projAABCNoCol() {
        Object a = new Object("", new Vd2(), new Vd2(100, 100), false);
        Object b = new Object("", new Vd2(101, 101), new Vd2(100, 100), false);
        a.setVelocity(new Vd2(-10, -10));

        Collider colA = new Collider(new Vd2(), new Vd2(100, 100));
        Collider colB = new Collider(new Vd2(), new Vd2(100, 100));

        colA.setParent(a);
        colB.setParent(b);

        Vd2 norm = new Vd2();

        assertTrue(colA.projAABC(colB, norm) == 0);

    }

    @Test
    public void projAABCCol() {
        Object a = new Object("", new Vd2(), new Vd2(100, 100), false);
        Object b = new Object("", new Vd2(101, 101), new Vd2(100, 100), false);
        a.setVelocity(new Vd2(10, 10));

        Collider colA = new Collider(new Vd2(), new Vd2(100, 100));
        Collider colB = new Collider(new Vd2(), new Vd2(100, 100));

        colA.setParent(a);
        colB.setParent(b);

        Vd2 norm = new Vd2();

        assertTrue(colA.projAABC(colB, norm) > 0);
    }

    @Test
    public void entityAABCNoCol() {
        Object a = new Object("", new Vd2(), new Vd2(100, 100), false);
        Object b = new Object("", new Vd2(200, 200), new Vd2(100, 100), false);

        Collider colA = new Collider(new Vd2(), new Vd2(100, 100));
        Collider colB = new Collider(new Vd2(), new Vd2(100, 100));

        colA.setParent(a);
        colB.setParent(b);

        Vd2 norm = new Vd2();

        assertTrue(colA.entityAABC(colB, norm) == 0);
    }

    @Test
    public void entityAABCCol() {
        Object a = new Object("", new Vd2(), new Vd2(100, 100), false);
        Object b = new Object("", new Vd2(50, 50), new Vd2(100, 100), false);

        Collider colA = new Collider(new Vd2(), new Vd2(100, 100));
        Collider colB = new Collider(new Vd2(), new Vd2(100, 100));

        colA.setParent(a);
        colB.setParent(b);

        Vd2 norm = new Vd2();

        assertTrue(colA.entityAABC(colB, norm) > 0);
    }
}
