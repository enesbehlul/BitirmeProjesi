package entrants.pacman.enesbehlul;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.util.*;
import java.util.concurrent.TimeUnit;


/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */

public class MyPacMan extends PacmanController {
    //bu move nesneleri metodlarda kullanilmak uzre olusturuldu

    private MOVE escapingMove;
    private MOVE catchingMove;
    private MOVE catchingOrEscapingMove;

    int currentPacmanLocation;
    int random;
    int MIN_DISTANCE = 30;
    int closestGhostDistance;
    int closestGhostLocation;
    int activeTargetPill;
    int ghostLocation;
    int currentLevel = 0;

    boolean isEveryPowerPillEaten = false;

    long startTime, stopTime, elapsedTime;

    Set<Integer> visitedLocations = new HashSet<Integer>();

    Map<MOVE, Constants.GHOST> dangerousDirectionAndGhostMap = new HashMap<MOVE, Constants.GHOST>();

    Map<MOVE, Constants.GHOST> edibleGhostDirectionsAndGhostMap = new HashMap<MOVE, Constants.GHOST>();

    //junction indexi ve o junctiona en yakin olan hayaletin junctiona olan uzaklıgi
    Map<Integer, Integer> junctionIndexAndClosestGhostDistanceToTheJunction = new HashMap<Integer, Integer>();

    private MOVE getRandomMove(MOVE[] possibleMoves){
        random = new Random().nextInt(possibleMoves.length);
        return possibleMoves[random];
    }

    /**
     * burada herhangi iki noktadan biri digerine gore hangi yonde onu hesapliyoruz
     * bunu hesaplarken oyunun haritasindaki konum bilgilerini kullaniyoruz, haritada
     * saga gidildikce konum degeri 1er 1er artarken, asagi gidildikce 4er 6sar artiyor
     * dolayisiyla iki konum degerinini birbirinden cikarttigimizda(mutlak deger olarak),
     * elde ettigimiz deger gercek uzakliktan kucukse, iki konumu dikeyde kiyasliyoruz,
     * esitse yatayda kiyasliyoruz
     **/
    private Map.Entry<MOVE, Constants.GHOST> getRelativeDirectionAndGhostObjectBetweenPacmanAndGhost(Game game, int fromNodeIndex, int toNodeIndex, Constants.GHOST ghost){
        int distanceBetweenNodes = game.getShortestPathDistance(fromNodeIndex, toNodeIndex);

        int range = fromNodeIndex - toNodeIndex;
        int absoluteRange = Math.abs(range);

        // iki konum degeri birbirinden cikarildiginda eger gercek uzakliktan daha buyuk bir deger donuyorsa
        // bu iki node dikey duzlemde kiyaslanmalidir, ornegin ilk nodumuzun degeri 246 ikincisi de 252 olsun
        // bu iki node arasindaki gercek uzaklik 1dir yani komsular, fakat aralarindaki fark 6, dolayisiya
        // harita bilgisinden yola cikarak, 252 konumu 246 konumunun asagisindadir diyebiliriz
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

    private MOVE getMoveForClosestEdibleGhost(Game game){
        setEdibleGhostDirectionAndGhostMap(game);

        int distanceBetweenPacmanAndEdibleGhost, edibleTime, lairTime;
        closestGhostLocation = -1;
        closestGhostDistance = Integer.MAX_VALUE;
        Constants.GHOST ghost = null;

        MOVE closestMoveForEdibleGhost = null;
        if (isThereAnyEdibleGhost()){

            for (Map.Entry<MOVE, Constants.GHOST> moveAndGhost: edibleGhostDirectionsAndGhostMap.entrySet()){
                ghost = moveAndGhost.getValue();
                // daha onceden olusturdugumuz yenilebilir hayaletler sozlugu icindeki hayaletin konumu
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);

                distanceBetweenPacmanAndEdibleGhost = game.getShortestPathDistance(currentPacmanLocation, ghostLocation);
                edibleTime = game.getGhostEdibleTime(ghost);
                lairTime = game.getGhostLairTime(ghost);

                // burada en sagdaki kosul hayaletin yenilebilir oldugu sure icinde ona yetisebileceksek
                if (edibleTime > 0 && lairTime == 0 && edibleTime - distanceBetweenPacmanAndEdibleGhost >= 0) {
                    if (distanceBetweenPacmanAndEdibleGhost < closestGhostDistance){
                        closestMoveForEdibleGhost = moveAndGhost.getKey();
                        closestGhostDistance = game.getShortestPathDistance(currentPacmanLocation, ghostLocation);
                        closestGhostLocation = ghostLocation;
                    }
                }

            }
        }
        return closestMoveForEdibleGhost;
    }

    /**
     * eğer pacman ile hayalet birbirlerine 30 birim uzaklıktan daha yakınsa
     * ve konumları arasındaki farkın mutlak değeri birbirlerine olan uzaklıktan daha fazlaysa
     * dikey konumda karsilastir, oteki turlu yatayda karsilastir, karsilastima sonucu konumu belirle
     * ve bir dizi icinde hangi yonde ve ne kadar uzaklikta oldugunu kaydet
     *
     */
    private void setDangerousDirectionAndGhostMap(Game game){
        // tehlikeli yonleri sifirliyoruz ki yeni adimda eski sonuclara gore hareket etmeyelim
        resetDangerousDirections();

        int distanceBetweenPacmanAndGhost;
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            ghostLocation = game.getGhostCurrentNodeIndex(ghost);
            if (ghostLocation != -1) {
                distanceBetweenPacmanAndGhost = game.getShortestPathDistance(currentPacmanLocation, ghostLocation);
                if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0 && distanceBetweenPacmanAndGhost < MIN_DISTANCE) {
                    Map.Entry<MOVE, Constants.GHOST> moveAndDistance = getRelativeDirectionAndGhostObjectBetweenPacmanAndGhost(game, currentPacmanLocation, ghostLocation, ghost);
                    dangerousDirectionAndGhostMap.put(moveAndDistance.getKey(), ghost);
                }
            }
        }
    }

    private void resetDangerousDirections(){ dangerousDirectionAndGhostMap.clear(); }

    private void setEdibleGhostDirectionAndGhostMap(Game game){
        // ilk olarak sifirlamamiz lazim ki eski sonuclara gore hareket etmeyelim
        resetEdibleGhostDirections();
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // hayaleti goruyorsak -1 degildir
            ghostLocation = game.getGhostCurrentNodeIndex(ghost);

            // eger hayalet yenilebilir durumda ise
            if (ghostLocation != -1 && game.getGhostEdibleTime(ghost) > 0 && game.getGhostLairTime(ghost) == 0) {
                Map.Entry<MOVE, Constants.GHOST> moveAndDistance = getRelativeDirectionAndGhostObjectBetweenPacmanAndGhost(game, currentPacmanLocation, ghostLocation, ghost);
                edibleGhostDirectionsAndGhostMap.put(moveAndDistance.getKey(), ghost);
            }
        }
    }

    private void resetEdibleGhostDirections(){ edibleGhostDirectionsAndGhostMap.clear(); }

    private boolean isThereAnyDangerousDirection(){ return !dangerousDirectionAndGhostMap.isEmpty(); }

    private boolean isThereAnyEdibleGhost(){ return !edibleGhostDirectionsAndGhostMap.isEmpty(); }

    private boolean isThereAnyGhostComingTowardsPacman(Game game){

        if (isThereAnyDangerousDirection()){

            for (Map.Entry<MOVE, Constants.GHOST> moveAndGhost: dangerousDirectionAndGhostMap.entrySet()) {
                if (isDangerousGhostComingTowerdsPacman(game, moveAndGhost.getValue())){
                    return true;
                }
            }
        }
        return false;
    }

    private void setAvailableJunctionsForEscaping(Game game){

        int ghostJunctionDistance, pacmanJunticonDistance;

        for (Constants.GHOST ghost: dangerousDirectionAndGhostMap.values()){
            int ghostLocation = game.getGhostCurrentNodeIndex(ghost);
            int[] pathIndices = game.getShortestPath(currentPacmanLocation, ghostLocation);
            for (int index: pathIndices){
                // hayalet ve pacman arasında junction var mi
                if (game.isJunction(index)){
                    pacmanJunticonDistance = game.getShortestPathDistance(currentPacmanLocation, index);
                    ghostJunctionDistance = game.getShortestPathDistance(ghostLocation, index);
                    // eger pacman donemece daha yakinsa ekle
                    if (pacmanJunticonDistance < ghostJunctionDistance){
                        junctionIndexAndClosestGhostDistanceToTheJunction.put(index, ghostJunctionDistance);
                    }
                }
            }
        }
    }

    /**
     * oncelikle gidilmesi tehlikeli yonler bulunuyor eger tehlikeli yon varsa
     * etrafimizin sarilip sarilmadigi konrol ediliyor, sarildiysa arasinda kaldigimiz hayaletler
     * arasinda gidebilecegimiz junctionlara gitmeye calisiyoruz, sarili degilse, en yakindaki power pille
     * gidilmesi icin donulmesi gereken yonde tehlike yoksa o yonu donduruyoruz, varsa tehlike
     * olmayan rastgele bir yon donduruyoruz
     *
     */
    private MOVE getEscapingMoveFromGhosts(Game game){

        setDangerousDirectionAndGhostMap(game);

        // uzerimize dogru gelen hayalet var mi onu kontrol ediyoruz
        // yoksa kacmaya gerek yok demektir
        if (isThereAnyGhostComingTowardsPacman(game)){

            MOVE surroundedMove = getMoveForSurroundedPacman(game);
            // etrafimiz sarili mi?
            if (surroundedMove != null){
                return surroundedMove;
            }

            // eger kovalanirken, en yakindaki power pile gitmek icin donmemiz gereken yon guvenli ise
            // o yone donelim degilse rastgele yon secimi bir sonraki adimda
            MOVE closestPowerPillMove = getMoveForClosestAvailablePowerPill(game);
            if (closestPowerPillMove != null && !dangerousDirectionAndGhostMap.containsKey(closestPowerPillMove)){
                System.out.println("hayalet goruldu, en yakindaki power pille yonelindi");
                return closestPowerPillMove;
            }

            MOVE closestPillMove = null;
            if (isEveryPowerPillEaten){
                closestPillMove = getMoveForClosestUnvisitedLocation(game);
                if (!dangerousDirectionAndGhostMap.containsKey(closestPillMove)){
                    System.out.println("Butun guc pilleri tukendi, yenmemis pile yonelindi");
                    return closestPillMove;
                }
            }

            Random random = new Random();
            // dongu sayisi 100 cunku rastgele bir guvenli yone kacmak isyorum 100 denemeye kadar
            // sanirim mutlaka tehlikesiz bir yon bulunur (yuzde 99.9) xd
            for (int i = 0; i < 100; i++){
                int rand = random.nextInt(game.getPossibleMoves(currentPacmanLocation).length);
                if (!dangerousDirectionAndGhostMap.containsKey(game.getPossibleMoves(currentPacmanLocation)[rand])){
                    System.out.println("En yakindaki power pile gidemiyoruz");
                    return game.getPossibleMoves(currentPacmanLocation)[rand];
                }
            }
        }
        return null;
    }

    private boolean isDangerousGhostComingTowerdsPacman(Game game, Constants.GHOST dangerousGhost){
        int dangerousGhostLocation = game.getGhostCurrentNodeIndex(dangerousGhost);
        Map.Entry<MOVE, Constants.GHOST> pacmanDirectionByGhost = getRelativeDirectionAndGhostObjectBetweenPacmanAndGhost(game, dangerousGhostLocation, currentPacmanLocation, dangerousGhost);
        return game.getGhostLastMoveMade(dangerousGhost) == pacmanDirectionByGhost.getKey();
    }

    // eger pacman hem yenilebilir hem de kacmasi gereken hayalet goruyorsa, yakin olan hangisiyse ondan kacacak
    private MOVE getCatchingOrEscapingMove(Game game){
        // gorunurde hem yenilebilir hem de kacilmasi gereken hayalet varsa
        if (escapingMove != null && catchingMove != null){
            MOVE pacmanLastMove = game.getPacmanLastMoveMade();

            for (Map.Entry<MOVE, Constants.GHOST> directionAndGhostTypeForDangerousGhost: dangerousDirectionAndGhostMap.entrySet()) {

                Constants.GHOST dangerousGhost = directionAndGhostTypeForDangerousGhost.getValue();
                int dangerousGhostLocation = game.getGhostCurrentNodeIndex(dangerousGhost);
                MOVE dangerousGhostLastMove = game.getGhostLastMoveMade(dangerousGhost);
                for (Map.Entry<MOVE, Constants.GHOST> directionAndGhostTypeForEdibleGhost: edibleGhostDirectionsAndGhostMap.entrySet()) {

                    Constants.GHOST edibleGhost = directionAndGhostTypeForEdibleGhost.getValue();
                    int edibleGhostLocation = game.getGhostCurrentNodeIndex(edibleGhost);

                    int distanceBetweenPacmanAndEdibleGhost = game.getShortestPathDistance(currentPacmanLocation, edibleGhostLocation);
                    int distanceBetweenPacmanAndDangerousGhost = game.getShortestPathDistance(currentPacmanLocation, dangerousGhostLocation);

                    // bu kontrolu yapiyorum cunku, pacman yenilebilir hayaleti kovalarken, uzerine tehlikeli hayalet gelebilir
                    // ve pacman yenilebilir hayalete daha yakin oldugu icin kacis stratejisini uygulamayabilir
                    if (pacmanLastMove == dangerousGhostLastMove){
                        // hayalet ve pacman ayni yonde ilerliyorsa ve pacman yenilebilir hayalete daha yakinsa
                        if (distanceBetweenPacmanAndEdibleGhost < distanceBetweenPacmanAndDangerousGhost ){
                            return catchingMove;
                        }
                        else{
                            return escapingMove;
                        }
                    }

                    // eger hayalet pacmanin uzerine dogru gelmiyorsa
                    if (!isDangerousGhostComingTowerdsPacman(game, dangerousGhost)){
                        return catchingMove;
                    }

                    // uzerimize dogru gelse de eger yenilebilir hayalet daha yakinsa
                    // BURASI GELISTIRILMELI UNUTMA, yukarida bu kontrolu zaten yapiyoruz burda bunu yapmak
                    // gereksiz olmus, burada yenilebilir hayaletin hizi yariya indiginden pacman ile hayaletler
                    // arasindaki mesafeleri dogrudan buyukluk acisindan kiyaslamak yanlis olur,
                    // tehlikeli hayalete olan uzakligimiz, yenilebilire olan uzakligin iki katiysa vs gibi olsun
                    if (distanceBetweenPacmanAndEdibleGhost < distanceBetweenPacmanAndDangerousGhost){
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
    }

    private int getClosestUnvisitedLocation(Game game){
        int closestPillDistance = Integer.MAX_VALUE;
        int closestPillLocation = -1;
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


    private MOVE getMoveForClosestUnvisitedLocation(Game game){
        int closestPillLocation = getClosestUnvisitedLocation(game);
        //System.out.println(closestPillLocation);
        if (closestPillLocation != -1){
            MOVE move = game.getNextMoveTowardsTarget(currentPacmanLocation, closestPillLocation, game.getPacmanLastMoveMade(), Constants.DM.MANHATTAN);
            return move;
        }
        return null;
    }

    private int getClosestAvailablePowerPillIndex(Game game){
        int closestPowerPillDistance = Integer.MAX_VALUE;
        int closestPowerPillIndex = -1;
        int tempDistance;
        for (int powerPillIndex: game.getPowerPillIndices()) {
            if (!visitedLocations.contains(powerPillIndex) && powerPillIndex != -1){
                tempDistance = game.getShortestPathDistance(currentPacmanLocation, powerPillIndex);
                if (closestPowerPillDistance > tempDistance){
                    closestPowerPillDistance = tempDistance;
                    closestPowerPillIndex = powerPillIndex;
                }
            }
        }
        return closestPowerPillIndex;
    }

    private MOVE getMoveForClosestAvailablePowerPill(Game game){
        int index = getClosestAvailablePowerPillIndex(game);
        if (index == -1){
            isEveryPowerPillEaten = true;
            return null;
        }
        return game.getNextMoveTowardsTarget(currentPacmanLocation, index, game.getPacmanLastMoveMade(), Constants.DM.MANHATTAN);
    }

    // pacmanin anlik olarak gidebilecegi tum yonlerde tehlikeli hayalet varsa
    private MOVE getMoveForSurroundedPacman(Game game){

        // eger gidilebilecek yon sayisi ile tehlikeli yon sayisi esitse, etrafimiz sarilmis demektir
        // bu durumda bize uzak olan hayaletin bulundugu konuma kadar gidersek belki kurtuluruz(bi umit)
        // yine bu durumda etrafimizdaki tum hayaletler uzerimize dogru gelmiyor olabilir,

        if (dangerousDirectionAndGhostMap.size() == game.getPossibleMoves(currentPacmanLocation).length){
            int tempGhostDistance = Integer.MIN_VALUE;
            int distanceBetweenPacmanAndGhost;
            MOVE tempMove = null;


            // uzerimize gelmeyen en uzaktaki hayalete dogru yonelmek icin
            for (Map.Entry<MOVE, Constants.GHOST> moveAndGhost: dangerousDirectionAndGhostMap.entrySet()) {
                distanceBetweenPacmanAndGhost = game.getShortestPathDistance(currentPacmanLocation, game.getGhostCurrentNodeIndex(moveAndGhost.getValue()));
                if (tempGhostDistance < distanceBetweenPacmanAndGhost && !isDangerousGhostComingTowerdsPacman(game, moveAndGhost.getValue())){
                    tempGhostDistance = distanceBetweenPacmanAndGhost;
                    tempMove = moveAndGhost.getKey();
                }
            }
            if (tempMove != null){
                return tempMove;
            }

            // kod buraya ulastiysa tum hayaletler uzerimize geliyor demektir
            junctionIndexAndClosestGhostDistanceToTheJunction.clear();
             setAvailableJunctionsForEscaping(game);
            // eger iki hayalet arasinda pacmanin kacabilecegi junctionlar varsa
            if (!junctionIndexAndClosestGhostDistanceToTheJunction.isEmpty()){
                for (Map.Entry<Integer, Integer> junctionAndDistance: junctionIndexAndClosestGhostDistanceToTheJunction.entrySet()){
                    // gidebilecegimiz junctiondan kacabilecegimiz yonler tehlikeli degilse
                    for (MOVE junctionsMove: game.getPossibleMoves(junctionAndDistance.getKey())){
                        if (!dangerousDirectionAndGhostMap.containsKey(junctionsMove)){
                            return game.getNextMoveTowardsTarget(currentPacmanLocation, junctionAndDistance.getKey(), Constants.DM.MANHATTAN);
                        }
                    }
                }
            }

            // kod buraya ulastiysa kacacak yer kalmadi demektir ELVEDA...
            for (Map.Entry<MOVE, Constants.GHOST> moveAndGhost: dangerousDirectionAndGhostMap.entrySet()) {
                distanceBetweenPacmanAndGhost = game.getShortestPathDistance(currentPacmanLocation, game.getGhostCurrentNodeIndex(moveAndGhost.getValue()));
                if (tempGhostDistance < distanceBetweenPacmanAndGhost){
                    tempGhostDistance = distanceBetweenPacmanAndGhost;
                    tempMove = moveAndGhost.getKey();
                }
            }
            System.out.println("Etrafimiz sarildi...");
            return tempMove;
        }
        return null;
    }

    private void checkState(Game game){
        if (currentLevel != game.getCurrentLevel()){
            currentLevel++;
            System.out.println("***YENI LEVEL'A GECILDI***");

            visitedLocations.clear();
            isEveryPowerPillEaten = false;
            System.out.println("Ziyaret edilen konumlar dizisi sifirlandi.");
        }
    }

    // 40ms lik sure limitinin asilip asilmadigini kontrol etmek icin
    private void printMoveMethotExecutionTime(long startTime, long stopTime){

        System.out.print("Toplam sure: ");

        elapsedTime = stopTime - startTime;

        //nanosaniyeden milisaniyeye cevirip yazdiriyoruz.
        System.out.print(TimeUnit.NANOSECONDS.toMillis(elapsedTime));
        System.out.print("millisecond and ");
        System.out.print(elapsedTime);
        System.out.println("nanosecond.");
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        //Place your game logic here to play the game as Ms Pac-Man
        checkState(game);
        // metodun calisma sureseni hesaplamak istersek diye
        startTime = System.nanoTime();

        currentPacmanLocation = game.getPacmanCurrentNodeIndex();

        //eger pacman sabitse(bir onceki konumu ile ayni yerdeyse

        visitedLocations.add(currentPacmanLocation);

        escapingMove = getEscapingMoveFromGhosts(game);

        catchingMove = getMoveForClosestEdibleGhost(game);
        catchingOrEscapingMove = getCatchingOrEscapingMove(game);

        if (catchingOrEscapingMove != null){
            System.out.println("iki yontem arasinda kaldik");
            return catchingOrEscapingMove;
        }

        if (escapingMove != null){
            return escapingMove;
        }

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

    /* KULLANIM DISI METHODLAR

    // bu metod artik kullanilmiyor
    private MOVE getEscapingMoveFromGhostsOld(Game game){


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

    // bu metodu artik kullanmiyoruz, cunku edibleGhostAndDirectionMapi icinde
    // yenilebilir hayaletleri zaten tutuyoruz
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

    private boolean areDirectionsOpposite(MOVE d1, MOVE d2){
        if(d1 == d2){
            return false;
        }
        if (d1 == MOVE.LEFT && d2 == MOVE.RIGHT || d1 == MOVE.RIGHT && d2 == MOVE.LEFT){
            return true;
        }
        return (d1 == MOVE.UP && d2 == MOVE.DOWN || d1 == MOVE.DOWN && d2 == MOVE.UP);

    }

    // bu metod gelistirilmeli su an calismiyor zaten kopyaladim
    private MOVE getMoveForClosestUnvisitedLocationNew(Game game){
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

     */
}