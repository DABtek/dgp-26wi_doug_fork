package shredders;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.Random;

public class RobotPlayer {

    public static enum SqueakType {
        INVALID,
        ENEMY_RAT_KING,
        ENEMY_COUNT,
        CHEESE_MINE,
        CAT_FOUND,
    }

    public static SqueakType[] squeakTypes = SqueakType.values();
    public static Direction[] directions = Direction.values();
    public static MapLocation mineLoc = null;
    public static int numMines = 0;
    public static ArrayList<Integer> mineLocs = new ArrayList<>();
    static final Random rand = new Random(6147);
    static Direction d = null;

    static MapLocation kingLoc = null;

    // When team cheese is at/above this value, rally near the king to enable promotions
    public static final int PROMO_RALLY_CHEESE = 50;

    // Only a small subset of babies should rally/hold for promotion.
    // This prevents the entire swarm from freezing around the king.
    public static boolean isKingBuilder(RobotController rc) {
        // 1 out of 5 babies becomes a builder (tuneable)
        return (rc.getID() % 5) == 0;
    }



    public static void run(RobotController rc) {

        while (true) {
        try {
            if (rc.getType().isRatKingType()) {
                runRatKing(rc);
            } else {
                // Read king beacon from shared array each turn (written by the Rat King)
                MapLocation beacon = kingLoc(rc);
                if (beacon != null) {
                    kingLoc = beacon;
                }

                // Try to promote this baby into a new king before doing anything else
                if (tryBuildKing(rc)) {
                    continue; // this robot is now a king; next loop will runRatKing()
                }
               
                
                // Per-robot decision (DO NOT use shared global state)
                int raw = rc.getRawCheese();
                boolean builder = isKingBuilder(rc);
                boolean rally = (builder && kingLoc != null && rc.getAllCheese() >= PROMO_RALLY_CHEESE);

                if (raw > 0 || rally) {
                    runReturnToKing(rc);
                } else {
                    runFindCheese(rc);
                }
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException in RobotPlayer:");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in RobotPlayer:");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
            if (rc.getAllCheese() >= 45) {
    String role = null;
    System.out.println(
        "PROMO? r=" + rc.getRoundNum()
        + " id=" + rc.getID()
        + " role=" + role
        + " cheese=" + rc.getAllCheese()
        + " can=" + rc.canBecomeRatKing()
        + " action=" + rc.isActionReady()
       // + " d2King=" + (kingLoc(rc) == null ? -1 : rc.getLocation().distanceSquaredTo(kingLoc(rc)))
    );
}
        }
    }

    private static MapLocation kingLoc(RobotController rc) throws GameActionException {
        int kingX = rc.readSharedArray(0);
        int kingY = rc.readSharedArray(1);
        // If the king hasn't written yet, return null
        if (kingX == 0 && kingY == 0) return null;
        return new MapLocation(kingX, kingY);
    }

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

    public static void runReturnToKing(RobotController rc) throws GameActionException {
        if (kingLoc == null) {
            // No known king location yet; caller will keep exploring
            rc.setIndicatorString("No king beacon yet");
            return;
        }
        Direction toKing = rc.getLocation().directionTo(kingLoc);
        MapLocation nextLoc = rc.getLocation().add(toKing);
        int rawCheese = rc.getRawCheese();

        // HOLD inside king's 3x3 during promotion rally (builders only)
        if (rc.getAllCheese() >= PROMO_RALLY_CHEESE && isKingBuilder(rc)) {
            int d2 = rc.getLocation().distanceSquaredTo(kingLoc);
            if (d2 <= 2) {
                rc.setIndicatorString("Holding in 3x3 for promotion (builder)");
                return;
            }
        }

        if (rc.canTurn(toKing)) {
            rc.turn(toKing);
        }

        if (rc.canSenseLocation(kingLoc) && (kingLoc.distanceSquaredTo(rc.getLocation()) <= 4 )) {

            RobotInfo[] kingLocations = rc.senseNearbyRobots(kingLoc, 8, rc.getTeam());

            for (RobotInfo robotInfo : kingLocations) {
                if (robotInfo.getType().isRatKingType()) {
                    MapLocation actualKingLoc = robotInfo.getLocation();
                    boolean result = rc.canTransferCheese(actualKingLoc, rawCheese);
                    rc.setIndicatorString("Can transfer " + rawCheese + " to king at " + actualKingLoc.toString() + "? " + result);
                    if (result) {
                        rc.transferCheese(actualKingLoc, rawCheese);

                        // Only builders stick around to hold for promotion; others will explore next turn
                        if (rc.getAllCheese() >= PROMO_RALLY_CHEESE && isKingBuilder(rc)) {
                            rc.setIndicatorString("Transferred; holding for promotion (builder)");
                            // fall through to HOLD/movement logic below
                        } else {
                            rc.setIndicatorString("Transferred; returning to explore");
                            return;
                        }
                    } else {
                        // Can't transfer; caller will decide behavior next turn
                        rc.setIndicatorString("Near king but cannot transfer");
                    }
                    break;
                }
            }

            if (mineLoc != null) {
                int msgBytes = getSqueak(SqueakType.CHEESE_MINE, toInteger(mineLoc));
                rc.squeak(msgBytes);
                mineLoc = null;
            }
        }

        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
        }

        // Move toward the king (simple greedy movement)
        if (rc.canMove(toKing)) {
            rc.move(toKing);
        } else if (rc.canMoveForward()) {
            // Fallback: keep moving forward if we can't move in the desired direction
            rc.moveForward();
        } else {
            // If totally blocked, pick a new direction to avoid deadlock
            Direction turnDir = directions[rand.nextInt(directions.length - 1)];
            if (rc.canTurn(turnDir)) {
                rc.turn(turnDir);
            }
        }
    }

    // Baby-side promotion: THIS baby becomes a new Rat King when the engine allows it.
    public static boolean tryBuildKing(RobotController rc) throws GameActionException {
        if (!rc.getType().isBabyRatType()) return false;
        if (!isKingBuilder(rc)) return false;
        if (!rc.isActionReady()) return false;
        if (rc.getAllCheese() < PROMO_RALLY_CHEESE) return false;
        if (!rc.canBecomeRatKing()) return false;

        rc.becomeRatKing();
        System.out.println("NEW KING BUILT at round " + rc.getRoundNum() + " by rat " + rc.getID());
        return true;
    }

    public static int getFirstInt(int loc) {
        // extract 10 smallest place value bits from toInteger(loc)
        return loc % 1024;
    }

    public static int getLastInt(int loc) {
        // extract bits with place values >= 2^10 from toInteger(loc)
        return loc >> 10;
    }

    public static int toInteger(MapLocation loc) {
        return (loc.x << 6) | loc.y;
    }

    public static int getX(int encodedLoc) {
        return encodedLoc >> 6;
    }

    public static int getY(int encodedLoc) {
        return encodedLoc % 64;
    }

    public static int getSqueak(SqueakType type, int value) {
        switch (type) {
            case ENEMY_RAT_KING:
                return (1 << 12) | value;
            case ENEMY_COUNT:
                return (2 << 12) | value;
            case CHEESE_MINE:
                return (3 << 12) | value;
            case CAT_FOUND:
                return (4 << 12) | value;
            default:
                return value;
        }
    }
    
    public static SqueakType getSqueakType(int rawSqueak) {
        return squeakTypes[rawSqueak >> 12];
    }

    public static int getSqueakValue(int rawSqueak) {
        // Only uses lower 12 bits
        return rawSqueak % 4096;
    }
    
}
