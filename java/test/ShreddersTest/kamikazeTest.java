package ShreddersTest;

import battlecode.common.*;
import shreddersCopy.BabyRat;

public class Kamikaze extends BabyRat {

    public Kamikaze(RobotController rc) {
        super(rc);
    }

    @Override
    public void doAction() throws GameActionException {

        // 0) If we can attack adjacent, do it first.
        Direction d = rc.getLocation().directionTo;
        for (Direction dir : directions) {
            MapLocation adj = rc.getLocation().add(dir);
            if (rc.canAttack(adj)) {
                rc.attack(adj);
                rc.setIndicatorString("KAMI attack");
                return;
            }
        }