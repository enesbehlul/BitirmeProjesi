package entrants.pacman.enesbehlul;

import java.util.Collection;

/**
 * Provides UCB calculation based on the UCB1 formula.
 */
public class Ucb1SelectionPolicy  {

    private static final double DEFAULT_BALANCE = 10000;
    private double balanceParameter;

    /**
     * Constructor.  Uses the default balance parameter.
     */
    public Ucb1SelectionPolicy() {
        this.balanceParameter = DEFAULT_BALANCE;
    }

    /**
     * Selects the best child using UCB calculations
     * @param node
     * @return child
     */
    public MonteCarloGameNode selectChild(MonteCarloGameNode node)  {

        Collection<MonteCarloGameNode> children = node.getChildren();
        MonteCarloGameNode selectedChild = null;
        double max = Double.NEGATIVE_INFINITY;
        double currentUcb;

        if (children == null)
            throw new IllegalStateException("Cannot call selectChild on a leaf node.");

        for (MonteCarloGameNode child: children) {
            currentUcb = getUcbValue(child);
            if (currentUcb > max) {
                max = currentUcb;
                selectedChild = child;
            }
        }

        return selectedChild;
    }

    // Lecture note: MCTS - UCB1 calculations (CIG 6:14)
    public double getUcbValue(MonteCarloGameNode node) {
        return node.getAverageScore() + balanceParameter *
                Math.sqrt(Math.log(node.getParent().getNumberOfVisits()) / node.getNumberOfVisits());
    }
}
