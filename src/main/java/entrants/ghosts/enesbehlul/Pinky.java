package entrants.ghosts.enesbehlul;

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants;
import pacman.game.Game;

/**
 * Created by Piers on 11/11/2015.
 */
public class Pinky extends GhostCommunication {

    static int currentGhostLocation, pacmanLocation;

    public Pinky() {
        super(Constants.GHOST.PINKY);
    }

    /*@Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        currentGhostLocation = game.getGhostCurrentNodeIndex(Constants.GHOST.PINKY);
        pacmanLocation = game.getPacmanCurrentNodeIndex();
        if (pacmanLocation != -1)
            System.out.println(pacmanLocation);
        if(game.isJunction(currentGhostLocation)){
            System.out.println("donemec");

            return Constants.MOVE.LEFT;
            //return game.getNextMoveTowardsTarget(currentGhostLocation, pacmanLocation, Constants.DM.PATH);
        }
        else
            return Constants.MOVE.LEFT;
    }*/
}
