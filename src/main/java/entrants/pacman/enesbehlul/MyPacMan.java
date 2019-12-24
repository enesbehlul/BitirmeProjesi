package entrants.pacman.enesbehlul;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.info.GameInfo;
import pacman.game.internal.Maze;
import pacman.game.internal.Node;

import java.util.Random;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */
public class MyPacMan extends PacmanController {
    private MOVE myMove = Constants.MOVE.NEUTRAL;
    static int current, temp,random, MIN_DISTANCE = 20, counter = 0;
    static int targetPill, activeTargetPill, ghostLocation;

    boolean isInclude(int[] list, int element){
        for (int i = 0; i<list.length; i++){

            if (element == list[i]){
                System.out.println("VARVARVARVARVARVARVARVARVARVARVAR");
                return true;
            }
        }
        return false;
    }

    private MOVE getRandomMove(MOVE[] possibleMoves){
        for (Constants.MOVE move : possibleMoves){
            System.out.println(move);
        }
        System.out.println("********************************************");
        random = new Random().nextInt(possibleMoves.length);
        return possibleMoves[random];
    }

    public MOVE getMove(Game game, long timeDue) {
        //Place your game logic here to play the game as Ms Pac-Man
        current = game.getPacmanCurrentNodeIndex();

        System.out.println("pacman: "+current);
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If can't see these will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    if (game.getShortestPathDistance(current, ghostLocation) < MIN_DISTANCE) {
                        //System.out.println("Evading Ghost");
                        System.out.println("45 45 45 45 45 45");
                        return game.getNextMoveAwayFromTarget(current, ghostLocation, Constants.DM.PATH);
                    }
                }
            }
            if (game.getGhostEdibleTime(ghost) > 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    //System.out.println("Hunting Ghost");
                    System.out.println("54 54 54 54 54 54 54");
                    return game.getNextMoveTowardsTarget(current, ghostLocation, Constants.DM.PATH);
                }
            }
        }
        //eger pacman sabitse(bir onceki konumu ile ayni yerdeyse
        if (current == temp){
            return getRandomMove(game.getPossibleMoves(current));
        } else if (current != temp){
            try {
                activeTargetPill = game.getActivePillsIndices()[0];
                System.out.println("target: " + activeTargetPill);
                System.out.println("63 63 63 63 63");
                return game.getNextMoveTowardsTarget(current, activeTargetPill, Constants.DM.PATH);
            } catch (ArrayIndexOutOfBoundsException e){
                targetPill = game.getPillIndices()[0];
                System.out.println("ex diger listedeki target: " + targetPill);
                if (targetPill == 0 && game.isJunction(current)){
                    return getRandomMove(game.getPossibleMoves(current));
                }
            }
        }

        temp = current;
        System.out.println("random selected");
        if (game.isJunction(current))
            return getRandomMove(game.getPossibleMoves(current));
        return null;
    }
}