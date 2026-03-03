package shredders;

import battlecode.common.*;

public class Kamikaze extends BabyRat {

    // ===== Knobs (Kamikaze) =====
    // Trap frequency: lower = MORE traps
    private static final int TRAP_GATE_NORMAL = 11;
    private static final int TRAP_GATE_LONG   = 5;

    // Only trap when cat is within this distance window
    private static final int TRAP_MIN_D2 = 5;   // avoid wasting traps when cat is on top of us
    private static final int TRAP_MAX_D2 = 40;  // don’t trap for super-far cats

    // Long game threshold
    private static final int LONG_GAME_ROUND = 1300;

    public Kamikaze(RobotController rc) {
        super(rc);
    }

    @Override
    public void doAction() throws GameActionException {

        // Always refresh kingLoc if available (cheap and prevents “no transfer” issues)
        updateKingLocFromShared();

        // 0) If we can attack adjacent, do it first.
        for (Direction dir : directions) {
            MapLocation adj = rc.getLocation().add(dir);
            if (rc.canAttack(adj)) {
                rc.attack(adj);
                rc.setIndicatorString("KAMI: attack");
                return;
            }
        }

        // 1) HARD PRIORITY: nearest visible cat
        RobotInfo[] cats = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), Team.NEUTRAL);
        if (cats.length > 0) {
            RobotInfo target = nearest(cats);
            MapLocation t = target.getLocation();
            Direction toT = rc.getLocation().directionTo(t);
            MapLocation step = rc.getLocation().add(toT);

            // 1.1) Controlled CAT TRAP placement (rate-limited, “plausible only”)
            int d2 = rc.getLocation().distanceSquaredTo(t);
            int gateMod = (rc.getRoundNum() >= LONG_GAME_ROUND) ? TRAP_GATE_LONG : TRAP_GATE_NORMAL;
            boolean gate = ((rc.getRoundNum() + rc.getID()) % gateMod) == 0;

            if (gate && d2 >= TRAP_MIN_D2 && d2 <= TRAP_MAX_D2 && rc.isActionReady()) {
                MapLocation left  = rc.getLocation().add(toT.rotateLeft());
                MapLocation right = rc.getLocation().add(toT.rotateRight());

                if (rc.canPlaceCatTrap(step)) {
                    rc.placeCatTrap(step);
                    rc.setIndicatorString("KAMI: trap step");
                    return;
                } else if (rc.canPlaceCatTrap(left)) {
                    rc.placeCatTrap(left);
                    rc.setIndicatorString("KAMI: trap left");
                    return;
                } else if (rc.canPlaceCatTrap(right)) {
                    rc.placeCatTrap(right);
                    rc.setIndicatorString("KAMI: trap right");
                    return;
                }
            }

            // 1.2) Close distance: dig if blocked, otherwise move
            if (rc.canRemoveDirt(step)) {
                rc.removeDirt(step);
                rc.setIndicatorString("KAMI: dig->cat");
                return;
            }
            if (rc.canMove(toT)) {
                rc.move(toT);
                rc.setIndicatorString("KAMI: rush->cat");
                return;
            }

            // 1.3) Deadlock breaker
            jitter();
            rc.setIndicatorString("KAMI: jitter->cat");
            return;
        }

        // 2) No cat in sight: roam aggressively (toward map center + jitter)
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        stepToward(center);
        rc.setIndicatorString("KAMI: roam");
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