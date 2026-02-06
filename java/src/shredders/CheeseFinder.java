package shredders;

import battlecode.common.*;

public class CheeseFinder extends BabyRat {
    public CheeseFinder(RobotController rc) { 
        super(rc);
        //TODO Auto-generated constructor stub
    }

    public static void runFindCheese(RobotController rc) throws GameActionException {
        // search for cheese
        MapInfo[] nearbyInfos = rc.senseNearbyMapInfos();

        System.out.println("Sensed " + nearbyInfos.length + " tiles");
        MapLocation cheeseLoc = null;
        for (MapInfo info : nearbyInfos) {
            MapLocation loc = info.getMapLocation();
            if (info.getCheeseAmount() > 0) {
                Direction toCheese = rc.getLocation().directionTo(loc);

                if (rc.canTurn(toCheese)) {
                    rc.turn(toCheese);
                    cheeseLoc = info.getMapLocation();
                    break;
                }
            }
            if (info.hasCheeseMine()) {
                mineLoc = info.getMapLocation();
            }
            if (rc.canRemoveDirt(loc)) {
                rc.removeDirt(loc);
            }
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            rc.setIndicatorString("Finding cheese.");
        } else {
            d = directions[rand.nextInt(directions.length-1)];
            if (rc.canTurn()) {
                rc.turn(d);
            }
            rc.setIndicatorString("Blocked while finding cheese, turning " + d.toString());
            return;
        }

        if ((cheeseLoc != null) && rc.canPickUpCheese(cheeseLoc)) {
            rc.pickUpCheese(cheeseLoc);
            rc.setIndicatorString("Picked up cheese; will return");
        }
    }

    @Override
    public void doAction() throws GameActionException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAction'");
    }

}
