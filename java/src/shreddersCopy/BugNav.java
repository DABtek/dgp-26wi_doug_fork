package shredders;

import battlecode.common.*;

public class BugNav {

    enum Mode {
        TRACING_FARTHER,
        TRACING_CLOSER,
        STRAIGHT
    }

    Mode mode;
    MapLocation dest;

    Direction d;

    Direction destDirection;
    Direction scheduledTurn = null;

    int previousDistance;

    MapInfo[] neighbors;

    public BugNav(MapLocation dest, RobotController rc) throws GameActionException {
        mode = Mode.TRACING_FARTHER;
        this.dest = dest;
        MapLocation here = rc.getLocation();
        d = here.directionTo(dest);
        System.out.println("Started bugnav " + here.toString() + " heading toward " + dest.toString());
        neighbors = new MapInfo[RobotSubPlayer.directions.length];
        scheduledTurn = leftDirection(rc.getDirection());
        previousDistance = here.distanceSquaredTo(dest);
    }

    public static Direction leftDirection(Direction d) {
        int leftIndex = d.ordinal() - 1;
        if (leftIndex < 0) {
            leftIndex = leftIndex + RobotSubPlayer.directions.length;
        }
        return RobotSubPlayer.directions[leftIndex];
    }

    public static Direction rightDirection(Direction d) {
        int rightIndex = d.ordinal() + 1;
        if (rightIndex >= RobotSubPlayer.directions.length) {
            rightIndex = rightIndex - RobotSubPlayer.directions.length;
        }
        return RobotSubPlayer.directions[rightIndex];
    }

    public Direction senseNeighborsForNewTurnDirection(MapInfo[] infos, MapLocation here, Direction forward) {

        for (MapInfo info : infos) {
            MapLocation infoLoc = info.getMapLocation();
            if (!infoLoc.isAdjacentTo(here)) {
                continue;
            }
            Direction d = here.directionTo(infoLoc);
            neighbors[d.ordinal()] = info;
            System.out.println("Filling out neighbor " + d.toString() + " with " + info.getMapLocation().toString());
        }

        Direction left = leftDirection(forward);
        Direction right = rightDirection(forward);
        System.out.println("Left direction " + left.toString());
        System.out.println("Right direction " + right.toString());
        MapInfo leftNeighbor = neighbors[left.ordinal()];
        MapInfo rightNeighbor = neighbors[right.ordinal()];
        if (leftNeighbor.isPassable()) {
            return left;
        } else {
            Direction d = forward;
            MapInfo neighbor = null;
            do {
                d = leftDirection(d);
                neighbor = neighbors[d.ordinal()];
            } while (!neighbor.isPassable());

            return d;
        }
    }

    public boolean move(RobotController rc) throws GameActionException {
        if (!rc.canTurn()) {
            return false;
        }
        if (scheduledTurn != null) {
            System.out.println("Doing scheduled turn to  " + scheduledTurn.toString());
            rc.turn(scheduledTurn);
            scheduledTurn = null;
        }

        MapLocation here = rc.getLocation();
        Direction forward = rc.getDirection();
        MapLocation forwardLoc = here.add(forward);
        int forwardDist = forwardLoc.distanceSquaredTo(dest);
        System.out.println("Forward distance squared " + forwardDist);

        switch (mode) {
            case Mode.STRAIGHT:
                while (rc.canMoveForward()) {
                    rc.moveForward();
                }
                mode = Mode.TRACING_FARTHER;
                return false;
            case Mode.TRACING_FARTHER:
                MapInfo[] infos = rc.senseNearbyMapInfos(2);

                if (forwardDist < previousDistance) {
                    mode = Mode.TRACING_CLOSER;
                    return false;
                }
                Direction newDirection = senseNeighborsForNewTurnDirection(infos, here, forward);
                rc.setIndicatorString(("Bugnav, tracing with direction " + newDirection));

                if (forward != newDirection) {
                    if (rc.canTurn()) {
                        d = newDirection;
                        rc.turn(newDirection);
                    }
                }

                if (rc.canMoveForward()) {
                    rc.moveForward();
                }
                break;
            case TRACING_CLOSER:
                int straightDist = here.distanceSquaredTo(dest);
                System.out.println("Straight distance squared " + straightDist);
                if (straightDist < forwardDist) {
                    mode = Mode.STRAIGHT;
                    d = here.directionTo(dest);
                    if (rc.canTurn()) {
                        rc.turn(d);
                    }
                    return true;
                }
                break;
        }
        return false;
    }

}