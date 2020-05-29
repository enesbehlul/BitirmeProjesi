
import entrants.ghosts.enesbehlul.*;
//import examples.StarterPacMan.*;
//import examples.StarterGhost.*;
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

    // STANDART SAPMA VE STDERR HESAPLANACAK UNUTMA!!!

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

        //createListFromFile();

        //executor.runGame(new MyPacMan(), new MASController(controllers), 3);
        // Pacmani klavyeden yonetebilmek icin
        //executor.runGame(new KlavyeKontrol(new KeyBoardInput()), new MASController(controllers), 40);

        // delay suresini kisaltarak oyunu hizlandiriyoruz


        ArrayList<Integer> tumSonuclar = new ArrayList<>();
        int loop = 100;
        int totalScore = 0, currentScore = 0;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int avarage;
        int maxIndex = -1;
        for (int i = 0; i < loop; i++){
            currentScore = executor.runGame(new MyPacMan1(), new MASController(controllers), 0);
            tumSonuclar.add(currentScore);
            totalScore += currentScore;
            if (currentScore > max){
                max = currentScore;
                maxIndex=i;
            }

            if(currentScore < min)
                min = currentScore;
            avarage = totalScore / (i+1);
            System.out.println((i+1) +". game, avarage: "+ avarage + " max score: " + max);
        }
        avarage = totalScore/loop;
        System.out.println("avarage score: "+ avarage +" Max score: " + max + " Min score: " + min + " " + maxIndex);


        calculateStatistics(tumSonuclar, avarage);
        System.out.println(calculateStandardDeviation(tumSonuclar, avarage));
        printResultsToAFile(tumSonuclar, "tumSonuclarSirali.txt");





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
        System.out.println(maxIndex + ". index, max score "+ max + " avarage: " + totalScore/100);
        */

        //executor.replayGame("replays/56. statsFile.txt", true);
    }

    static void createListFromFile(){
        ArrayList<Integer> sonuclar = new ArrayList<>();
        try {
            BufferedReader input = new BufferedReader(new FileReader("tumSonuclarSirali.txt"));

            String satir;
            while ((satir = input.readLine()) != null) {
                int score = Integer.parseInt(satir);
                sonuclar.add(score);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int ort = calculateMean(sonuclar);
        double std = calculateStandardDeviation(sonuclar, ort);
        System.out.println("Ortalama: " + ort + " std: " + std);

    }

    static int calculateMean(ArrayList<Integer> tumSonuclar){
        int toplam = 0;

        for (int score : tumSonuclar){
            toplam += score;
        }

        return toplam/tumSonuclar.size();
    }

    public static void calculateStatistics(ArrayList<Integer> tumSonuclar , int mean){
        int median = calculateMedian(tumSonuclar);
        System.out.println("Median = " + median);
        double std = calculateStandardDeviation(tumSonuclar, mean);
        System.out.println(std + ": Standart dev");
    }

    public static int calculateMedian(ArrayList<Integer> tumSonuclar){
        int len = tumSonuclar.size();

        Collections.sort(tumSonuclar);
        if (len %2 == 0){
            return  (tumSonuclar.get((len-1)/2) + tumSonuclar.get(len/2))/2;
        } else{
            return tumSonuclar.get(((len-1)/2));
        }
    }

    public static double calculateStandardDeviation(ArrayList<Integer> tumSonuclar, int ortalama){

        double farklarinKarelerininToplami = 0;

        for (int sonuc : tumSonuclar){
            farklarinKarelerininToplami += Math.pow((sonuc-ortalama),2);
        }
        double varyans = farklarinKarelerininToplami/tumSonuclar.size();
        return Math.sqrt(varyans);
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
