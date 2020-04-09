
//import entrants.ghosts.enesbehlul.Blinky;
import examples.StarterGhost.Blinky;
//import entrants.ghosts.enesbehlul.Inky;
import examples.StarterGhost.Inky;
//import entrants.ghosts.enesbehlul.Pinky;
import examples.StarterGhost.Pinky;
import examples.StarterGhost.Sue;
//import entrants.ghosts.enesbehlul.Sue;
import entrants.pacman.enesbehlul.MyPacMan;
//import examples.StarterPacMan.MyPacMan;
import pacman.Executor;
import pacman.controllers.IndividualGhostController;
import pacman.controllers.MASController;
import pacman.game.Constants.*;

import java.util.EnumMap;


/**
 * Created by pwillic on 06/05/2016.
 */
public class Main {

    public static void main(String[] args) {
        Executor executor = new Executor.Builder()
                .setVisual(true)
                .setTickLimit(4000)
                .build();

        EnumMap<GHOST, IndividualGhostController> controllers = new EnumMap<>(GHOST.class);

        controllers.put(GHOST.INKY, new Inky());
        controllers.put(GHOST.BLINKY, new Blinky());
        controllers.put(GHOST.PINKY, new Pinky());
        controllers.put(GHOST.SUE, new Sue());

        executor.runGameTimed(new MyPacMan(), new MASController(controllers));

        

    }
}
