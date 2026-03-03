package shredders;

import battlecode.common.*;

public class Attacker extends BabyRat {
    public Attacker(RobotController rc) {
        super(rc);
        rc.setIndicatorString("Cat attacker reporting for duty");
    }

    public void doAction() throws GameActionException {
        // search for attackers
        RobotInfo[] nearbyInfos = rc.senseNearbyRobots();

        MapLocation enemyLoc = null;
        for (RobotInfo info : nearbyInfos) {
            if (info.getTeam() != rc.getTeam() && (info.getType().isCatType() || info.getType().isRatKingType())) {
                enemyLoc = info.getLocation();  
                    break; 
                }
            } 
            if (enemyLoc != null) { 
                if (rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
                rc.setIndicatorString("Attacking!");
                return;
        }
            Direction toEnemy = rc.getLocation().directionTo(enemyLoc);

            if (rc.canMove(toEnemy)) {
                rc.move(toEnemy);
                rc.setIndicatorString("Moving to enemy");
                return;
            }

        if (rc.getDirection() != toEnemy && rc.canTurn(toEnemy)) {
            rc.turn(toEnemy);
            rc.setIndicatorString("turning");
            return;
        }
    } 

        MapLocation nextLoc = rc.adjacentLocation(rc.getDirection());
        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
            return;
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            return;
        }

        Direction d = directions[rand.nextInt(directions.length)];
        if (rc.canTurn(d)) rc.turn(d);
    }
}
    