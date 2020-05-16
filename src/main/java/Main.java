
//import entrants.ghosts.enesbehlul.*;
//import examples.StarterPacMan.*;
import examples.StarterGhost.*;
import entrants.pacman.enesbehlul.*;
import pacman.Executor;
import pacman.controllers.IndividualGhostController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.MASController;
import pacman.game.Constants.*;
import pacman.game.internal.POType;
import pacman.game.util.Stats;

import java.util.EnumMap;


/**
 * Created by pwillic on 06/05/2016.
 */
public class Main {

    public static void main(String[] args) {
        Executor executor = new Executor.Builder()
                .setVisual(true)
                //.setPOType(POType.RADIUS)
                .setTickLimit(4000)
                .build();

        EnumMap<GHOST, IndividualGhostController> controllers = new EnumMap<>(GHOST.class);


        controllers.put(GHOST.INKY, new Inky());
        controllers.put(GHOST.BLINKY, new Blinky());
        controllers.put(GHOST.PINKY, new Pinky());
        controllers.put(GHOST.SUE, new Sue());

        //executor.runGame(new MyPacMan(), new MASController(controllers), 3);
        // Pacmani klavyeden yonetebilmek icin
        //executor.runGame(new KlavyeKontrol(new KeyBoardInput()), new MASController(controllers), 40);

        // delay suresini kisaltarak oyunu hizlandiriyoruz
         /*
       for (int i = 0; i < 10; i++){
           executor.runGame(new MyPacMan(), new MASController(controllers), 1);
        }*/

        // /*
        Stats[] stats = executor.runExperiment(new MyPacMan(), new MASController(controllers), 10, "denemeler");
        for (int i = 0; i < stats.length; i++){
            Executor.saveToFile(stats[i].toString(),"deneme" + i +".txt", false);
        } //*/

        // daha sonra oyunu replay yapabilmek icin kaydediyoruz
        //executor.runGameTimedRecorded(new MyPacMan(), new MASController(controllers), "stats");

        //executor.runGameTimed(new InformationSetMCTSPacMan(), new MASController(controllers));
    }
}
