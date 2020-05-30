import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Statistics {

    static ArrayList<Integer> tumSonuclar;
    static int q1, q3, iqr, median, minScore, maxScore;
    static double std, stdErr, mean;

    Statistics(){
        tumSonuclar = new ArrayList<>();
    }

    public static void calculateStatistics(){
        calculateMean();
        calculateMedian();
        calculateStandardDeviation();
        calculateStandardError();
        System.out.println("Population size: " + tumSonuclar.size());
        System.out.println("Mean: " + mean);
        System.out.println("Median: " + median);
        System.out.println("Std: " + std);
        System.out.println("StdErr: " + stdErr);
        // the array is ordered
        minScore = tumSonuclar.get(0);
        maxScore = tumSonuclar.get(tumSonuclar.size()-1);
        System.out.println("Min score: " + minScore);
        System.out.println("Max score: " + maxScore);
    }

    public static void calculateStatisticsFromFile(String fileAdress){
        createListFromFile(fileAdress);
        calculateStatistics();
    }

    static void createListFromFile(String fileAdress){
        tumSonuclar = new ArrayList<>();
        try {
            BufferedReader input = new BufferedReader(new FileReader(fileAdress));

            String satir;
            while ((satir = input.readLine()) != null) {
                int score = Integer.parseInt(satir);
                tumSonuclar.add(score);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void calculateMean(){
        double toplam = 0;

        for (int score : tumSonuclar){
            toplam += score;
        }

        mean  = toplam/tumSonuclar.size();
    }

    public static void calculateMedian(){
        int len = tumSonuclar.size();
        int q1Index, q3Index;

        Collections.sort(tumSonuclar);
        if (len %2 == 0){
            median = (tumSonuclar.get((len-1)/2) + tumSonuclar.get(len/2))/2;
        } else{
            median = tumSonuclar.get(((len-1)/2));
        }

    }

    public static void calculateStandardDeviation(){

        double farklarinKarelerininToplami = 0;

        for (int sonuc : tumSonuclar){
            farklarinKarelerininToplami += Math.pow((sonuc-mean),2);
        }
        double varyans = farklarinKarelerininToplami/tumSonuclar.size();
        std =  Math.sqrt(varyans);
    }

    public static void calculateStandardError(){
        double sqrtOfSize = Math.sqrt(tumSonuclar.size());
        stdErr = std/sqrtOfSize;
    }

    public static void main(String[] args) {
        calculateStatisticsFromFile("belgeler/1000tumSonuclarSirali32330.txt");
    }
}
