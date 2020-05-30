
//import entrants.ghosts.enesbehlul.*;
//import examples.StarterPacMan.*;
import examples.StarterGhostComm.*;
import entrants.pacman.enesbehlul.*;
import pacman.Executor;
import pacman.controllers.IndividualGhostController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.MASController;
import pacman.game.Constants.*;
import pacman.game.internal.POType;
import pacman.game.util.Stats;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;


/**
 * Created by pwillic on 06/05/2016.
 */
public class Main {

    public static void main(String[] args) {
        Executor executor = new Executor.Builder()
                .setVisual(true)
                //.setPOType(POType.RADIUS)
                //.setPacmanPO(false)
                .setTickLimit(130000)
                .build();

        EnumMap<GHOST, IndividualGhostController> controllers = new EnumMap<>(GHOST.class);

        controllers.put(GHOST.INKY, new Inky());
        controllers.put(GHOST.BLINKY, new Blinky());
        controllers.put(GHOST.PINKY, new Pinky());
        controllers.put(GHOST.SUE, new Sue());

        Statistics statistics = new Statistics();

        statistics.calculateStatisticsFromFile("belgeler/1000tumSonuclarSirali32330.txt");

        //executor.runGame(new MyPacMan(), new MASController(controllers), 3);
        // Pacmani klavyeden yonetebilmek icin
        //executor.runGame(new KlavyeKontrol(new KeyBoardInput()), new MASController(controllers), 40);

        /*


        int loop = 10;
        int totalScore = 0, currentScore = 0;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int average;
        int maxIndex = -1;
        for (int i = 0; i < loop; i++){
            currentScore = executor.runGame(new MyPacMan1(), new MASController(controllers), 0);
            statistics.tumSonuclar.add(currentScore);
            totalScore += currentScore;
            if (currentScore > max){
                max = currentScore;
                maxIndex=i;
            }

            if(currentScore < min)
                min = currentScore;
            average = totalScore / (i+1);
            System.out.println((i+1) +". game, average: "+ average + " max score: " + max + " min score: " + min);
        }
        average = totalScore/loop;
        System.out.println("average score: "+ average +" Max score: " + max + " Min score: " + min + " Max score order:" + maxIndex);


        Statistics.calculateStatistics();

        printResultsToAFile(statistics.tumSonuclar, "tumSonuclarSirali.txt");

         */





        /*
        Stats[] stats = executor.runExperiment(new MyPacMan(), new MASController(controllers), 200, "denemeler");
        for (int i = 0; i < stats.length; i++){
            Executor.saveToFile(stats[i].toString(),"AAAmypacman_" + i +".txt", true);
        }

/*
        Stats[] stats2 = executor.runExperiment(new MyPacMan1(), new MASController(controllers), 200, "denemeler");
        for (int i = 0; i < stats2.length; i++){
            Executor.saveToFile(stats2[i].toString(),"belgeler/deneme0" + i +".txt", true);
        }
*/


        // daha sonra oyunu replay yapabilmek icin kaydediyoruz
        //executor.runGameTimedRecorded(new MyPacMan(), new MASController(controllers), "stats");

        /*
        int loop = 100;
        int maxIndex = -1;
        double totalScore = 0;
        double currentScore = 0;
        double max = Integer.MIN_VALUE;
        double min = Integer.MAX_VALUE;
        for (int i = 0; i < 100; i++){
            Stats a = executor.runGameTimedRecorded(new MyPacMan1(), new MASController(controllers), i+ ". statsFile.txt");
            currentScore = a.getMax();
            totalScore += currentScore;
            if (currentScore > max){
                max = currentScore;
                maxIndex = i;
            }

            if(currentScore < min)
                min = currentScore;
        }
        System.out.println(maxIndex + ". index, max score "+ max + " average: " + totalScore/100);
        */

        //executor.replayGame("replays/56. statsFile.txt", true);
    }



    public static void printResultsToAFile(ArrayList<Integer> tumSonuclar, String fileName){
        Collections.sort(tumSonuclar);
        try {
            PrintWriter output = new PrintWriter(new FileWriter(fileName, true));
            for (int sonuc : tumSonuclar){
                output.println(sonuc);
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
