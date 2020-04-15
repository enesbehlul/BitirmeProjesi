package entrants.pacman.enesbehlul;

import pacman.controllers.MASController;
import pacman.controllers.examples.StarterPacMan;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.info.GameInfo;
import pacman.game.internal.Ghost;

import java.util.*;

/**
 * Created by anis016 on 28.05.17.
 */

public class MonteCarloTree {

    private Game game;
    private MASController ghosts;
    private Stack<Game> gameStates;
    private MonteCarloGameNode rootNode;
    public static Random random = new Random();
    private Set<Integer> activePowerPills;

    public MonteCarloTree(Game game){
        Game coGame;
        GameInfo info = game.getPopulatedGameInfo();
        info.fixGhosts((ghost) -> new Ghost(
                ghost,
                game.getCurrentMaze().lairNodeIndex,
                -1,
                -1,
                MOVE.NEUTRAL
        ));
        coGame = game.getGameFromInfo(info);

        this.game = coGame;
        this.gameStates = new Stack<>();
        this.rootNode = new MonteCarloGameNode(); // this is the root node

        // Make some ghosts for simulation purposes
        this.ghosts = new POCommGhosts(50);

        this.activePowerPills = new HashSet<Integer>();
        updateActivePowerPills(game.getActivePowerPillsIndices());
    }

    /**
     * Runs a Monte Carlo simulation from the current point in the game.
     */
    public void simulate() {

        List<MonteCarloGameNode> visitedNodes = new ArrayList<>();

        //save the number of lives so we can tell if we've lost a life during the simulation
        int lives = game.getPacmanNumberOfLivesRemaining();

        //save the game at its current point so we can put it back after the simulation
        pushGameState();
        try {

            //the first node is the root node
            MonteCarloGameNode node = rootNode;
            visitedNodes.add(node);

            //walk through the tree according to nodes with the highest UCB value,
            //until a leaf node is reached
            while (!node.isLeafNode()) {
                node = new Ucb1SelectionPolicy().selectChild(node);

                if (node == null)
                    return;

                //save the nodes we visit so we can update their scores later
                visitedNodes.add(node);

                //move the game state to this node
                //play the move that the node represents
                game.advanceGame(node.getMove(), ghosts.getMove(game, 0));
                //update active power pill indices if necessary
                int[] indices = game.getActivePowerPillsIndices();
                if (indices.length < activePowerPills.size()) {
                    updateActivePowerPills(indices);
                }

                // Repeat simulation till we find the next junction
                while (!game.isJunction(game.getPacmanCurrentNodeIndex())) {
                    game.advanceGame(MyPacMan.nonJunctionSim(game), ghosts.getMove(game, 40));
                }
                // once again leave the junction before extending the simulation
                MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
                game.advanceGame(possibleMoves[random.nextInt(possibleMoves.length)],
                        ghosts.getMove(game.copy(), 40));
            }

            //expand the node based on either of the following criteria:
            //1. Pick one of its children if it's been sampled enough (sampling is selected by how many visit did that children got)
            //2. Pick the node that is root node (always expand the root node)
            if (node.getNumberOfVisits() >= 20 || node == rootNode) { // always expand the rootnode
                node.expand(game);

                //run a simulation from each child
                for (MonteCarloGameNode child : node.getChildren()) {

                    int powerPillCount = game.getNumberOfActivePowerPills();
                    int pillCount = game.getNumberOfActivePills();
                    int level = game.getCurrentLevel(); // get the current level
                    MOVE move = child.getMove(); // get the childs move

                    //copy the game and play the move that this child represents
                    pushGameState();

                    //move the game state to this node
                    //play the move that the node represents
                    game.advanceGame(move, ghosts.getMove(game, 0));
                    //update active power pill indices if necessary
                    int[] indices = game.getActivePowerPillsIndices();
                    if (indices.length < activePowerPills.size()) {
                        updateActivePowerPills(indices);
                    }

                    // Repeat simulation till we find the next junction
                    while (!game.isJunction(game.getPacmanCurrentNodeIndex())) {
                        game.advanceGame(MyPacMan.nonJunctionSim(game), ghosts.getMove(game, 40));
                    }
                    // once again leave the junction before extending the simulation
                    MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
                    game.advanceGame(possibleMoves[random.nextInt(possibleMoves.length)],
                            ghosts.getMove(game.copy(), 40));

                    //if the move ate a power pill, mark it as such
                    if (game.getNumberOfActivePowerPills() < powerPillCount) {
                        child.setMoveEatsPowerPill(true);
                    }

                    //if the move ate any pills, mark it as such
                    if (game.getNumberOfActivePills() < pillCount) {
                        child.setMoveEatsPills(true);
                    }

                    int score = 0;

                    //if the move completes the level, give it a bonus
                    if (game.getCurrentLevel() > level)
                        score += 10000;

                    //run the roll out
                    score += runSimulation(visitedNodes, lives);
                    child.updateScore(score);

                    //restore the game state
                    popGameState();
                }

                node = new Ucb1SelectionPolicy().selectChild(node);
                if (node == null)
                    return;

                visitedNodes.add(node);

                //move the game state to this node
                //play the move that the node represents
                game.advanceGame(node.getMove(), ghosts.getMove(game, 0));
                //update active power pill indices if necessary
                int[] indices = game.getActivePowerPillsIndices();
                if (indices.length < activePowerPills.size()) {
                    updateActivePowerPills(indices);
                }
            }
            runSimulation(visitedNodes, lives);

        } finally {
            //restore the game state
            popGameState();
        }
    }


    /**
     * For a node based on its acton update the score
     * @return The updated score.
     */
    private int runSimulation(List<MonteCarloGameNode> visitedNodes, int lives) {
        int score = 0;

        //apply a penalty if we've lost a life
        if (game.getPacmanNumberOfLivesRemaining() < lives) {
            score -= 10000; // death penalty
        }

        //simulate the game to the end and get the score
        score += rollOut();

        //update the node scores
        for (MonteCarloGameNode n: visitedNodes) {
            n.updateScore(score);
        }

        return score;
    }

    /**
     * Plays a game using the specified ghost and pacman models until the end of level, game over or
     * simulation limit.
     * @return The score at the end of the simulation.
     */
    private int rollOut() {
        //save the level so we can end the simulation if Pac-Man progresses onto the next level
        int level = game.getCurrentLevel();
        int i = 0;

        //run up to the end of the level that is terminal state, until game over or until we've reached the simulation limit
        while (i++ < 10000 // simulations length
                && !game.gameOver()
                && game.getCurrentLevel() == level) {
            game.advanceGame(new StarterPacMan().getMove(game, 0), ghosts.getMove(game, 40));
        }

        //update the score at the end
        int score = game.getScore();

        return score;
    }

    /**
     * Saves the game state to a stack and uses a copy of it for the current game state.
     */
    public Game pushGameState() {
        gameStates.push(game);
        game = game.copy();
        return game;
    }

    /**
     * Pops a saved game state off the stack to use as the current game state.
     */
    public void popGameState() {
        game = gameStates.pop();
    }

    /**
     * Get the best move generated through MCTS.
     * @return
     */
    public MonteCarloGameNode bestNode() {

        double currentScore, max = Double.NEGATIVE_INFINITY;
        MonteCarloGameNode bestNode = null;
        MonteCarloGameNode searchNode;

        searchNode = rootNode;

        //make sure the search node has been expanded
        if (searchNode.getChildren() != null) {
            for (MonteCarloGameNode node: searchNode.getChildren()) {
                currentScore = node.getAverageScore();

                // System.out.println("Current Move: " + node.getMove() + " Score: " + currentScore);

                if (currentScore > max) {
                    max = currentScore;
                    bestNode = node;
                }
            }
        }
        // System.out.println("---- Selected : Move: " + bestNode.getMove() + " Score: " + max + " ----");

        return bestNode;
    }

    /**
     * Gets the collection of nodes which represents the moves PacMan can make.
     * @return childrens of the selected rootnode
     */
    public Collection<MonteCarloGameNode> getPacManChildren() {

        Collection<MonteCarloGameNode> children;
        children = rootNode.getChildren();

        //if there is no children, return an empty list instead of null
        if (children != null)
            return children;
        else
            return new ArrayList<>();
    }

    /**
     * Gets the current game state.
     * @return game
     */
    public Game getGameState() {
        return game;
    }

    /**
     * Sets the root node of the tree.
     * @param node
     */
    public void setRootNode(MonteCarloGameNode node) {
        rootNode = node;
    }

    /**
     * Updates the locations of the active power pills.
     * @param indices
     */
    private void updateActivePowerPills(int[] indices) {
        for (int index: indices) {
            activePowerPills.add(index);
        }
    }
}

