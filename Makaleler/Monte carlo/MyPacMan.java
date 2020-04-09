package entrants.pacman.enesbehlul;

import entrants.pacman.enesbehlul.evaluators.*;
import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.info.GameInfo;
import pacman.game.internal.Maze;
import pacman.game.internal.Node;

import pacman.Executor;
import pacman.controllers.PacmanController;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.internal.Ghost;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import java.util.Random;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */
/*
public class MyPacMan extends PacmanController {
    private MOVE myMove = Constants.MOVE.NEUTRAL;
    static int current, temp,random, MIN_DISTANCE = 20, counter = 0;
    static int targetPill, activeTargetPill, ghostLocation;

    boolean isInclude(int[] list, int element){
        for (int i = 0; i<list.length; i++){

            if (element == list[i]){
                System.out.println("VARVARVARVARVARVARVARVARVARVARVAR");
                return true;
            }
        }
        return false;
    }

    private MOVE getRandomMove(MOVE[] possibleMoves){
        for (Constants.MOVE move : possibleMoves){
            System.out.println(move);
        }
        System.out.println("********************************************");
        random = new Random().nextInt(possibleMoves.length);
        return possibleMoves[random];
    }

    public MOVE getMove(Game game, long timeDue) {
        //Place your game logic here to play the game as Ms Pac-Man
        current = game.getPacmanCurrentNodeIndex();

        System.out.println("pacman: "+current);
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If can't see these will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    if (game.getShortestPathDistance(current, ghostLocation) < MIN_DISTANCE) {
                        //System.out.println("Evading Ghost");
                        System.out.println("45 45 45 45 45 45");
                        return game.getNextMoveAwayFromTarget(current, ghostLocation, Constants.DM.PATH);
                    }
                }
            }
            if (game.getGhostEdibleTime(ghost) > 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                if (ghostLocation != -1) {
                    //System.out.println("Hunting Ghost");
                    System.out.println("54 54 54 54 54 54 54");
                    return game.getNextMoveTowardsTarget(current, ghostLocation, Constants.DM.PATH);
                }
            }
        }
        //eger pacman sabitse(bir onceki konumu ile ayni yerdeyse
        if (current == temp){
            return getRandomMove(game.getPossibleMoves(current));
        } else {
            try {
                activeTargetPill = game.getActivePillsIndices()[0];
                System.out.println("target: " + activeTargetPill);
                System.out.println("63 63 63 63 63");
                return game.getNextMoveTowardsTarget(current, activeTargetPill, Constants.DM.PATH);
            } catch (ArrayIndexOutOfBoundsException e){
                targetPill = game.getPillIndices()[0];
                System.out.println("ex diger listedeki target: " + targetPill);
                if (targetPill == 0 && game.isJunction(current)){
                    return getRandomMove(game.getPossibleMoves(current));
                }
            }
        }

        temp = current;
        System.out.println("random selected");
        if (game.isJunction(current))
            return getRandomMove(game.getPossibleMoves(current));
        return null;
    }
}

 */

public class MyPacMan extends PacmanController {

    /*public static void main(String[] args) {
        Executor po = new Executor(true, true, true);
        po.setDaemon(true);
        po.runGame(new MyPacMan(), new POCommGhosts(), true, 40);
    }*/

    public MOVE getMove(Game game, long timeDue) {

        // We will only simulate the next moves in a junction,
        // otherwise we will walk straight along the hallway
        int myNodeIndex = game.getPacmanCurrentNodeIndex();

        // First priority is to keep Ms. Pac-man alive.
        // If there is any Ghost near by then must EVADE from Ghost, don't bother anything else
        MOVE move = checkNearByGhost(game);
        if(move != MOVE.NEUTRAL) {
            // System.out.println("Run Away from Ghosts!");
            return move;
        }

        // Next check if any edible Ghost is present in the path. Go for it !
        int minDistance = Integer.MAX_VALUE;
        Constants.GHOST minGhost = null;
        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If it is > 0 then it is visible.
            if (game.getGhostEdibleTime(ghost) > 0) {
                int distance = game.getShortestPathDistance(myNodeIndex, game.getGhostCurrentNodeIndex(ghost));

                if (distance < minDistance) {
                    minDistance = distance;
                    minGhost = ghost;
                }
            }
        }

        if (minGhost != null) {
            // System.out.println("Hunting Ghost");
            return game.getNextMoveTowardsTarget(
                    myNodeIndex,
                    game.getGhostCurrentNodeIndex(minGhost),
                    Constants.DM.PATH);
        }

        if (game.isJunction(myNodeIndex)) {
            // return best direction determined through MCTS
            return mcts(game);
        } else {
            return nonJunctionSim(game);
        }
    }

    /**
     * Checks if there are any nearby Ghosts
     * @param game
     * @return NEUTRAL if no Ghost nearby else EVADE
     */
    public static MOVE checkNearByGhost(Game game) {

        int[] junctions;
        int nearestJunctionPoint = -1;
        int nearestJunctionDistance = -1;
        int pacManNodeIndex = game.getPacmanCurrentNodeIndex();
        int DEFAULT_MINIMUM_DISTANCE = 10;
        int[] junctionPath = null;
        int ghostLocation = -1;
        int closestJunctionPoint = -1;
        boolean foundPath = false;
        ///////////////////////////////// Test Run /////////////////////////////////
        // Buggy !

        /*for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If can't see, this will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                // If Ghost is within a certain Range: 10
                if (game.getShortestPathDistance(pacManNodeIndex, ghostLocation) <= DEFAULT_MINIMUM_DISTANCE) {
                    // System.out.println("Evading Ghost");
                    ArrayList<Integer>junctionArray = new ArrayList<>(); // store the pacman move junctions
                    junctions = game.getJunctionIndices();
                    boolean foundGhost = false;
                    // get pacman all possible moves
                    for(int i = 0; i < junctions.length; i++) {
                        // find the nearby junctions from pacman moves
                        foundGhost = false;
                        int[] pacManIndices = game.getShortestPath(pacManNodeIndex, junctions[i]);
                        for(Constants.GHOST ghost1 : Constants.GHOST.values()) {
                            // find if any ghost is there in that path
                            int ghost1Location = game.getGhostCurrentNodeIndex(ghost1);
                            for(int indices : pacManIndices) {
                                if(ghost1Location == indices) {
                                    foundGhost = true;
                                    System.out.println("Ghost Found at: " + indices);
                                    break;
                                }
                            }
                            if (foundGhost == true) {
                                break;
                            }
                        }
                        if(foundGhost == false) {
                            System.out.println("Safe junction: " + junctions[i]);
                            junctionArray.add(new Integer(junctions[i]));
                        }
                    }
                    junctionPath = new int[junctionArray.size()];
                    for(int i = 0; i < junctionArray.size(); i++) {
                        junctionPath[i] = junctionArray.get(i);
                        foundPath = true;
                    }
                    System.out.println("Total Junction length found: " + junctionPath.length);
                }
            }
        }
        if(foundPath) {
            System.out.println("Found Path: " + foundPath);
            GameView.addPoints(game, Color.CYAN, game.getClosestNodeIndexFromNodeIndex(pacManNodeIndex, junctionPath, Constants.DM.PATH));
            return game.getNextMoveTowardsTarget(
                    pacManNodeIndex,
                    game.getClosestNodeIndexFromNodeIndex(pacManNodeIndex, junctionPath, Constants.DM.PATH),
                    Constants.DM.PATH);
        } else {
            return game.getNextMoveAwayFromTarget(pacManNodeIndex, ghostLocation, Constants.DM.PATH);
        }*/
        /////////////////////////////////

        for (Constants.GHOST ghost : Constants.GHOST.values()) {
            // If can't see, this will be -1 so all fine there
            if (game.getGhostEdibleTime(ghost) == 0 && game.getGhostLairTime(ghost) == 0) {
                ghostLocation = game.getGhostCurrentNodeIndex(ghost);
                // If Ghost is within a certain Range: 10
                if (game.getShortestPathDistance(pacManNodeIndex, ghostLocation) <= DEFAULT_MINIMUM_DISTANCE) {
                    // System.out.println("Evading Ghost");
                    // ghostArray.add(new Integer(ghostLocation));
                    return game.getNextMoveAwayFromTarget(pacManNodeIndex, ghostLocation, Constants.DM.PATH);
                }
            }
        }

        /*int[] ghostIndices = new int[ghostArray.size()];
        for(int i = 0; i < ghostArray.size(); i++) {
            ghostIndices[i] = ghostArray.get(i);
        }
        if(ghostIndices.length > 0) {
            return game.getNextMoveAwayFromTarget(pacManNodeIndex, ghostLocation, Constants.DM.PATH);
        }*/

        return MOVE.NEUTRAL;
    }

    /**
     * Non Junction point simulation
     * @param game
     */
    public static MOVE nonJunctionSim(Game game){

        // get the current position of PacMan (returns -1 in case you can't see PacMan)
        int myNodeIndex = game.getPacmanCurrentNodeIndex();

        // From ExtendedGame.Java
        // Rather than simply following the path, lets try to chose the best path first

        //set pills
        int[] powerPills = game.getPowerPillIndices();
        int[] pills = game.getPillIndices();
        ArrayList<Integer> allPills = new ArrayList<>();

        for (int i = 0; i < powerPills.length; i++) {
            Boolean checkPowerPill = game.isPowerPillStillAvailable(i);
            if (checkPowerPill != null) { // Avoids NPE
                // System.out.println("checkPowerPill: " + checkPowerPill);

                if (checkPowerPill == true){
                    allPills.add(powerPills[i]);
                }
            }
        }

        for (int i = 0; i < pills.length; i++) {
            Boolean checkPill = game.isPillStillAvailable(i);
            if (checkPill != null) { // Avoids NPE
                // System.out.println("checkPill: " + checkPill);

                if (checkPill == true){
                    allPills.add(pills[i]);
                }
            }
        }

        // If there are some pills or power pills and we have seen Pac-Man
        if (!allPills.isEmpty() && myNodeIndex != -1) {
            int[] targetPath = new int[allPills.size()];
            for(int i = 0; i < targetPath.length; i++) {
                targetPath[i] = allPills.get(i);
            }

            return game.getNextMoveTowardsTarget(
                    myNodeIndex,
                    game.getClosestNodeIndexFromNodeIndex(myNodeIndex, targetPath, Constants.DM.PATH),
                    Constants.DM.PATH);
        }

        MOVE[] moves = game.getPossibleMoves(myNodeIndex, game.getPacmanLastMoveMade());
        if (moves.length > 0) {
            Random random = new Random();
            return moves[random.nextInt(moves.length)];
        }
        // Must be possible to turn around
        return game.getPacmanLastMoveMade().opposite();
    }

    /**
     * Simulation through MCS
     * @param game
     */
    public static MOVE mcts(Game game) {

        // create MCTS Tree object for simulation
        MonteCarloTree tree = new MonteCarloTree(game);
        MOVE move = MOVE.NEUTRAL;

        for (int i = 0; i < 10; i++) { // Run simulations
            tree.simulate();
        }

        // Run some evaluators every time of the simulations
        entrants.pacman.enesbehlul.evaluators.
        ITreeEvaluator[] additionalEvaluators = new ITreeEvaluator[] {
                new DistanceToOpportunityEvaluator(),
                new PowerPillActiveEvaluator(),
                new RuleBasedEvaluator()};

        for (ITreeEvaluator evaluator: additionalEvaluators) {
            evaluator.evaluateTree(tree);
        }

        // Pick the move with the best score
        MonteCarloGameNode node = tree.bestNode();
        if (node != null) {
            move = node.getMove();
        } else {
            move = MOVE.NEUTRAL; // just do the earlier move. hopefully this will never execute.
            // System.out.println("move: " + move);
        }
        tree.setRootNode(node);

        return move;
    }
}