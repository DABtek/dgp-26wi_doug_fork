package teacher10a;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer {

    int numRats;
    final static int SPAWN_COOLDOWN_IN_TURNS = 5;
    int turnsUntilSpawn = 0;
    Set<Integer> kingMakingIds;
    int kingMakingIndex;

    public RatKing(RobotController rc) throws GameActionException {
        super(rc);
        numRats = 0;
        turnsUntilSpawn = 0;
        // In the last slot, read current king count
        int oldRatKingCount = rc.readSharedArray(SHARED_ARRAY_LENGTH-1);
        // and write over an increment including us
        rc.writeSharedArray(SHARED_ARRAY_LENGTH-1, oldRatKingCount + 1);
        kingMakingIds = new HashSet<>();
        kingMakingIndex = 0;
    }

    @Override
    public void doAction() throws GameActionException {
        int currentCost = rc.getCurrentRatCost();


        // The rats will read their IDs in the array to know which tile they should gather
        // on
        // | 0 | 1 | 2 |
        // | 3 | 4 | 5 |
        // | 6 | 7 | 8 |
        if (kingMakingIds.size() < 9) {
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
            for (RobotInfo robot : nearbyRobots) {
                if (robot.type.isBabyRatType() && (robot.team == rc.getTeam())) {
                    if (!kingMakingIds.contains(robot.ID)) {
                        // Mask to lower ten bits in binary 1111111111 
                        rc.writeSharedArray(kingMakingIds.size(), (robot.ID & 0x3FF));
                        System.out.println("Picked baby rat ID " + robot.ID + " to make a king");
                        kingMakingIds.add(robot.ID);
                        if (kingMakingIndex >= 9) {
                            break;
                        }
                    }
                }
            }

        }

        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        boolean spawn = (currentCost <= 10 || rc.getAllCheese() > currentCost + 500) && (turnsUntilSpawn == 0);

        for (MapLocation loc : potentialSpawnLocations) {
            if (spawn && rc.canBuildRat(loc)) {
                turnsUntilSpawn = SPAWN_COOLDOWN_IN_TURNS;
                rc.buildRat(loc);
                numRats += 1;
                rc.writeSharedArray(0, numRats);
                break;
            }

            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                break;
            }
        }

        turnsUntilSpawn -= 1;
        // moveRandom(rc);

    }
    
    

}
