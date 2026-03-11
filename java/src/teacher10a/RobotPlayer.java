package teacher10a;

import battlecode.common.*;

public class RobotPlayer {


    static RobotSubPlayer rsp;
    static boolean babyRatToggle = false;

    public static BabyRat createToggle(RobotController rc) throws GameActionException {
        int numRats = rc.readSharedArray(0);
        if ((numRats % 3) == 0) {
            return new CheeseFinder(rc);
        } else if ((numRats % 3) == 1) {
            return new CatAttacker(rc);
        } else {
            return new KingMaker(rc);
        }
    }

    public static void run(RobotController rc) {

        try {
            if (rc.getType().isRatKingType()) {
                rsp = new RatKing(rc);
            } else {
                rsp = createToggle(rc);
            }
        } catch (GameActionException e) {
            System.out.println("GameActionException in RobotPlayer:");
            e.printStackTrace();
        }
        while (true) {
            try {
                rsp.doAction();
            } catch (GameActionException e) {
                System.out.println("GameActionException in RobotPlayer:");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in RobotPlayer:");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }
    
}