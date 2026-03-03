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

    MapLocation kingLocEnemy = null;
    MapLocation catLocEnemy = null;

    for (RobotInfo info : nearbyInfos) {
        if (info.getTeam() == rc.getTeam()) continue;

        if (info.getType().isRatKingType()) {
            kingLocEnemy = info.getLocation();
            break; 
        }

        if (catLocEnemy == null && info.getType().isCatType()) {
            catLocEnemy = info.getLocation(); 
            }
        }

        MapLocation enemyLoc = (kingLocEnemy != null) ? kingLocEnemy :catLocEnemy;
            
        if (enemyLoc == null) {
            MapLocation nextLoc = rc.adjacentLocation(rc.getDirection());

            if (rc.canRemoveDirt(nextLoc)) {
                rc.removeDirt(nextLoc);
                return;
            }

            if (rc.canMoveForward()) {
                rc.moveForward();;
                rc.setIndicatorString("Finding enemyLoc.");
            }
                return;
        }

        if (rc.canAttack(enemyLoc)) {
            rc.attack(enemyLoc);
            rc.setIndicatorString("Attacking!");
            return;
        }

        Direction toEnemy = rc.getLocation().directionTo(enemyLoc);

        if (rc.canTurn(toEnemy)) {
            rc.turn(toEnemy);
            return;
        }

        if (rc.canMove(toEnemy)) {
            rc.move(toEnemy);
            rc.setIndicatorString("Looking for enemyLoc");
            return;
        }

        Direction left = toEnemy.rotateLeft();
        Direction right = toEnemy.rotateRight();
        if (rc.canMove(left)) {
            rc.move(left);
            return;
        }
        if (rc.canMove(right)) {
            rc.move(right);
            return;
        }
    }
}
    

