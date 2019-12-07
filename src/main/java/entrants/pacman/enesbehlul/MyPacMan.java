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
    static int current, temp,random, MIN_DISTANCE = 30;

    public MOVE getMove(Game game, long timeDue) {
        //Place your game logic here to play the game as Ms Pac-Man
        current = game.getPacmanCurrentNodeIndex();

        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If can't see these will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                int ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    if (game.getShortestPathDistance(current, ghostLocation) < MIN_DISTANCE) {
                        //System.out.println("Evading Ghost");
                        return game.getNextMoveAwayFromTarget(current, ghostLocation, Constants.DM.PATH);
                    }
                }
            }
            if (game.getGhostEdibleTime(ghost) > 0) {
                int ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    //System.out.println("Hunting Ghost");
                    return game.getNextMoveTowardsTarget(current, ghostLocation, Constants.DM.PATH);
                }
            }
        }

        if (game.isJunction(current) || current == temp){
            random = new Random().nextInt(MOVE.values().length);
            return MOVE.values()[random];
        }
        temp = current;
        return MOVE.values()[random];

    }
    private boolean a(){
        return true;
    }
    private Boolean b(){
        return true;
    }
}