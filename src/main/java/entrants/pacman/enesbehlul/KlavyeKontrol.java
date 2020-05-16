package entrants.pacman.enesbehlul;

import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class KlavyeKontrol extends HumanController {

    public KlavyeKontrol(KeyBoardInput input) {
        super(input);
    }

    @Override
    public MOVE getMove(Game game, long dueTime) {
        System.out.println(game.getPacmanCurrentNodeIndex());
        return super.getMove(game, dueTime);
    }
}
