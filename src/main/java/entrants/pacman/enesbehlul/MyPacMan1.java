package entrants.pacman.enesbehlul;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;


/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */

public class MyPacMan1 extends PacmanController {
    //bu move nesneleri metodlarda kullanilmak uzre olusturuldu

    private MOVE escapingMove;
    private MOVE catchingMove;

    static int currentPacmanLocation;
    static int temp;
    static int random;
    static int lastGhostSeenLoc;
    static int MIN_DISTANCE = 50;
    static int closestGhostDistance;
    static int closestGhostLocation;
    static int targetPill;
    static int activeTargetPill;
    static int ghostLocation;
    static int closestActivePillDistance;
    static int closestActivePillLocation;
    static int closestPillLocation;
    static int closestPillDistance;
    int currentLevel = 0;

    int ghostNotSeenCounter = 0;

    long startTime, stopTime, elapsedTime;

    Set<Integer> visitedLocations = new HashSet<Integer>();

    Map<MOVE, Integer> dangerousDirections2 = new HashMap<MOVE, Integer>();

    private MOVE getRandomMove(MOVE[] possibleMoves){
        random = new Random().nextInt(possibleMoves.length);
        return possibleMoves[random];
    }

    //etrafta hayalet var mi kontrol et
    private boolean checkGhosts(Game game){
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If can't see these will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getClosestGhostLocation(Game game){
        closestGhostLocation = -1;
        closestGhostDistance = Integer.MAX_VALUE;

        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If can't see these will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    setDangerousDirections(game, currentPacmanLocation, ghostLocation);
                    if (game.getShortestPathDistance(currentPacmanLocation, ghostLocation) < closestGhostDistance){
                        closestGhostDistance = game.getShortestPathDistance(currentPacmanLocation, ghostLocation);
                        closestGhostLocation = game.getGhostCurrentNodeIndex(ghost);
                    }
                }
            }
        }
        return closestGhostLocation;
    }

    private int getClosestEdibleGhostLocation(Game game){
        closestGhostLocation = -1;
        closestGhostDistance = Integer.MAX_VALUE;

        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If can't see these will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) > 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    if (game.getShortestPathDistance(currentPacmanLocation, ghostLocation) < closestGhostDistance){
                        closestGhostDistance = game.getShortestPathDistance(currentPacmanLocation, ghostLocation);
                        closestGhostLocation = game.getGhostCurrentNodeIndex(ghost);
                    }
                }
            }
        }
        return closestGhostLocation;
    }

    // bu metodu artik kullanmiyoruz
    private int getDigitCountOfAnInteger(int number){
        if (number < 100) {
            if (number < 10) {
                return 1;
            } else {
                return 2;
            }
        } else {
            if (number < 1000) {
                return 3;
            } else {
                // haritada 2000'den buyuk bir konum olmadigi icin
                // 1000'den buyuk her konum 4 basamaklidir.
                if (number < 2000) {
                    return 4;
                }
            }
        }
        return -1;
    }

    /**
     * eğer pacman ile hayalet birbirlerine 30 birim uzaklıktan daha yakınsa
     * ve konumları arasındaki farkın mutlak değeri birbirlerine olan uzaklıktan daha fazlaysa
     * dikey konumda karsilastir, oteki turlu yatayda karsilastir, karsilastima sonucu konumu belirle
     * ve bir dizi icinde hangi yonde ve ne kadar uzaklikta oldugunu kaydet
     *
     * @param pacmanLocation the pacman location
     * @param ghostLocation the ghost location
     *
     */
    private void setDangerousDirections(Game game, int pacmanLocation, int ghostLocation){
        int pacmanIleGhostArasindakiMesafe = game.getShortestPathDistance(pacmanLocation, ghostLocation);

        // aradaki farkin pozitif olmasi lazim
        int fark = pacmanLocation - ghostLocation;
        int farkMutlak = Math.abs(fark);
        // eger hayalet pacman'e tresholdumuzdan daha yakinsa
        if (pacmanIleGhostArasindakiMesafe < MIN_DISTANCE){

            // bu kosulda dikeyde karsilastirmaliyiz, cunku dikeyde haritadaki degerler 4er 6 sar yukseliyor
            // dolayisiyla harita konumlari arasindaki fark gercek mesafeden buyuk olacaktir
            if (farkMutlak > pacmanIleGhostArasindakiMesafe){

                // pacman lokasyonu hayaletinkinden kucukse, yani hayalet asagidaysa
                if (fark < 0){
                    dangerousDirections2.put(MOVE.DOWN, pacmanIleGhostArasindakiMesafe);
                } else {
                    dangerousDirections2.put(MOVE.UP, pacmanIleGhostArasindakiMesafe);
                }

            } else {
                // bu kosulda pacman daha soldadir yani hayalet sagdadir, tehlikeli konum sag
                if (fark < 0){
                    dangerousDirections2.put(MOVE.RIGHT, pacmanIleGhostArasindakiMesafe);
                } else{
                    dangerousDirections2.put(MOVE.LEFT, pacmanIleGhostArasindakiMesafe);
                }
            }
        }
    }

    private void resetDangereousDirections(){
        dangerousDirections2.clear();
    }

    private boolean isThereAnyDangereousDirection(){
        return !dangerousDirections2.isEmpty();
    }

    private MOVE getNewEscapingMoveFromGhosts(Game game){
        // tehlikeli yonleri sifirliyoruz ki yeni adimda
        // eski sonuclara gore hareket etmeyelim
        resetDangereousDirections();

        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    // hayalet gorduk o yuzden sayaci sifirliyoruz
                    ghostNotSeenCounter = 0;

                    // burada hayaletlerin pacman'e gore konumlari dangereousDirections dizisine aktariliyor
                    setDangerousDirections(game, currentPacmanLocation, ghostLocation);
                }
            }
        }
        // pacman'in bulundugu konumda yapabilecegi(possibleMoves) hareketler ile
        // hayaletlerin pacman'e gore bulundugu yonleri(dangereousDirections) kiyaslayacagiz
        // ama once tehlikeli yon dizisinde true deger var mi onu kontrol etmeliyiz
        // true deger yoksa etrafta hayalet yok demektir bu da kacmaya gerek yok demektir
        Random random = new Random();

        if (isThereAnyDangereousDirection()){

            // eger gidilebilecek yon sayisi ile tehlikeli yon sayisi esitse, etrafimiz sarilmis demektir
            // bu durumda bize uzak olan hayaletin bulundugu konuma kadar gidersek belki kurtuluruz(bi umit)
            if (dangerousDirections2.size() == game.getPossibleMoves(currentPacmanLocation).length){
                int tempGhostDistance = Integer.MIN_VALUE;
                MOVE tempMove = null;
                for (Map.Entry<MOVE, Integer> moveAndDistance: dangerousDirections2.entrySet()) {
                    if (tempGhostDistance < moveAndDistance.getValue()){
                        tempGhostDistance = moveAndDistance.getValue();
                        tempMove = moveAndDistance.getKey();
                    }
                }
                System.out.println("Etrafimiz sarildi...");
                return tempMove;
            }

            // UNUTMA! etrafimiz sariliyken, ornegin iki hayalet arasindaysak arada bir yerde junction varsa oraya yonelt UNUTMA!

            // eger kovalanirken, en yakindaki power pile gitmek icin donmemiz gereken yon guvenli ise
            // o yone donelim degilse rastgele
            MOVE closestPowerPillMove = getMoveForClosestAvailablePowerPill(game);
            if (!dangerousDirections2.containsKey(closestPowerPillMove)){
                System.out.println("hayalet goruldu, en yakindaki power pille yonelindi");
                return closestPowerPillMove;
            }
            System.out.println("En yakindaki power pile gitmek icin gereken yon tehlikeli");

            // dongu sayisi 100 cunku rastgele bir guvenli yone kacmak isyorum
            for (int i = 0; i < 100; i++){
                int rand = random.nextInt(game.getPossibleMoves(currentPacmanLocation).length);
                if (!dangerousDirections2.containsKey(game.getPossibleMoves(currentPacmanLocation)[rand])){
                    return game.getPossibleMoves(currentPacmanLocation)[rand];
                }
            }
        }
        return null;
    }

    // bu metod artik kullanilmiyor
    private MOVE getEscapingMoveFromGhosts(Game game){
        /*
         * 1. Buraya duzenleme olarak, birden fazla hayalet tarafindan kovalaniyorsak,
         * en yakin olanindan kacma komutu eklenebilir.(EKLENDI)*/

        closestGhostLocation = getClosestGhostLocation(game);

        if (closestGhostLocation != -1){
            if (game.getShortestPathDistance(currentPacmanLocation, ghostLocation) < MIN_DISTANCE) {
                System.out.println("haylt: " + ghostLocation + " Hayaletten kaciliyor. ");
                temp = currentPacmanLocation;
                return game.getNextMoveAwayFromTarget(currentPacmanLocation, ghostLocation, Constants.DM.PATH);
            }
        }
        return null;
    }

    private MOVE getPacmanCatchingMoveForGhosts(Game game){

        //en yakindaki hayaleti yemek icin
        closestGhostLocation = getClosestEdibleGhostLocation(game);

        if (closestGhostLocation != -1){
            return game.getNextMoveTowardsTarget(currentPacmanLocation, closestGhostLocation, Constants.DM.PATH);
        }
        return null;

        /*
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If can't see these will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) > 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    System.out.println("haylt: " + ghostLocation + " Hayalet kovalaniyor. ");
                    temp = current;
                    return game.getNextMoveTowardsTarget(current, ghostLocation, Constants.DM.PATH);
                }
            }
        } */

    }

    private int getClosestActivePillIndice(Game game){
        //gorunurde pil yoksa en yakin pil hesaplanamaz
        if (game.getActivePillsIndices().length == 0){
            return -1;
        }
        closestActivePillLocation = game.getClosestNodeIndexFromNodeIndex(currentPacmanLocation, game.getActivePillsIndices(), Constants.DM.MANHATTAN);
        /*
        closestActivePillDistance = Integer.MAX_VALUE;
        for (int i = 0; i<game.getActivePillsIndices().length; i++){

            if (closestActivePillDistance > game.getShortestPathDistance(current, game.getActivePillsIndices()[i])){
                closestActivePillDistance = game.getShortestPathDistance(current, game.getActivePillsIndices()[i]);
                closestActivePillLocation = game.getActivePillsIndices()[i];
            }
        }*/
        return closestActivePillLocation;
    }

    private int getClosestUnvisitedLocation(Game game){
        closestPillDistance = Integer.MAX_VALUE;
        //pil ve hayalet gorunmuyor oyleyse daha once gitmedigin konumlara git
        /*
         * Buraya duzenleme olarak, getPillIndices dizisinde en son bakilan indisten baslatabiliris
         * bu sayede her seferinde onceki indisler kontrol edilmemis olur
         * */
        for (int pillLocation : game.getPillIndices()) {
            if (!visitedLocations.contains(pillLocation)){
                if (closestPillDistance > game.getShortestPathDistance(currentPacmanLocation, pillLocation)){
                    closestPillDistance = game.getShortestPathDistance(currentPacmanLocation, pillLocation);
                    closestPillLocation = pillLocation;
                }
            }
        }
        return closestPillLocation;
    }

    public MOVE getMoveForClosestUnvisitedLocation(Game game){
        closestPillLocation = getClosestUnvisitedLocation(game);
        return game.getNextMoveTowardsTarget(currentPacmanLocation, closestPillLocation, game.getPacmanLastMoveMade(), Constants.DM.MANHATTAN);
    }

    public int getClosestAvailablePowerPillIndex(Game game){
        int closestPowerPillDistance = Integer.MAX_VALUE;
        int closestPowerPillIndex = -1;
        int tempDistance;
        for (int powerPillIndex: game.getPowerPillIndices()) {
            if (!visitedLocations.contains(powerPillIndex)){
                tempDistance = game.getShortestPathDistance(currentPacmanLocation, powerPillIndex);
                if (closestPowerPillDistance > tempDistance){
                    closestPowerPillDistance = tempDistance;
                    closestPowerPillIndex = powerPillIndex;
                }
            }
        }
        return closestPowerPillIndex;
    }

    public MOVE getMoveForClosestAvailablePowerPill(Game game){
        int index = getClosestAvailablePowerPillIndex(game);
        if (index == -1)
            return null;

        return game.getNextMoveTowardsTarget(currentPacmanLocation, index, game.getPacmanLastMoveMade(), Constants.DM.MANHATTAN);
    }

    private void checkState(Game game){
        if (currentLevel != game.getCurrentLevel()){
            currentLevel++;
            System.out.println("***YENI LEVEL'A GECILDI***");

            visitedLocations.clear();
            System.out.println("Ziyaret edilen konumlar dizisi sifirlandi.");
        }
    }

    // bu metodu artik kullanmiyoruz cunku main sinifinda executor metotlari ile bu islemleri gerceklestirebiliyoruz
    private void saveGameInformation(Game game){
        try (PrintWriter writer = new PrintWriter(new File("gameInformations.csv"))) {

            StringBuilder sb = new StringBuilder();
            sb.append(game.getCurrentLevel() + "," + game.getTotalTime() + "," + game.getScore());

            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    // 40ms lik sure limitinin asilip asilmadigini kontrol etmek icin
    public void printMoveMethotExecutionTime(long startTime, long stopTime){

        System.out.print("Toplam sure: ");

        elapsedTime = stopTime - startTime;

        //nanosaniyeden milisaniyeye cevirip yazdiriyoruz.
        System.out.print(TimeUnit.NANOSECONDS.toMillis(elapsedTime));
        System.out.print("millisecond and ");
        System.out.print(elapsedTime);
        System.out.println("nanosecond.");
    }

    public MOVE getMove(Game game, long timeDue) {
        //Place your game logic here to play the game as Ms Pac-Man
        checkState(game);
        // metodun calisma sureseni hesaplamak istersek diye
        startTime = System.nanoTime();

        currentPacmanLocation = game.getPacmanCurrentNodeIndex();

        //eger pacman sabitse(bir onceki konumu ile ayni yerdeyse

        visitedLocations.add(currentPacmanLocation);

        escapingMove = getNewEscapingMoveFromGhosts(game);
        if (escapingMove != null){
            ghostNotSeenCounter = 0;
            return escapingMove;
        }
        // tehlikeli hayalet yok arttiracagiz
        ghostNotSeenCounter++;

        catchingMove = getPacmanCatchingMoveForGhosts(game);

        if (catchingMove != null){
            return catchingMove;
        }

        // power pili her zaman yemesin, tehlike durumunda yesin diye
        /*
        if (game.getActivePowerPillsIndices().length != 0) {
            System.out.println("not seen counter " + ghostNotSeenCounter);
            System.out.println(visitedLocations.size() + " " + game.getPillIndices().length);
            if (ghostNotSeenCounter > 20 && visitedLocations.size() < 900) {
                return game.getNextMoveAwayFromTarget(currentPacmanLocation, game.getActivePowerPillsIndices()[0], Constants.DM.MANHATTAN);
            }
        }*/

        //gorus alanindaki pillerden en yakin olanini bul
        activeTargetPill = getClosestActivePillIndice(game);

        if (activeTargetPill == -1){
            return getMoveForClosestUnvisitedLocation(game);
        }



        return game.getNextMoveTowardsTarget(currentPacmanLocation, activeTargetPill, Constants.DM.MANHATTAN);
    }
}