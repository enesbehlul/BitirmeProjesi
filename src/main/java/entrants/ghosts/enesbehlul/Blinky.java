package entrants.ghosts.enesbehlul;

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants;
import pacman.game.Game;

/**
 * Created by Piers on 11/11/2015.
 */
public class Blinky extends GhostCommunication {
    static int currentGhostLocation, pacmanLocation;

    public Blinky() {
        super(Constants.GHOST.BLINKY);
    }

    /*@Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        //pacman ve hayaletin konum bilgilerini atama
        currentGhostLocation = game.getGhostCurrentNodeIndex(Constants.GHOST.BLINKY);
        pacmanLocation = game.getPacmanCurrentNodeIndex();
        //hayaletimiz pacman'i goruyor mu
        if (pacmanLocation != -1)
            System.out.println(pacmanLocation);
        //donemec noktasi mi
        if(game.isJunction(currentGhostLocation)){
            System.out.println("donemec");
            return game.getNextMoveTowardsTarget(currentGhostLocation, pacmanLocation, Constants.DM.PATH);
        }
        else
            return null;
    }*/
}
