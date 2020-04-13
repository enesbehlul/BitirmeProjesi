package entrants.pacman.enesbehlul;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;


/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */

public class MyPacMan extends PacmanController {
    private MOVE myMove = Constants.MOVE.NEUTRAL;
    private MOVE escapingMove;
    private MOVE catchingMove;
    static int current;
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
    int levelScore;
    int[] levelScores = new int[3];
    int[] levelTimes = new int[3];

    Set<Integer> visitedLocations = new HashSet<Integer>();

    private MOVE getRandomMove(MOVE[] possibleMoves){
        random = new Random().nextInt(possibleMoves.length);
        return possibleMoves[random];
    }

    private MOVE getEscapingMoveFromGhosts(Game game){
        /*
        * 1. Buraya duzenleme olarak, birden fazla hayalet tarafindan kovalaniyorsak,
        * en yakin olanindan kacma komutu eklenebilir.(EKLENDI)*/

        closestGhostLocation = -1;
        closestGhostDistance = Integer.MAX_VALUE;
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If can't see these will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    if (game.getShortestPathDistance(current, ghostLocation) < closestGhostDistance){
                        closestGhostDistance = game.getShortestPathDistance(current, ghostLocation);
                        closestGhostLocation = game.getGhostCurrentNodeIndex(ghost);
                    }
                }
            }
        }
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
        }

        return null;
    }

    private int getClosestActivePillIndice(Game game){
        //gorunurde pil yoksa en yakin pil hesaplanamaz
        if (game.getActivePillsIndices().length == 0){
            return -1;
        }
        closestActivePillDistance = Integer.MAX_VALUE;
        for (int i = 0; i<game.getActivePillsIndices().length; i++){

            if (closestActivePillDistance > game.getShortestPathDistance(current, game.getActivePillsIndices()[i])){
                closestActivePillDistance = game.getShortestPathDistance(current, game.getActivePillsIndices()[i]);
                closestActivePillLocation = game.getActivePillsIndices()[i];
            }
        }
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
        System.out.println("BASKA BIR LOKASYONA GIDILIYOR-------------------------------------");
        return closestPillLocation;
    }

    private void checkState(Game game){
        if (currentLevel != game.getCurrentLevel() && currentLevel == 0){
            levelScore = game.getScore();
            levelScores[0] = levelScore;
            levelTimes[0] = game.getTotalTime();
            currentLevel++;
            System.out.println("yeni level'a gecildi");
            visitedLocations.clear();
            System.out.println("Ziyaret edilen konumlar dizisi sifirlandi.");
            saveGameInformation(game);
        }
        else if (currentLevel != game.getCurrentLevel() && game.getCurrentLevel() != 3){
            levelScore = game.getScore() - levelScores[0] - levelScores[1] - levelScores[2];
            levelScores[currentLevel] = levelScore;
            levelTimes[currentLevel] = game.getCurrentLevelTime();
            currentLevel++;
        }
        if (game.gameOver()){
            saveGameInformation(game);
            System.out.println("GAME OVER");
        }

    }

    private void saveGameInformation(Game game){
        try (PrintWriter writer = new PrintWriter(new File("gameInformations.csv"))) {

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 3; i++){
                sb.append(levelTimes[i] + "," + levelScores[i] + ",");
            }

            if (game.getCurrentLevel() == 4){
                sb.append(game.getCurrentLevelTime() + "," + game.getScore());
            }
            sb.append("\n");
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }



    public MOVE getMove(Game game, long timeDue) {
        //Place your game logic here to play the game as Ms Pac-Man
        if (game.getPacmanNumberOfLivesRemaining() < 2){
            game.updateGame(false,false,true,false,false);
        }

        checkState(game);

        current = game.getPacmanCurrentNodeIndex();
        visitedLocations.add(current);

        System.out.println("pacman: "+current);


        escapingMove = getEscapingMoveFromGhosts(game);
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
                System.out.println("target pill " + activeTargetPill);
                /*//eger yakinlarda power pill varsa konumlarini yazdir
                for (int i = 0; i < game.getActivePowerPillsIndices().length; i++){
                    System.out.println("AKTIF POWERPILL -----------------------");
                    System.out.println(game.getActivePowerPillsIndices()[i]);
                }*/

                /*if (closestGhostDistance > 150  && game.getActivePowerPillsIndices().length != 0){

                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!power pillden kaciliyor " + closestGhostDistance);
                    return game.getNextMoveAwayFromTarget(current, game.getActivePowerPillsIndices()[0], Constants.DM.PATH);
                }*/
                if (activeTargetPill == -1){
                    activeTargetPill = getClosestUnvisitedLocation(game);
                }

                System.out.println("target: " + activeTargetPill);
                temp = current;
                return game.getNextMoveTowardsTarget(current, activeTargetPill, game.getPacmanLastMoveMade(), Constants.DM.EUCLID);
            } catch (ArrayIndexOutOfBoundsException e){
                System.out.println("gorunurde pil yok");
                int pillLocation = getClosestUnvisitedLocation(game);
                temp = current;
                return game.getNextMoveTowardsTarget(current, pillLocation, game.getPacmanLastMoveMade(), Constants.DM.EUCLID);
            }
        }
    }
}