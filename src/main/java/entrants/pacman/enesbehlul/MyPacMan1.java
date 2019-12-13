package entrants.pacman.enesbehlul;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Game;

public class MyPacMan1 extends PacmanController {
    static int pacmanLocation, ghostLocation;

    public MyPacMan1(){

    }

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        pacmanLocation = 1028;
        for (Constants.MOVE move : game.getPossibleMoves(pacmanLocation)){
            System.out.println(move);
        }
        System.out.println("********************************************");
        return null;
    }
}
