package app;

/**
 * Sword
 */
public class Sword extends Object {
    public Sword(String _tag, Vd2 _pos, Vd2 _size) {
        super(_tag, _pos, _size, false);
    }

    private enum swordPos {
        DOWN,
        UP
    }
    private swordPos sPos = swordPos.DOWN;

    // Hitbox sizes
    private Vd2 hColSize = null;
    private Vd2 hColOff = null;
    private Vd2 vColSize = null;
    private Vd2 vColOff = null;

    private double angle = 0f;
    private float rotSpeed = 3;

    // Attack parameters
    private final double aSpeed = 1500f;
    private double aAngle = 0f;
    private double aRange = 85f;
    private boolean inAttack = false;
    private float aDelay = .45f;
    private float aDelayTimer = .25f;

    @Override
    public void update() {
        // Get player object and its position
        Player playerObject = (Player)ObjectHandler.getObject("player");
        if (playerObject == null) {
            return;
        }
        if (!((Object)playerObject).isAlive()) return;

        Vd2 playerPos = Vd2.sub(
            playerObject.getCol().getPos(),
            new Vd2(
                0,
                playerObject.getCol().getSize().y/2));
        
        // Rotation vector
        Vd2 pos = new Vd2(getSize().x * 0.7f, 0);

        // Attack check
        if (inAttack) {
            if (sPos == swordPos.DOWN && angle - aAngle >= aRange*2) {
                inAttack = false;
                aDelayTimer = 0f;
                sPos = swordPos.UP;
            } else if (sPos == swordPos.UP && aAngle - angle >= aRange*2) {
                inAttack = false;
                aDelayTimer = 0f;
                sPos = swordPos.DOWN;
            }

            if (sPos == swordPos.DOWN) {
                angle += aSpeed * Time.DeltaTime();
            } else {
                angle -= aSpeed * Time.DeltaTime();
            }
        } else {
            if (aDelayTimer >= aDelay) {
                // Get angle from player to cursor
                Vd2 targetPos = null;
                if (Camera.getMode() == Camera.Mode.doubleTarget &&
                ObjectHandler.getObject(Camera.getTarget()) != null) {
                    targetPos = ObjectHandler.getObject(Camera.getTarget()).getCol().getCenter();
                } else {
                    targetPos = Camera.worldPos(Input.getMousePos());
                }
                double newAngle = Vd2.angle(playerPos, targetPos);

                newAngle += (sPos == swordPos.DOWN) ? -aRange: aRange;

                // Figure out closest path to rotate
                if (angle - newAngle > 180) {
                    angle = (1 - rotSpeed * Time.DeltaTime()) * angle + rotSpeed * Time.DeltaTime() * (newAngle + 360);
                    // angle = (1 - 0.025f) * angle + 0.025f * (newAngle + 360);
                } else if (newAngle - angle > 180) {
                    angle = (1 - rotSpeed * Time.DeltaTime()) * angle + rotSpeed * Time.DeltaTime() * (newAngle - 360);
                    // angle = (1 - 0.025f) * angle + 0.025f * (newAngle - 360);
                } else {
                    angle = (1 - rotSpeed * Time.DeltaTime()) * angle + rotSpeed * Time.DeltaTime() * (newAngle);
                    // angle = (1 - 0.025f) * angle + 0.025f * newAngle;
                }
                angle %= 360;

                // Check for attack
                if (Input.leftButton.press() && !inAttack && !Menu.isRunning()) {
                    if (playerObject.hasStam()) {
                        inAttack = true;
                        aAngle = angle;
                        playerObject.takeStam(playerObject.attStamCost, true);
                        playerObject.blockMov(playerObject.attBlockDel);
                        Vd2 dir = Vd2.sub(targetPos, playerObject.getCol().getCenter());
                        dir.normalize();
                        playerObject.push(Vd2.mult(
                            dir,
                            playerObject.attSpd));
                        playerObject.getAnim().playAnim("player_idle", true);
                    }
                }
            } else {
                aDelayTimer += Time.DeltaTime();
            }
        }

        // Rotate
        pos.rotate(angle);
        rotate(angle);

        // Collider
        if (Math.abs(pos.y) > Math.abs(pos.x)) {
            getCol().setSize(vColSize);
            if (pos.y > playerPos.y) {
                getCol().setOffset(vColOff);
            } else {
                getCol().setOffset(new Vd2(vColOff.x, -vColOff.y));
            }
        } else {
            getCol().setSize(hColSize);
            if (pos.x > playerPos.x) {
                getCol().setOffset(hColOff);
            } else {
                getCol().setOffset(new Vd2(-hColOff.x, hColOff.y));
            }
        }
 
        // Add vectors for final sword position
        playerPos = Vd2.add(playerPos, new Vd2(-playerObject.getSize().x/4, playerObject.getSize().y / 2));
        playerPos = Vd2.add(playerPos, pos);
        setPos(Vd2.sub(playerPos, new Vd2(getSize().x/8, getSize().y/2)));
    }


    @Override
    public void setCollider(Collider _col) {
        super.setCollider(_col);
        this.getCol().setTrigger(true);

        // Get collider sizes
        hColSize = getCol().getSize();
        vColSize = new Vd2(hColSize.y, hColSize.x);
        
        // Get collider offsets
        hColOff = getCol().getOffset();
        vColOff = new Vd2(hColOff.y, hColOff.x);
    }

    @Override
    public void onTrigger(Object obj) {
        Player player = (Player)ObjectHandler.getObject("player");
        if (inAttack && obj.isEntity() && !obj.getTag().equals("player")) {
            obj.hit(Player.levelDmg[player.getLevel()], player.getCol().getCenter(), player);
        }
    }
}
