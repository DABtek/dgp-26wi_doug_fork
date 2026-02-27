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
            Direction toEnemy = rc.getLocation().directionTo(enemyLoc);              
            if (rc.canTurn(toEnemy)) 
                rc.turn(toEnemy);
            
            if (enemyLoc != null && rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
                rc.setIndicatorString("Attacking!");
                return;
        }
    }
        MapLocation nextLoc = rc.adjacentLocation(rc.getDirection());
        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
            return;
        }
        if (enemyLoc != null) {  
        Direction toEnemy = rc.getLocation().directionTo(enemyLoc);     
        if (rc.canMove(toEnemy)) {
            rc.move(toEnemy);
            rc.setIndicatorString("Finding enemyLoc.");
            return;
        } else {
            Direction left = toEnemy.rotateLeft();
            Direction right = toEnemy.rotateRight();
            if (rc.canMove(left)) {
                rc.move(left);
                return; 
            }
            if (rc.canMove(right))  {  
                rc.move(right);
                return;
            }
        }
    }

        if (rc.canMoveForward()) {
            rc.moveForward();
         } else {
                d = directions[rand.nextInt(directions.length)];
                if (rc.canTurn(d)) {
                    rc. turn(d);
                }
            }
        }
    }  

