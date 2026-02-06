package shredders;

import battlecode.common.*;

public class RatKing extends RobotPlayer{
        public static void runRatKing(RobotController rc) throws GameActionException {
        int currentCost = rc.getCurrentRatCost();

        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        boolean spawn = currentCost <= 10 || rc.getAllCheese() > currentCost + 1900;

        

        for (MapLocation loc : potentialSpawnLocations) {
            if (spawn && rc.canBuildRat(loc)) {
                rc.buildRat(loc);
                break;
            }

            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                break;
            }
        }

        Message[] squeaks = rc.readSqueaks(rc.getRoundNum());

        for (Message msg : squeaks) {
            int rawSqueak = msg.getBytes();

            if (getSqueakType(rawSqueak) != SqueakType.CHEESE_MINE) {
                continue;
            }

            int encodedLoc = getSqueakValue(rawSqueak);

            if (mineLocs.contains(encodedLoc)) {
                continue;
            }

            mineLocs.add(encodedLoc);
            int firstInt = getFirstInt(encodedLoc);
            int lastInt = getLastInt(encodedLoc);

            rc.writeSharedArray(2 * numMines + 2, firstInt);
            rc.writeSharedArray(2 * numMines + 3, lastInt);
            System.out.println("Writing to shared array: " + firstInt + ", " + lastInt);
            System.out.println("Cheese mine located at: " + getX(encodedLoc) + ", " + getY(encodedLoc));

            numMines++;
        }

        // moveRandom(rc);

        // TODO make more efficient and expand communication in the communication lecture
        rc.writeSharedArray(0, rc.getLocation().x);
        rc.writeSharedArray(1, rc.getLocation().y);

    }

    // Move in a straight line until we bump into something
    // then turn to a new direction
    public static void moveRandom(RobotController rc) throws GameActionException {

        if (d == null) {
            d = directions[rand.nextInt(directions.length-1)];
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
        } else {
            d = directions[rand.nextInt(directions.length-1)];
            if (rc.canTurn()) {
                rc.turn(d);
            }
        }

    }


}
