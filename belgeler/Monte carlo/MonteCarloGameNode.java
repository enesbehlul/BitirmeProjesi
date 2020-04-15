package entrants.pacman.enesbehlul;

import java.util.*;

import pacman.controllers.MASController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;


public class MonteCarloGameNode
{
    private MOVE move;
    private int numberOfVisits, scoreBonus;
    private MonteCarloGameNode parent;
    private Map<Object, MonteCarloGameNode> children;
    private double mean;
    private boolean moveEatsPowerPill;
    private boolean moveEatsPills;

    /**
     * Constructor for root nodes.
     */
    public MonteCarloGameNode() {
        this.numberOfVisits = 0;
        this.move = MOVE.NEUTRAL;
        this.scoreBonus = 0;
        this.parent = null;
    }

    /**
     * Constructor for child nodes.
     * @param parent The parent of this node.
     * @param move The move that this node represents.
     */
    private MonteCarloGameNode(MonteCarloGameNode parent, MOVE move) {
        this.parent = parent;
        this.move = move;
    }

    /**
     * Updates the score and visit count of this node.
     * @param score
     */
    public void updateScore(int score) {
        numberOfVisits++;

        //calculate mean
        if (numberOfVisits == 1) {
            this.mean = score;
        }
        else {
            double lastMean = this.mean;
            this.mean += (score - lastMean) / numberOfVisits;
        }
    }


    /**
     * Adds a bonus to the average score.
     * @param bonus
     */
    public void addScoreBonus(int bonus) {
        this.scoreBonus += bonus;
    }


    /**
     * Expands this node by adding children based on the possible moves from the current position in game.
     * @param game
     */
    public void expand(Game game) {
        MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
        children = new HashMap<>(possibleMoves.length);

        for (int i = 0; i < possibleMoves.length; i++) {
            children.put(possibleMoves[i], new MonteCarloGameNode(this, possibleMoves[i]));
        }
    }


    /**
     * Determines if this node has any children or not.
     * @return True if the node has no children; otherwise, false.
     */
    public boolean isLeafNode() {
        return children == null;
    }


    /**
     * Gets the number of times this node has been visited.
     * @return
     */
    public int getNumberOfVisits() {
        return numberOfVisits;
    }

    /**
     * Gets the move that this node represents.
     * @return
     */
    public MOVE getMove() {
        return move;
    }


    /**
     * Gets the children of this node.
     * @return children
     */
    public Collection<MonteCarloGameNode> getChildren() {
        if (children == null)
            return null;

        return children.values();
    }

    /**
     * Gets the parent node for this node.
     * @return
     */
    public MonteCarloGameNode getParent() {
        return parent;
    }

    /**
     * Gets the average score for this node, plus any bonuses.
     * @return
     */
    public double getAverageScore() {
        if (numberOfVisits > 0)
            return mean + scoreBonus;
        else
            return scoreBonus;
    }

    /**
     * Gets whether or not this move eats a power pill.
     * @return True if, after executing this move, a power pill is eaten; otherwise, false.
     */
    public boolean getMoveEatsPowerPill() {
        return moveEatsPowerPill;
    }


    /**
     * Sets whether or not this move eats a power pill.
     * @param value
     */
    public void setMoveEatsPowerPill(boolean value) {
        moveEatsPowerPill = value;
    }


    /**
     * Gets whether or not this move results in one or more pills being eaten.
     * @return True if, after executing this move, one or more pills are eaten; otherwise, false.
     */
    public boolean getMoveEatsPills() {
        return moveEatsPills;
    }


    /**
     * Sets whether or not this move eats one or more pills.
     * @param value
     */
    public void setMoveEatsPills(boolean value) {
        moveEatsPills = value;
    }


    /**
     * Gets whether or not there exists a move subsequent to this move which results in pills being eaten.
     * @return
     */
    public boolean getCanEatPillsOnSubsequentMove() {
        //if there's no children, return false
        if (children == null)
            return false;

        //basically, this condition is true if any of the child moves eat pills
        for (MonteCarloGameNode child: children.values()) {
            if (child.getMoveEatsPills())
                return true;
        }

        return false;
    }
}
