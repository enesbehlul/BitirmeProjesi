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

enum State{
    SAFE,
    DANGER,
    HUNTING
}

public class MyPacMan extends PacmanController {
    //bu move nesneleri metodlarda kullanilmak uzre olusturuldu
    private MOVE myMove = Constants.MOVE.NEUTRAL;
    private MOVE escapingMove;
    private MOVE catchingMove;

    State state;

    static int current;
    static int temp;
    static int random;
    static int lastGhostSeenLoc;
    static int MIN_DISTANCE = 35;
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

    long startTime, stopTime, elapsedTime;

    // bu dizi yukari[0], asagi[1], sag[2] ve sol[3] yonlerde
    // hayalet var mi bilgisi tutacak
    boolean[] dangerousDirections = new boolean[4];

    Set<Integer> visitedLocations = new HashSet<Integer>();

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
                    setDangerousDirections(game, current, ghostLocation);
                    if (game.getShortestPathDistance(current, ghostLocation) < closestGhostDistance){
                        closestGhostDistance = game.getShortestPathDistance(current, ghostLocation);
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
                    if (game.getShortestPathDistance(current, ghostLocation) < closestGhostDistance){
                        closestGhostDistance = game.getShortestPathDistance(current, ghostLocation);
                        closestGhostLocation = game.getGhostCurrentNodeIndex(ghost);
                    }
                }
            }
        }
        return closestGhostLocation;
    }

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
     * dikey konumda karsilastir, oteki turlu yatayda karsilastir
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
                    dangerousDirections[1] = true;
                } else {
                    dangerousDirections[0] = true;
                }

            } else {
                // bu kosulda pacman daha soldadir yani hayalet sagdadir, tehlikeli konum sag
                if (fark < 0){
                    dangerousDirections[2] = true;
                } else{
                    dangerousDirections[3] = true;
                }
            }
        }
    }

    private void resetDangereousDirections(){
        for (int i = 0; i < dangerousDirections.length; i++){
            dangerousDirections[i] = false;
        }
    }

    private boolean isThereAnyDangereousDirection(){
        for (boolean ghostSeen : dangerousDirections) {
            if (ghostSeen){
                return true;
            }
        }
        return false;
    }

    private MOVE getNewEscapingMoveFromGhosts(Game game){
        // tehlikeli yonleri sifirliyoruz ki yeni adimda
        // eski sonuclara gore hareket etmeyelim
        resetDangereousDirections();

        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    // burada hayaletlerin pacman'e gore konumlari
                    // dangereousDirections dizisine aktariliyor
                    setDangerousDirections(game, current, ghostLocation);
                }
            }
        }
        // pacman'in bulundugu konumda yapabilecegi(possibleMoves) hareketler ile
        // hayaletlerin pacman'e gore bulundugu yonleri(dangereousDirections) kiyaslayacagiz
        // ama once tehlikeli yon dizisinde true deger var mi onu kontrol etmeliyiz
        // true deger yoksa etrafta hayalet yok demektir bu da kacmaya gerek yok demektir
        Random random = new Random();

        if (isThereAnyDangereousDirection()){
            for (int i = 0; i < 100; i++){
                int rastgele = random.nextInt(game.getPossibleMoves(current).length);
                // eger yukari cikabiliyorsak ve yukarisi tehlikeli degilse
                if (game.getPossibleMoves(current)[rastgele].equals(MOVE.UP) && !dangerousDirections[0]){
                    return MOVE.UP;
                }
                if (game.getPossibleMoves(current)[rastgele].equals(MOVE.DOWN) && !dangerousDirections[1]){
                    return MOVE.DOWN;
                }
                if (game.getPossibleMoves(current)[rastgele].equals(MOVE.RIGHT) && !dangerousDirections[2]){
                    return MOVE.RIGHT;
                }
                if (game.getPossibleMoves(current)[rastgele].equals(MOVE.LEFT) && !dangerousDirections[3]){
                    return MOVE.LEFT;
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
            if (game.getShortestPathDistance(current, ghostLocation) < MIN_DISTANCE) {
                System.out.println("haylt: " + ghostLocation + " Hayaletten kaciliyor. ");
                temp = current;
                return game.getNextMoveAwayFromTarget(current, ghostLocation, Constants.DM.PATH);
            }
        }
        return null;
    }

    private MOVE getPacmanCatchingMoveForGhosts(Game game){

        //en yakindaki hayaleti yemek icin
        closestGhostLocation = getClosestEdibleGhostLocation(game);

        if (closestGhostLocation != -1){
            return game.getNextMoveTowardsTarget(current, closestGhostLocation, Constants.DM.PATH);
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
        closestActivePillLocation = game.getClosestNodeIndexFromNodeIndex(current, game.getActivePillsIndices(), Constants.DM.MANHATTAN);
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
                if (closestPillDistance > game.getShortestPathDistance(current, pillLocation)){
                    closestPillDistance = game.getShortestPathDistance(current, pillLocation);
                    closestPillLocation = pillLocation;
                }
            }
        }
        System.out.println("Gorunurde olmayan en yakindaki pille gidiliyor.");
        return closestPillLocation;
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
        startTime = System.nanoTime();
        
        current = game.getPacmanCurrentNodeIndex();
        visitedLocations.add(current);

        System.out.println("pacman: "+current);

        escapingMove = getNewEscapingMoveFromGhosts(game);
        if (escapingMove != null){
            temp = current;
            return escapingMove;
        }

        catchingMove = getPacmanCatchingMoveForGhosts(game);

        if (catchingMove != null){
            temp = current;
            return catchingMove;
        }

        //eger pacman sabitse(bir onceki konumu ile ayni yerdeyse
        if (current == temp){
            System.out.println("Ayni yerde takilma sorunu");
            return getRandomMove(game.getPossibleMoves(current));
        } else {
            try {

                //gorus alanindaki pillerden en yakin olanini bul

                activeTargetPill = getClosestActivePillIndice(game);
                //System.out.println("target pill " + activeTargetPill);

                /*
                 * BURASI GELISTIRILECEK
                // eger yakinlarda power pill varsa, hayalet gelmesini bekle
                if (game.getActivePowerPillsIndices().length != 0 && !checkGhosts(game)){
                    if (powerPillWaitingCounter++ % 2 == 0){
                        return game.getPossibleMoves(current)[0];
                    }
                    else{
                        return game.getPossibleMoves(current)[1];
                    }
                } else if(checkGhosts(game)){
                    return game.getNextMoveTowardsTarget(current, game.getActivePowerPillsIndices()[0], Constants.DM.PATH);
                }

                 */

                /*if (closestGhostDistance > 150  && game.getActivePowerPillsIndices().length != 0){

                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!power pillden kaciliyor " + closestGhostDistance);
                    return game.getNextMoveAwayFromTarget(current, game.getActivePowerPillsIndices()[0], Constants.DM.PATH);
                }*/
                if (activeTargetPill == -1){
                    activeTargetPill = getClosestUnvisitedLocation(game);
                    System.out.println("target: " + activeTargetPill);
                }


                temp = current;
                return game.getNextMoveTowardsTarget(current, activeTargetPill, game.getPacmanLastMoveMade(), Constants.DM.MANHATTAN);
            } catch (ArrayIndexOutOfBoundsException e){
                System.out.println("gorunurde pil yok");
                int pillLocation = getClosestUnvisitedLocation(game);
                temp = current;
                return game.getNextMoveTowardsTarget(current, pillLocation, game.getPacmanLastMoveMade(), Constants.DM.MANHATTAN);
            }
        }
    }
}