package app;

/**
 * RedKnight
 */
public class RedKnight extends Enemy {

    public RedKnight(String _tag, Vd2 _pos, Vd2 _size, int _hp) {
        super(_tag, _pos, _size, _hp);

        maxCombo = 2;
        idleAnim = "red_knight_idle";
        moveAnim = "red_knight_right";
        attAnim = new String[] {"red_knight_att1_right", "red_knight_att2_right"};
        attUpAnim = new String[] {"red_knight_att1_up", "red_knight_att2_up"};
        attDownAnim = new String[] {"red_knight_att1_down", "red_knight_att2_down"};

        prepAttLen = new float[] {1f, .6f};
        attDelLen = new float[] {.6f};
        vertColSizeMult = .8f;
        attPush = 2000f;
        attPushDiv = 150f;

        hitBackDiv = .8f;
        hitBackIF = .8f;

        actionTable = new int[] {10, 0, 0, 14};
        maxAction = 14;
        moveActionSpeed = 75f;

        coinCost = 50;
        setCoin(2);
    }
}
