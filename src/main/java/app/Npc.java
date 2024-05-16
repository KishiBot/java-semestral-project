package app;

import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_E;

import app.Logger.Severity;

/**
 * Npc
 */
public class Npc extends Enemy {

    private String name = "Fendle";
    private Runnable aiUpdate = null;

    private boolean friendly = true;
    private static final double maxInteractDist = 100f;
    private boolean pressedE = false;
    private int friendlyLim = 200;
    private boolean isBoss = false;

    // Dialog parameters
    private boolean activeDialog = false;
    private int curDialogSet = 0;
    private int curDialog = 0;
    private boolean nextDialog = false;

    // Dialogs
    private String[][] dialogs = null;
    private String hitDialog = null;
    private String angryDialog = null;
    private String attackDialog = null;

    private Runnable customDialog = null;

    public Npc(String _tag, Vd2 _pos, Vd2 _size, int _hp) {
        super(_tag, _pos, _size, _hp);
    }

    public boolean getIsBoss() {
        return isBoss;
    }
    public boolean isFriendly() {
        return friendly;
    }
    public void setFriendly(boolean _friendly) {
        friendly = _friendly;
    }

    public String getName() {
        return name;
    }

    public int getDialogSet() {
        return curDialogSet;
    }
    public void setDialogSet(int _dialogSet) {
        curDialogSet = _dialogSet;
    }


    @Override
    public void update() {
        if (inCombat || !friendly) {
            super.update();
        }

        if (friendly) {
            friendlyUpdate();
        }

        // For being able to be hit back
        if (hitTimer > 0f) {
            hitTimer -= Time.DeltaTime();
            mov.mult(hitBackDiv);
        }
        setVelocity(mov);
        return;
    }

    private void dialog() {
        if (customDialog != null) {
            customDialog.run();
            return;
        }

        if (activeDialog) {
            // Continue active dialog
            if (!Ui.skipDialog()) return;

            // Check whether to end current dialog
            if (curDialog == dialogs[curDialogSet].length) {
                Ui.drawDialog(null, name);
                activeDialog = false;
                if (nextDialog) {
                    ++curDialogSet;
                    nextDialog = false;
                }
                if (isBoss) {
                    target = ObjectHandler.getObject("player");
                    friendly = false;
                    inCombat = true;
                    setTag("enemy");
                    Ui.setBoss(this);
                    ObjectHandler.simpleSaveState(false);
                }
                return;
            }

            Ui.drawDialog(dialogs[curDialogSet][curDialog++], name);

        } else {
            // Start new dialog
            if (curDialogSet == dialogs.length) return;

            curDialog = 0;
            Ui.drawDialog(dialogs[curDialogSet][curDialog++], name);
            activeDialog = true;
        }
    }

    private void friendlyUpdate() {
        if (aiUpdate != null) {
            aiUpdate.run();
        }

        double dist = Vd2.sub(getCol().getCenter(),
                ObjectHandler.getObject("player").getCol().getCenter()).length();

        // Player interaction
        if (dist > maxInteractDist) {
            if (activeDialog) {
                Ui.drawDialog(null, name);
                activeDialog = false;
            }
            return;
        }

        if (Input.getKey(SDLK_E) && !pressedE) {
            pressedE = true;
            dialog();
        } else if (pressedE && !Input.getKey(SDLK_E)) {
            pressedE = false;
        } else if (!pressedE) {

            // Draw e button above
            if (!activeDialog) {
                Ui.drawNpcE(Vd2.add(
                    getCol().getPos(),
                    new Vd2(getCol().getSize().x/2, -100)));
            }
        }
    }

    protected void onHit(Object attacker) {
        if (attacker.getTag().equals("player")) {
            if (getHp() < friendlyLim) {
                if (friendly) {
                    Ui.drawMessage(angryDialog, this);
                    activeDialog = false;
                }

                target = ObjectHandler.getObject("player");
                friendly = false;
                inCombat = true;
                setTag("enemy");

                if (isBoss) {
                    Ui.setBoss(this);
                }
                ObjectHandler.simpleSaveState(false);
            } else {
                Ui.drawMessage(hitDialog, this);
                activeDialog = false;
            }
        } else if (attacker.getTag().equals("enemy")) {
            if (!inCombat) {
                Ui.drawMessage(attackDialog, this);
                target = attacker;
            }
            inCombat = true;
        }
    }

    @Override
    public void onDeath() {
        if (isBoss) {
            Ui.setBoss(null);
        }
        super.onDeath();
    }

    public void setNpcAi(String _name) {
        name = _name;
        switch (name) {
            case "Fendle":
            FendelAi();
            break;
            case "Rendle":
            RendleAi();
            break;
            case "post":
            postAi();
            break;
            case "anvil":
            anvilAi();
            break;
            default:
            Logger.log(Severity.WARNING, "Npc", "Unknown npc: " + name);
            break;
        }
    }

    private void FendelAi() {
        setMaxHp(5000);
        dialogs = new String[][] {
        {
                "Hello there traveler.",
                "I am the mighty knight\nof the brigde Fendle!",
                "Be careful on your way.\nThere is a lot of nasty\ncreatures lurking around.",
                "There is also a goblin\ncamp just south of here.",
                "I would advise you stay\nclear of there."},
        {
                "By the way.",
                "Do you see that post\ndown there?",
                "I have heard that those\nlost always find their\nway to one of those."}
        };

        hitDialog = "Have you gone mad?";
        angryDialog = "As you wish. Prepare yourself TRAVELER";
        attackDialog = "Prepare to die monster!";

        maxCombo = 2;
        idleAnim = "knight_idle";
        moveAnim = "knight_right";
        attAnim = new String[] {"knight_att1_right", "knight_att2_right"};
        attUpAnim = new String[] {"knight_att1_up", "knight_att2_up"};
        attDownAnim = new String[] {"knight_att1_down", "knight_att2_down"};

        prepAttLen = new float[] {1f, .6f};
        attDelLen = new float[] {.6f};
        vertColSizeMult = .8f;
        attPush = 2000f;
        attPushDiv = 150f;
        friendlyLim = 200;

        hitBackDiv = .8f;
        hitBackIF = .8f;

        actionTable = new int[] {10, 0, 0, 13};
        maxAction = 13;
        moveActionSpeed = 75f;

        aiUpdate = new Runnable() {
            @Override
            public void run() {
                if (curDialogSet == 0 && curDialog == dialogs[curDialogSet].length) {
                    nextDialog = true;
                }
            }
        };

        coinCost = 50;
        setCoin(50);
    }

    private void RendleAi() {
        setMaxHp(20000);
        dialogs = new String[][] {
        {"So another one came"}
        };

        hitDialog = "";
        angryDialog = "Then i take it you are prepared.";
        attackDialog = "Prepare to die monster!";

        maxCombo = 2;
        idleAnim = "knight_idle";
        moveAnim = "knight_right";
        attAnim = new String[] {"knight_att1_right", "knight_att2_right"};
        attUpAnim = new String[] {"knight_att1_up", "knight_att2_up"};
        attDownAnim = new String[] {"knight_att1_down", "knight_att2_down"};

        prepAttLen = new float[] {1f, .6f};
        attDelLen = new float[] {.6f};
        vertColSizeMult = .8f;
        attPush = 2000f;
        attPushDiv = 75f;
        friendlyLim = getMaxHp();

        hitBackDiv = .8f;
        hitBackImm = true;
        isBoss = true;

        actionTable = new int[] {10, 0, 11, 14};
        maxAction = 14;
        moveActionSpeed = 75f;

        coinCost = 50;
        setCoin(100);
    }

    private void postAi() {
        setMaxHp(10000);
        dialogs = new String[][] {};

        idleAnim = null;
        actionTable = new int[] {0, 0, 0, 0};
        speed = 0;
        hitBackImm = true;

        setCoin(1);

        customDialog = new Runnable() {
            @Override
            public void run() {
                ObjectHandler.saveState(true);
            }
        };
    }

    private void anvilAi() {
        setMaxHp(10000);
        dialogs = new String[][] {};

        idleAnim = null;
        actionTable = new int[] {0, 0, 0, 0};
        speed = 0;
        hitBackImm = true;

        setCoin(5);

        customDialog = new Runnable() {
            @Override
            public void run() {
                Player player = (Player)ObjectHandler.getObject("player");
                if (player.getLevel() >= Player.maxLevel) {
                    Ui.drawMessage("Max level reached", Npc.this);
                    return;
                }
                if (player.getCoinNum() >= Player.levelCost[player.getLevel()]) {
                    player.setCoinNum(player.getCoinNum() - Player.levelCost[player.getLevel()]);
                    player.setLevel(player.getLevel()+1);
                    Ui.drawMessage("Sword leveled up", Npc.this);
                } else {
                    Ui.drawMessage(Player.levelCost[player.getLevel()] + " coins needed", Npc.this);
                }
            }
        };
    }
}

