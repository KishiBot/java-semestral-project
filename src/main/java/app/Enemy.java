package app;

import io.github.libsdl4j.api.rect.SDL_Rect;

/**
 * Enemy
 */
public class Enemy extends Object{
    // Basic parameters
    protected int speed = 250;
    protected Vd2 mov = new Vd2();
    protected float seeDist = 600;
    protected float attDist = 80;
    protected Object target = null;
    protected boolean inCombat = false;
    protected float hitBackDiv = .9f;

    // Reset
    private Collider originalCol = null;
    private Vd2 originalSize = null;

    // Wander
    private Vd2 wanderPos = new Vd2();
    private float wanderTimer = 0;
    private float wanderDel = 10f;
    private float wanderSpeed = 50f;
    private float wanderDist = 500f;

    // Hit
    protected float hitTimer = 0f;

    // Attack parameters
    protected int curCombo = 0;
    protected int maxCombo = 1;

    // Attack preparation
    protected float prepAttLen[] = {.6f};
    protected float prepAtt = 0f;
    protected float hitBackIF = 0f;
    protected boolean hitBackImm = false;

    // Attack
    private Action action = Action.MOVE;
    protected float attLen[] = {.2f};
    protected float att = 0f;
    protected float attPush = 0f;
    protected float attPushDiv = 1f;
    private Vd2 attDir = new Vd2();
    private boolean hitAlready = false;

    // Move action
    protected float moveLen = 2f;
    protected float moveActionSpeed = 75;
    private float moveTimer = 0f;

    // Stand action
    protected float standLen = 1f;
    private float standTimer = 0f;

    // Attack delay
    protected float attDelLen[] = {.4f};
    protected float attDel = 0f;

    // Attack colliders
    private Vd2 defColOffset = null;
    private Vd2 defColSize = null;
    protected float vertColSizeMult = .5f;

    /**
     * Table of luck deciding which action will enemy take.<br>
     * Order - attack, throw, move, stand<br>
     * 0 omits option from table<br>
     */
    protected int actionTable[] = {5, 0, 6, 7};
    protected int maxAction = 7;

    // Throwable
    protected Runnable createThrowable = null;
    protected float throwPrepLen = 1f;
    protected float throwLen = 1f;
    protected float throwDel = 1f;

    // Animations
    protected String idleAnim = "goblin_idle";
    protected String moveAnim = "goblin_right";
    protected String[] attAnim = {"goblin_att_right"};
    protected String[] attUpAnim = {"goblin_att_up"};
    protected String[] attDownAnim = {"goblin_att_down"};
    protected String throwAnim = null;

    // Loot
    protected int maxCoin = 0;
    protected int coinCost = 1;
    private int coin = 0;

    public Enemy(String _tag, Vd2 _pos, Vd2 _size, int _hp) {
        super(_tag, _pos, _size, _hp, true);
        wanderTimer = (float)Math.random()*wanderDel;
        defPos = new Vd2(_pos);
        originalSize = new Vd2(_size);
        setCoin(3);
    }

    /**
     * Sets maximum amount of coined dropped by this enemy<br>
     * and chooses random amount to be dropped on death
     */
    public void setCoin(int _maxCoin) {
        maxCoin = _maxCoin;
        coin = MathFuncs.randInt(maxCoin) + 1;
    }

    private void prepareAttack() {
        mov.zero();
        prepAtt -= Time.DeltaTime();
        if (prepAtt < 0) {
            if (action == Action.ATTACK) {
                if (curCombo >= attLen.length) {
                    att = attLen[attLen.length-1];
                } else {
                    att = attLen[curCombo];
                }
            } else if (action == Action.THROW) {
                if (createThrowable != null) {
                    createThrowable.run();
                    att = throwLen;
                }
            }

            mov = attDir;
            mov.normalize();
            mov.mult(attPush);
            mov.mult(Time.DeltaTime());
        }
    }

    private void attack() {
        att -= Time.DeltaTime();

        float div = attPushDiv * Time.DeltaTime();
        mov.div((div >= 1) ? div : 1);

        if (att < 0) {
            if (action == Action.ATTACK) {
                ++curCombo;
                getAttCol().setTrigger(true);
                if (curCombo >= attDelLen.length) {
                    attDel = attDelLen[attDelLen.length-1];
                } else {
                    attDel = attDelLen[curCombo];
                }
            } else if (action == Action.THROW) {
                attDel = throwDel;
            }
            return;
        }
        if (action == Action.ATTACK) {
            getAttCol().setTrigger(false);
        }
    }

    private void postAttack() {
        if (action == Action.ATTACK) {
            getAttCol().setOffset(defColOffset);
            getAttCol().setSize(defColSize);
        }
        attDel -= Time.DeltaTime();
    }

    private void startAttack() {
        if (action == Action.ATTACK) {
            if (curCombo >= prepAttLen.length) {
                prepAtt = prepAttLen[prepAttLen.length-1];
            } else {
                prepAtt = prepAttLen[curCombo];
            }
        } else if (action == Action.THROW) {
            prepAtt = throwPrepLen;
        }

        attDir = new Vd2(mov);
        hitAlready = false;

        if (Math.abs(mov.x) > Math.abs(mov.y)) {
            // Attack horizontally
            if (mov.x > 0) {
                setFlip(false);
                if (action == Action.ATTACK) {
                    getAnim().playAnim(attAnim[curCombo], false);
                } else if (action == Action.THROW) {
                    getAnim().playAnim(throwAnim, false);
                }
            } else {
                setFlip(true);
                if (action == Action.ATTACK) {
                    getAnim().playAnim(attAnim[curCombo], false);
                    getAttCol().setOffset(new Vd2(
                                defColOffset.x -
                                getCol().getSize().x -
                                getAttCol().getSize().x,

                                defColOffset.y)
                            );
                } else if (action == Action.THROW) {
                    getAnim().playAnim(throwAnim, false);
                }
            }
        } else {
            // Attack vertically
            if (mov.y > 0) {
                setFlip(false);
                if (action == Action.ATTACK) {
                    getAnim().playAnim(attDownAnim[curCombo], false);
                    getAttCol().setSize(new Vd2(
                                defColSize.y,
                                defColSize.x * vertColSizeMult
                                ));
                    getAttCol().setOffset(new Vd2(
                                getCol().getOffset().x + getCol().getSize().x / 2 - defColSize.y/2,
                                getCol().getOffset().y + getCol().getSize().y
                                ));
                } else if (action == Action.THROW) {
                    getAnim().playAnim(throwAnim, false);
                }
            } else {
                setFlip(false);
                if (action == Action.ATTACK) {
                    getAnim().playAnim(attUpAnim[curCombo], false);
                    getAttCol().setSize(new Vd2(
                                defColSize.y,
                                defColSize.x * vertColSizeMult
                                ));
                    getAttCol().setOffset(new Vd2(
                                getCol().getOffset().x + getCol().getSize().x / 2 - defColSize.y/2,
                                getCol().getOffset().y - getAttCol().getSize().y
                                ));
                } else if (action == Action.THROW) {
                    getAnim().playAnim(throwAnim, false);
                }
            }
        }

    }

    private void wander() {
        if (wanderTimer <= 0) {
            if (wanderPos.isZero()) {
                // Get new wander pos
                wanderPos.x = Math.random()*2 - 1;
                wanderPos.y = Math.random()*2 - 1;
                wanderPos.mult(Math.random()*wanderDist);
            } else {
                wanderPos.zero();
            }
            wanderTimer = (float)Math.random() * wanderDel;
            mov.zero();
        } else {
            wanderTimer -= Time.DeltaTime();
            if (!wanderPos.isZero()) {
                Vd2 dist = Vd2.sub(Vd2.add(defPos, wanderPos), getPos());
                if (dist.length() > 1) {
                    mov = Vd2.mult(Vd2.normalized(dist), Time.DeltaTime() * wanderSpeed);

                    if (mov.x < 0) setFlip(true);
                    else setFlip(false);
                    getAnim().playAnim(moveAnim, true);
                    return;
                }
            }
            getAnim().playAnim(idleAnim, true);
            mov.zero();
        }
    }

    private void move() {
        if (target == null) {
            wander();
            return;
        }

        mov = Vd2.sub(target.getCol().getCenter(), getCol().getCenter());

        // Check if enemy sees target or target is too far
        if (mov.length() > seeDist * ((inCombat) ? 2 : 1)) {
            inCombat = false;
            if (this instanceof Npc && ((Npc)this).getIsBoss()) {
                Ui.setBoss(null);
            }
            wander();
            return;
        }

        // Stop attacking if target is dead
        if (!target.isAlive()) {
            inCombat = false;
            target = null;
            if (this instanceof Npc && ((Npc)this).getIsBoss()) {
                Ui.setBoss(null);
            }
            wander();
            return;
        }

        inCombat = true;

        // Check if enemy should attack
        if (mov.length() < attDist) {
            if (this instanceof Npc && ((Npc)this).getIsBoss()) {
                Ui.setBoss(this);
            }

            // Decide move
            int choice = (int)(Math.random()*maxAction);
            for (int i = 0; i < actionTable.length; ++i) {
                if (actionTable[i] > 0 && choice < actionTable[i]) {
                    if (i == 0) {
                        action = Action.ATTACK;
                        curCombo = (int)(Math.random()*maxCombo)%maxCombo;
                        break;
                    } else if (i == 1) {
                        action = Action.THROW;
                        break;
                    } else if (i == 2) {
                        action = Action.MOVE;
                        mov = Vd2.sub(getCol().getCenter(), target.getCol().getCenter());

                        getAnim().playAnim(moveAnim, true);
                        if (mov.x > 0) {
                            setFlip(false);
                        } else {
                            setFlip(true);
                        }

                        mov.normalize();
                        moveTimer = moveLen;
                        break;
                    } else if (i == 3) {
                        action = Action.STAND;
                        getAnim().playAnim(idleAnim, true);
                        mov.zero();
                        standTimer = standLen;
                        break;
                    }
                }
            }

            if (action == Action.ATTACK || action == Action.THROW) {
                startAttack();
                mov.zero();
            }

            return;
        }

        mov.normalize();
        mov.mult(speed);
        mov.mult(Time.DeltaTime());

        if (mov.x > 0) {
            setFlip(false);
            getAnim().playAnim(moveAnim, true);
        } else if (mov.x < 0) {
            setFlip(true);
            getAnim().playAnim(moveAnim, true);
        }
    }

    @Override
    public void update() {
        if (!alive) return;
        if (originalCol == null) {
            originalCol = new Collider(getCol().getOffset(), getCol().getSize());
        }

        if (target == null && getTag().equals("enemy")) {
            target = ObjectHandler.getObject("player");
        }

        if (getAttCol() != null) {
            if (defColOffset == null) {
                defColOffset = getAttCol().getOffset();
            }
            if (defColSize == null) {
                defColSize = getAttCol().getSize();
            }
        }

        if (hitTimer > 0f) {
            hitTimer -= Time.DeltaTime();
            mov.mult(hitBackDiv);
        } else if (prepAtt > 0) {
            prepareAttack();
        } else if (att > 0) {
            attack();
        } else if (attDel > 0) {
            postAttack();
        } else if (moveTimer > 0) {
            if (!getAnim().getAnimTag().equals(moveAnim)) {
                getAnim().playAnim(moveAnim, true);
            }
            moveTimer -= Time.DeltaTime();
            setVelocity(Vd2.mult(mov, speed * .75f * Time.DeltaTime()));
        } else if (standTimer > 0) {
            standTimer -= Time.DeltaTime();
        } else {
            move();
        }

        if (moveTimer <= 0 || hitTimer > 0) {
            setVelocity(mov);
        }
    }

    @Override
    public void onHit(Vd2 pos, Object attacker) {
        if (!alive) return;

        if ((prepAtt <= 0 || prepAtt > hitBackIF) && !hitBackImm) {
            target = attacker;
            getAnim().playAnim(idleAnim, true);
            hitTimer = .5f;
            mov = Vd2.mult(Vd2.normalized(Vd2.sub(getCol().getCenter(), pos)), 15);
            att = 0;
            prepAtt = 0;
            attDel = 0;
        }

        if (getTag().equals("npc")) {
            ((Npc)this).onHit(attacker);
        }
    }

    @Override
    public void onAttTrigger(Object obj) {
        if (!obj.getTag().equals(this.getTag()) && !hitAlready) {
            hitAlready = true;
            obj.hit(30, getCol().getCenter(), this);
        }
    }

    public void reset() {
        if (originalCol != null) {
            getCol().setSize(originalCol.getSize());
            getCol().setOffset(originalCol.getOffset());
        }
        if (originalSize != null) {
            setSize(originalSize);
        }

        if (!(this instanceof Npc) || (this instanceof Npc && !((Npc)this).isFriendly())) {
            setTag("enemy");
        }

        if (this instanceof Npc && !isAlive()) {
            onDeath();
        } 

    }

    /**
     * Use when npc should be dead after loading
     */
    public void simulateDeath() {
        getAnim().playAnim("death", false);
        getAnim().jumpEnd();
        setTag("object");
        setSize(Vd2.mult(getSize(), .5f));
        setPos(Vd2.add(getPos(), Vd2.mult(getSize(), .5f)));
        getCol().setSize(Vd2.mult(getCol().getSize(), .5f));
        getCol().setOffset(Vd2.sub(getCol().getOffset(), Vd2.mult(getCol().getSize(), 1.5f)));
    }

    @Override
    public void onDeath() {
        alive = false;
        mov.zero();
        getAnim().playAnim("death", false);
        if (getAttCol() != null) {
            getAttCol().setTrigger(true);
        }
        setTag("object");
        setSize(Vd2.mult(getSize(), .5f));
        setPos(Vd2.add(getPos(), Vd2.mult(getSize(), .5f)));
        getCol().setSize(Vd2.mult(getCol().getSize(), .5f));
        getCol().setOffset(Vd2.sub(getCol().getOffset(), Vd2.mult(getCol().getSize(), 1.5f)));

        // Drop coins
        for (int i = 0; i < coin; ++i) {
            Vd2 dir = new Vd2(Math.random()*2 - 1, Math.random()*2 - 1);

            // Create coin
            Coin obj = new Coin("throwable", Vd2.sub(getCol().getCenter(), new Vd2(32, 32)), new Vd2(16, 16), 1, 1f, (float)(100*Math.random()), dir, 0, coinCost);
            SDL_Rect a = new SDL_Rect();
            a.y = 0;
            a.w = 16;
            a.h = 16;

            String text = null;
            if (coinCost < 10) {
                a.x = 32;
                text = "copper";
            } else if (coinCost < 50) {
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
    }

    private enum Action {
        MOVE,
        STAND,
        ATTACK,
        THROW
    }
}
