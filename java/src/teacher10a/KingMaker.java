package teacher10a;

import battlecode.common.*;

// KingMakers are a special kind of cheese finder 
// that wait after they 
public class KingMaker extends CheeseFinder {

    public KingMaker(RobotController rc) {
        super(rc);
    }

    boolean becomingKing = false;
    MapLocation kingMakingDest;

    /**
     * Get a MapLocation within the 3x3 kingmaking grid based on index i
     * @param i - index of shared array where we found our ID
     * @return
     */
    private MapLocation getKingMakingDest(int i) {
        // | 0 | 1 | 2 |
        // | 3 | 4 | 5 |
        // | 6 | 7 | 8 |
        return new MapLocation(mineLoc.x + (i % 3) - 1,  mineLoc.y + (i / 3) - 1);
    }

    private void senseBecomingKing() throws GameActionException {
        for (int i = 0; i < 9; i += 1) {
            int id = rc.readSharedArray(i);
            if ((id & 0x3FF) == rc.getID()) {
                becomingKing = true;
                kingMakingDest = getKingMakingDest(i);
                rc.setIndicatorString("Received orders to become a new king at " + kingMakingDest);
                return;
            }
        }
    }

    protected void runFindingCheese() throws GameActionException {
        if (mineLoc != null) {
            senseBecomingKing();
        }
        super.runFindCheese();
    }
/*
    protected void runReturnToKing() throws GameActionException {
        if (becomingKing) {
            rc.setIndicatorString("Becoming king");
            //bugNav = new BugNav()
        } else {
            super.runReturnToKing();
        }
    }

*/

}
