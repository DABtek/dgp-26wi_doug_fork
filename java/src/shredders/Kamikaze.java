package shredders;

import battlecode.common.*;

public class Kamikaze extends BabyRat {

    public Kamikaze(RobotController rc) {
        super(rc);
    }

    @Override
    public void doAction() throws GameActionException {

        // 0) If we can attack adjacent, do it first.
        for (Direction dir : directions) {
            MapLocation adj = rc.getLocation().add(dir);
            if (rc.canAttack(adj)) {
                rc.attack(adj);
                rc.setIndicatorString("KAMI attack");
                return;
            }
        }

        // 1) HARD PRIORITY: nearest visible cat
        RobotInfo[] cats = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), Team.NEUTRAL);
        if (cats.length > 0) {
            RobotInfo target = nearest(cats);
            MapLocation t = target.getLocation();
            Direction toT = rc.getLocation().directionTo(t);
            MapLocation next = rc.getLocation().add(toT);

                        // 1.5) Controlled CAT TRAP placement (low frequency, only when it makes sense)
            // Only attempt if cat is not right on top of us (avoid wasting on panic turns)
            int d2 = rc.getLocation().distanceSquaredTo(t);
            if (d2 >= 5 && d2 <= 36) { // ~2 to 6 tiles away
                int gate = (rc.getRoundNum() + rc.getID()) % 11; // knob: lower = more traps
                if (gate == 0) {
                    MapLocation step = rc.getLocation().add(toT);
                    MapLocation left = rc.getLocation().add(toT.rotateLeft());
                    MapLocation right = rc.getLocation().add(toT.rotateRight());

                    if (rc.canPlaceCatTrap(step)) {
                        rc.placeCatTrap(step);
                        rc.setIndicatorString("KAMI trap->CAT");
                        return;
                    } else if (rc.canPlaceCatTrap(left)) {
                        rc.placeCatTrap(left);
                        rc.setIndicatorString("KAMI trapL->CAT");
                        return;
                    } else if (rc.canPlaceCatTrap(right)) {
                        rc.placeCatTrap(right);
                        rc.setIndicatorString("KAMI trapR->CAT");
                        return;
                    }
                }
            }

            // Close distance: dig if blocked, otherwise move
            if (rc.canRemoveDirt(next)) {
                rc.removeDirt(next);
                rc.setIndicatorString("KAMI dig->CAT");
                return;
            }
            if (rc.canMove(toT)) {
                rc.move(toT);
                rc.setIndicatorString("KAMI rush->CAT");
                return;
            }

            // Deadlock breaker
            jitter();
            rc.setIndicatorString("KAMI jitter->CAT");
            return;
        }

        //2) No cat in sight: roam aggressively (toward map center + jitter)
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        stepToward(center);
        rc.setIndicatorString("KAMI roam");
    }

    private RobotInfo nearest(RobotInfo[] arr) {
        RobotInfo best = arr[0];
        int bestD2 = rc.getLocation().distanceSquaredTo(best.getLocation());
        for (int i = 1; i < arr.length; i++) {
            int d2 = rc.getLocation().distanceSquaredTo(arr[i].getLocation());
            if (d2 < bestD2) {
                best = arr[i];
                bestD2 = d2;
            }
        }
        return best;
    }

    private void stepToward(MapLocation target) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        MapLocation next = rc.getLocation().add(dir);

        if (rc.canRemoveDirt(next)) {
            rc.removeDirt(next);
            return;
        }
        if (rc.canMove(dir)) {
            rc.move(dir);
            return;
        }
        jitter();
    }

    private void jitter() throws GameActionException {
        Direction dir = directions[(rc.getID() + rc.getRoundNum()) % directions.length];
        MapLocation next = rc.getLocation().add(dir);

        if (rc.canRemoveDirt(next)) {
            rc.removeDirt(next);
            return;
        }
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}