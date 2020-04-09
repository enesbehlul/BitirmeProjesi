package entrants.pacman.enesbehlul.evaluators;

import java.util.Collection;

import entrants.pacman.enesbehlul.MonteCarloGameNode;
import entrants.pacman.enesbehlul.MonteCarloTree;
import pacman.game.Constants.GHOST;
import pacman.game.Game;


/**
 * Adds a penalty to a move which eats a power pill if there is still a power pill active.
 */
public class PowerPillActiveEvaluator implements ITreeEvaluator {

	private static final int DEFAULT_PENALTY = 300;
	private int penalty;
	
	
	public PowerPillActiveEvaluator(int penalty) {
		this.penalty = penalty;
	}
	
	
	public PowerPillActiveEvaluator() {
		this(DEFAULT_PENALTY);
	}
	
	
	@Override
	public void evaluateTree(MonteCarloTree simulator) {
		//get the children of the root node, if there aren't any we can't make any decisions
		Collection<MonteCarloGameNode> children = simulator.getPacManChildren();
		
		if (children == null)
			return;
		
		Game game = simulator.getGameState();
		
		if (isPowerPillActive(game)) {
			//no ghosts nearby, penalise nodes which eat power pills
			for (MonteCarloGameNode child: children) {
				if (child.getMoveEatsPowerPill()) {
					child.addScoreBonus(-penalty);
				}
			}
		}
	}
	
	
	/**
	 * Returns true if any ghosts are edible; otherwise, returns false.
	 * @param game
	 * @return
	 */
	private boolean isPowerPillActive(Game game) {
		int edibleTime = game.getGhostEdibleTime(GHOST.BLINKY)
			+ game.getGhostEdibleTime(GHOST.INKY)
			+ game.getGhostEdibleTime(GHOST.PINKY)
			+ game.getGhostEdibleTime(GHOST.SUE);
		
		return edibleTime > 0;
	}
}
