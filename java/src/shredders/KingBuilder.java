package shredders;

import battlecode.common.*;
import teacher05a.BabyRat;

public class KingBuilder extends BabyRat {
    public KingBuilder(RobotController rc) {
        super(rc);
        //TODO Auto-generated constructor stub
    }

    static MapLocation kingLoc = null;

    // RatKing: where we want to camp (prefer cheese mines)
    static MapLocation kingTargetMine = null;

    static boolean secondKingBuilt = false;

    // When team cheese is at/above this value, rally near the king to enable promotions
    public static final int PROMO_RALLY_CHEESE = 50;

    // Start rallying a bit early so units are nearby when we hit 50
    public static final int PROMO_ASSIST_CHEESE = 45;
    // Only assist rally if within this range of the king (prevents map-wide freeze)
    public static final int PROMO_ASSIST_RADIUS2 = 64; // ~8 tiles

    // A subset of non-builders help form the 7-pack near the king when we're close to promotion.
    public static boolean isPromoAssistant(RobotController rc) {
        // 1 out of 2 non-builders assist (tuneable)
        return (rc.getID() % 2) == 0;
    }

    // Only a small subset of babies should rally/hold for promotion.
    // This prevents the entire swarm from freezing around the king.
    public static boolean isKingBuilder(RobotController rc) {
        // 1 out of 5 babies becomes a builder (tuneable)
        return (rc.getID() % 5) == 0;
    }

    // Baby-side promotion: ANY baby can become a Rat King when the engine allows it.
    public static boolean tryBuildKing(RobotController rc) throws GameActionException {
    if (!rc.getType().isBabyRatType()) return false;
    if (!rc.isActionReady()) return false;
    if (rc.getAllCheese() < PROMO_RALLY_CHEESE) return false;
    if (!rc.canBecomeRatKing()) return false;

    rc.becomeRatKing();
    secondKingBuilt = true;
    System.out.println("NEW KING BUILT at round " + rc.getRoundNum() + " by rat " + rc.getID());
    return true;
}

    @Override
    public void doAction() throws GameActionException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAction'");
    }
    
}

