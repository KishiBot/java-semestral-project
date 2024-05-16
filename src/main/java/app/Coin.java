package app;

/**
 * Coin
 */
public class Coin extends Throwable {

    private static final float intDist = 40f;
    private static final float movDist = 100f;
    private static final float speed = 100f;
    private double playerDist = 0;
    private Player player = null;
    private boolean givenCoins = false;
    private int cost = 1;

    public Coin(String _tag, Vd2 _pos, Vd2 _size, int _hp, float _lifeTime, float _distance, Vd2 _dir, int _dmg, int _cost) {
        super(_tag, _pos, _size, _hp, _lifeTime, _distance, _dir, _dmg);
        player = (Player)ObjectHandler.getObject("player");
        cost = _cost;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public void update() {

        timer += Time.DeltaTime();
        mov();
        if (timer >= lifeTime) {
            onGround = true;
            if (player == null) return;
            if (!player.isAlive()) return;
            Vd2 dir = Vd2.sub(player.getCol().getCenter(), getCol().getCenter());
            playerDist = dir.length();

            if (playerDist < movDist) {
                setVelocity(Vd2.mult(Vd2.normalized(dir), speed * Time.DeltaTime()));
            }
        }
    }

    @Override
    public void onTrigger(Object obj) {
        if (givenCoins) return;
        if (obj.getTag().equals("player") && onGround) {
            if (playerDist < intDist && obj.isAlive()) {
                ((Player)obj).giveCoin(cost);
                ObjectHandler.removeObject(getId());
                givenCoins = true;
            }
        }
    }
}
