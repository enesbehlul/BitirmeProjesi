package entrants.ghosts.enesbehlul;

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.Random;

/**
 * Created by Piers on 11/11/2015.
 */
public class Sue extends IndividualGhostController{
    GhostCommunication ghostCommunication;
    static int currentGhostLocation, pacmanLocation;

    public Sue() {
        super(Constants.GHOST.SUE);
        ghostCommunication = new GhostCommunication(Constants.GHOST.SUE);
    }

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        return ghostCommunication.getMove(game, timeDue);
    }

    /*public Constants.MOVE getMove(Game game, long timeDue) {

        //pacman ve hayaletin konum bilgilerini atama
        currentGhostLocation = game.getGhostCurrentNodeIndex(ghost);

        if (game.isJunction(currentGhostLocation)){
            possibleMoves = game.getPossibleMoves(currentGhostLocation);
            a = rand.nextInt(possibleMoves.length);
            System.out.println(possibleMoves[a]);
            return possibleMoves[a];

        }
        return null;
    }*/
}
