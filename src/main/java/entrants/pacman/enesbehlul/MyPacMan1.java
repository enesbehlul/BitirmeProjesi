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
    private MOVE catchingOrEscapingMove;

    static int currentPacmanLocation;
    static int temp;
    static int random;
    static int MIN_DISTANCE = 30;
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

    Set<Integer> visitedLocations = new HashSet<Integer>();

    Map<MOVE, Constants.GHOST> dangerousDirections = new HashMap<MOVE, Constants.GHOST>();

    Map<MOVE, Constants.GHOST> eatableGhostsDirections = new HashMap<MOVE, Constants.GHOST>();

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
        int shortestPathDistance, edibleTime, lairTime;
        closestGhostLocation = -1;
        closestGhostDistance = Integer.MAX_VALUE;

        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            ghostLocation = game.getGhostCurrentNodeIndex(ghost);
            if (ghostLocation != -1) {

                shortestPathDistance = game.getShortestPathDistance(currentPacmanLocation, ghostLocation);
                edibleTime = game.getGhostEdibleTime(ghost);
                lairTime = game.getGhostLairTime(ghost);

                // burada en sagdaki kosul hayaletin yenilebilir oldugu sure icinde ona yetisebileceksek
                if (edibleTime > 0 && lairTime == 0 && edibleTime - shortestPathDistance >= 0) {
                    if (shortestPathDistance < closestGhostDistance){
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


    private Map.Entry<MOVE, Constants.GHOST> getRelativeDirectionAndGhostObjectBetweenPacmanAndGhost(Game game, int fromNodeIndex, int toNodeIndex, Constants.GHOST ghost){
        // burada herhangi iki noktadan biri digerine gore hangi yonde onu hesapliyoruz
        // bunu hesaplarken oyunun haritasindaki konum bilgilerini kullaniyoruz
        // haritada saga gidildikce konum degeri 1er 1er artarken, asagi gidildikce 4er 6sar artiyor

        int distanceBetweenNodes = game.getShortestPathDistance(fromNodeIndex, toNodeIndex);

        int range = fromNodeIndex - toNodeIndex;
        int absoluteRange = Math.abs(range);

        // iki konum degeri birbirinden cikarildiginda eger gercek uzakliktan daha buyuk bir deger donuyorsa
        // bu iki node dikey duzlemde kiyaslanmalidir, ornegin ilk nodumuzun degeri 246 ikincisi de 252 olsun
        // bu iki node arasindaki gercek uzaklik 1dir yani komsular, fakat aralarindaki fark 6, dolayisiya
        // harita bilgisinden yola cikarak, 252 konumu 246 konumunun asagisindadir diyebiliriz -> MOVE.DOWN
        if (absoluteRange > distanceBetweenNodes){

            // pacman lokasyonu hayaletinkinden kucukse, yani hayalet asagidaysa
            if (range < 0){
                return new AbstractMap.SimpleEntry(MOVE.DOWN, ghost);
            } else {
                return new AbstractMap.SimpleEntry(MOVE.UP, ghost);
            }

        } else {
            // bu kosulda pacman daha soldadir yani hayalet sagdadir, tehlikeli konum sag
            if (range < 0) {
                return new AbstractMap.SimpleEntry(MOVE.RIGHT, ghost);
            } else {
                return new AbstractMap.SimpleEntry(MOVE.LEFT, ghost);
            }
        }
    }

    /**
     * eğer pacman ile hayalet birbirlerine 30 birim uzaklıktan daha yakınsa
     * ve konumları arasındaki farkın mutlak değeri birbirlerine olan uzaklıktan daha fazlaysa
     * dikey konumda karsilastir, oteki turlu yatayda karsilastir, karsilastima sonucu konumu belirle
     * ve bir dizi icinde hangi yonde ve ne kadar uzaklikta oldugunu kaydet
     *
     */
    private void setDangerousDirections(Game game){
        int distanceBetweenPacmanAndGhost;
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            ghostLocation = game.getGhostCurrentNodeIndex(ghost);
            if (ghostLocation != -1) {
                distanceBetweenPacmanAndGhost = game.getShortestPathDistance(currentPacmanLocation, ghostLocation);
                if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                    // eger hayalet pacman'e tresholdumuzdan daha yakinsa
                    if (distanceBetweenPacmanAndGhost < MIN_DISTANCE){
                        Map.Entry<MOVE, Constants.GHOST> moveAndDistance = getRelativeDirectionAndGhostObjectBetweenPacmanAndGhost(game, currentPacmanLocation, ghostLocation, ghost);
                        dangerousDirections.put(moveAndDistance.getKey(), ghost);
                    }
                }
            }
        }
    }

    private void resetDangereousDirections(){
        dangerousDirections.clear();
    }

    private void setEatableGhostDirections(Game game){

        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            ghostLocation = game.getGhostCurrentNodeIndex(ghost);
            // hayaleti goruyorsak -1 degildir
            if (ghostLocation != -1) {
                // eger hayalet yenilebilir durumda ise
                if (game.getGhostEdibleTime(ghost) > 0 && game.getGhostLairTime(ghost) == 0) {
                    Map.Entry<MOVE, Constants.GHOST> moveAndDistance = getRelativeDirectionAndGhostObjectBetweenPacmanAndGhost(game, currentPacmanLocation, ghostLocation, ghost);
                    eatableGhostsDirections.put(moveAndDistance.getKey(), ghost);
                }
            }
        }
    }

    private boolean isThereAnyDangereousDirection(){
        return !dangerousDirections.isEmpty();
    }

    private boolean isThereAnyEatableGhost(){
        return !eatableGhostsDirections.isEmpty();
    }

    /**
     * oncelikle gidilmesi tehlikeli yonler bulunuyor eger tehlikeli yon varsa
     * etrafimizin sarilip sarilmadigi konrol ediliyor, sarildiysa en uzaktaki hayalete
     * dogru kacmak icin gereken yonu donduruyoruz, sarili degilse, en yakindaki power pille
     * gidilmesi icin donulmesi gereken yonde tehlike yoksa o yonu donduruyoruz, varsa tehlike
     * olmayan rastgele bir yon donduruyoruz
     *
     */
    private MOVE getEscapingMoveFromGhosts(Game game){
        // tehlikeli yonleri sifirliyoruz ki yeni adimda
        // eski sonuclara gore hareket etmeyelim
        resetDangereousDirections();

        setDangerousDirections(game);


        // pacman'in bulundugu konumda yapabilecegi(possibleMoves) hareketler ile
        // hayaletlerin pacman'e gore bulundugu yonleri(dangereousDirections) kiyaslayacagiz
        // ama once tehlikeli yon var mi onu kontrol etmeliyiz
        // yoksa etrafta hayalet yok demektir bu da kacmaya gerek yok demektir
        if (isThereAnyDangereousDirection()){

            // eger gidilebilecek yon sayisi ile tehlikeli yon sayisi esitse, etrafimiz sarilmis demektir
            // bu durumda bize uzak olan hayaletin bulundugu konuma kadar gidersek belki kurtuluruz(bi umit)
            if (dangerousDirections.size() == game.getPossibleMoves(currentPacmanLocation).length){
                int tempGhostDistance = Integer.MIN_VALUE;
                int distanceBetweenPacmanAndGhost;
                MOVE tempMove = null;
                for (Map.Entry<MOVE, Constants.GHOST> moveAndGhost: dangerousDirections.entrySet()) {
                    distanceBetweenPacmanAndGhost = game.getShortestPathDistance(currentPacmanLocation, game.getGhostCurrentNodeIndex(moveAndGhost.getValue()));
                    if (tempGhostDistance < distanceBetweenPacmanAndGhost){
                        tempGhostDistance = distanceBetweenPacmanAndGhost;
                        tempMove = moveAndGhost.getKey();
                    }
                }
                System.out.println("Etrafimiz sarildi...");
                return tempMove;
            }

            // UNUTMA! etrafimiz sariliyken, ornegin iki hayalet arasindaysak arada bir yerde junction varsa oraya yonelt UNUTMA!

            // Buraya hayalet bize dogru geliyorsa kacalim yoksa kacmayalim kodu yazilacak
            for (Map.Entry<MOVE, Constants.GHOST> moveAndGhost: dangerousDirections.entrySet()) {
                if (!isDangerousGhostComingTowerdsPacman(game, moveAndGhost.getValue())){
                    return null;
                }
            }
            
            // eger kovalanirken, en yakindaki power pile gitmek icin donmemiz gereken yon guvenli ise
            // o yone donelim degilse rastgele
            MOVE closestPowerPillMove = getMoveForClosestAvailablePowerPill(game);
            if (!dangerousDirections.containsKey(closestPowerPillMove)){
                System.out.println("hayalet goruldu, en yakindaki power pille yonelindi");
                return closestPowerPillMove;
            }

            Random random = new Random();
            // dongu sayisi 100 cunku rastgele bir guvenli yone kacmak isyorum
            for (int i = 0; i < 100; i++){
                int rand = random.nextInt(game.getPossibleMoves(currentPacmanLocation).length);
                if (!dangerousDirections.containsKey(game.getPossibleMoves(currentPacmanLocation)[rand])){
                    System.out.println("En yakindaki power pile gitmek icin gereken yon tehlikeli");
                    return game.getPossibleMoves(currentPacmanLocation)[rand];
                }
            }
        }
        return null;
    }

    // bu metod artik kullanilmiyor
    private MOVE getEscapingMoveFromGhostsOld(Game game){
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
        setEatableGhostDirections(game);
        //en yakindaki hayaleti yemek icin
        closestGhostLocation = getClosestEdibleGhostLocation(game);

        if (closestGhostLocation != -1){
            return game.getNextMoveTowardsTarget(currentPacmanLocation, closestGhostLocation, Constants.DM.MANHATTAN);
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

    public boolean isDangerousGhostComingTowerdsPacman(Game game, Constants.GHOST dangerousGhost){
        int dangerousGhostLocation = game.getGhostCurrentNodeIndex(dangerousGhost);
        MOVE dangerousGhostLastMove = game.getGhostLastMoveMade(dangerousGhost);
        Map.Entry<MOVE, Constants.GHOST> pacmanDirectionByGhost = getRelativeDirectionAndGhostObjectBetweenPacmanAndGhost(game, dangerousGhostLocation, currentPacmanLocation, dangerousGhost);
        if ((dangerousGhostLastMove == pacmanDirectionByGhost.getKey())){
            return true;
        }
        return false;
    }

    // eger pacman hem yenilebilir hem de kacmasi gereken hayalet goruyorsa, yakin olan hangisiyse ondan kacacak
    private MOVE getCatchingOrEscapingMove(Game game){
        // gorunurde hem yenilebilir hem de kacilmasi gereken hayalet varsa
        if (escapingMove != null && catchingMove != null){
            MOVE pacmanLastMove = game.getPacmanLastMoveMade();

            for (Map.Entry<MOVE, Constants.GHOST> directionAndGhostTypeForDangerousGhost: dangerousDirections.entrySet()) {

                Constants.GHOST dangerousGhost = directionAndGhostTypeForDangerousGhost.getValue();
                int dangerousGhostLocation = game.getGhostCurrentNodeIndex(dangerousGhost);
                MOVE dangerousGhostLastMove = game.getGhostLastMoveMade(dangerousGhost);
                for (Map.Entry<MOVE, Constants.GHOST> directionAndGhostTypeForEatableGhost: eatableGhostsDirections.entrySet()) {

                    Constants.GHOST eatableGhost = directionAndGhostTypeForEatableGhost.getValue();
                    int eatableGhostLocation = game.getGhostCurrentNodeIndex(eatableGhost);

                    int pacmanAndEatableGhostDistance = game.getShortestPathDistance(currentPacmanLocation, eatableGhostLocation);
                    int pacmanAndDangerousGhostDistance = game.getShortestPathDistance(currentPacmanLocation, dangerousGhostLocation);

                    // hayalet ve pacman ayni yonde ilerliyorsa ama hayalet yenilebilir hayalete daha yakınsa
                    int dangerousGhostAndEatableGhostDistance = game.getShortestPathDistance(dangerousGhostLocation, eatableGhostLocation);

                    if (pacmanLastMove == dangerousGhostLastMove && dangerousGhostAndEatableGhostDistance < pacmanAndEatableGhostDistance ){
                        return escapingMove;
                    }

                    // eger hayalet pacmanin uzerine dogru gelmiyorsa
                    if (!isDangerousGhostComingTowerdsPacman(game, dangerousGhost)){
                        return catchingMove;
                    }

                    // uzerimize dogru gelse de eger yenilebilir hayalet daha yakinsa
                    if (pacmanAndEatableGhostDistance < pacmanAndDangerousGhostDistance){
                        return catchingMove;
                    } else {
                        return escapingMove;
                    }
                }
            }
        }

        return null;
    }

    private int getClosestActivePillIndice(Game game){
        //gorunurde pil yoksa en yakin pil hesaplanamaz
        if (game.getActivePillsIndices().length == 0){
            return -1;
        }
        return game.getClosestNodeIndexFromNodeIndex(currentPacmanLocation, game.getActivePillsIndices(), Constants.DM.MANHATTAN);
        /*
        closestActivePillDistance = Integer.MAX_VALUE;
        for (int i = 0; i<game.getActivePillsIndices().length; i++){

            if (closestActivePillDistance > game.getShortestPathDistance(current, game.getActivePillsIndices()[i])){
                closestActivePillDistance = game.getShortestPathDistance(current, game.getActivePillsIndices()[i]);
                closestActivePillLocation = game.getActivePillsIndices()[i];
            }
        }
        return closestActivePillLocation;
         */
    }

    private int getClosestUnvisitedLocation(Game game){
        closestPillDistance = Integer.MAX_VALUE;
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
        System.out.println(closestPillLocation);
        MOVE move = game.getApproximateNextMoveTowardsTarget(currentPacmanLocation, closestPillLocation, game.getPacmanLastMoveMade(), Constants.DM.MANHATTAN);
        return move;
    }

    // bu metod gelistirilmeli su an calismiyor zaten kopyaladim
    public MOVE getMoveForClosestUnvisitedLocationNew(Game game){
        closestPillLocation = getClosestUnvisitedLocation(game);

        int distanceBetweenNodes = game.getShortestPathDistance(currentPacmanLocation, closestPillLocation);

        int range = currentPacmanLocation - closestPillLocation;
        int absoluteRange = Math.abs(range);

        // iki konum degeri birbirinden cikarildiginda eger gercek uzakliktan daha buyuk bir deger donuyorsa
        // bu iki node dikey duzlemde kiyaslanmalidir, ornegin ilk nodumuzun degeri 246 ikincisi de 252 olsun
        // bu iki node arasindaki gercek uzaklik 1dir yani komsular, fakat aralarindaki fark 6, dolayisiya
        // harita bilgisinden yola cikarak, 252 konumu 246 konumunun asagisindadir diyebiliriz -> MOVE.DOWN
        if (absoluteRange > distanceBetweenNodes){

            // pacman lokasyonu hayaletinkinden kucukse, yani hayalet asagidaysa
            if (range < 0){
                return MOVE.DOWN;
            } else {
                return MOVE.UP;
            }

        } else {
            // bu kosulda pacman daha soldadir yani hayalet sagdadir, tehlikeli konum sag
            if (range < 0) {
                return MOVE.RIGHT;
            } else {
                return MOVE.LEFT;
            }
        }
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

        escapingMove = getEscapingMoveFromGhosts(game);

        catchingMove = getPacmanCatchingMoveForGhosts(game);
        catchingOrEscapingMove = getCatchingOrEscapingMove(game);

        if (catchingOrEscapingMove != null){
            System.out.println("iki yontem arasinda kaldik");
            return catchingOrEscapingMove;
        }

        if (escapingMove != null){
            return escapingMove;
        }

        if (catchingMove != null){
            System.out.println("hayalet kovalaniyor");
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
            System.out.println("gorunurde pil yok");
            if (game.isJunction(currentPacmanLocation)){
                System.out.println("junction");
            }
            System.out.println("pacman: " + currentPacmanLocation);
            return getMoveForClosestUnvisitedLocation(game);
        }


        System.out.println("pil yeniyor");
        return game.getNextMoveTowardsTarget(currentPacmanLocation, activeTargetPill, Constants.DM.MANHATTAN);


    }
}