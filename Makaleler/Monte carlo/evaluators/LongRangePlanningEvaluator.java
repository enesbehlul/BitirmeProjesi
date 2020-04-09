package entrants.pacman.enesbehlul.evaluators;

import entrants.pacman.enesbehlul.MonteCarloGameNode;
import entrants.pacman.enesbehlul.MonteCarloTree;
import entrants.pacman.enesbehlul.MyPacMan;
import pacman.controllers.MASController;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Game;

import java.util.Random;

/**
 * This evaluator awards a score to any move which decreases the distance to the nearest pill.
 */
public class LongRangePlanningEvaluator implements ITreeEvaluator {
	private static final int DEFAULT_BONUS = 200;
	private int bonus;
	
	public LongRangePlanningEvaluator(int bonus) {
		this.bonus = bonus;
	}
	
	public LongRangePlanningEvaluator() {
		this(DEFAULT_BONUS);
	}
	
	
	@Override
	public void evaluateTree(MonteCarloTree tree) {
		//get the current distance to the nearest pill
		Game game = tree.getGameState();
		double distance = getDistanceToNeartestPill(game);
		
		//see if it improves for each of the children
		for (MonteCarloGameNode child: tree.getPacManChildren()) {
			//save the game state and play the move
			game = tree.pushGameState();

			//move the game state to this node
			//play the move that the node represents
			MASController ghosts = new POCommGhosts(50);
			game.advanceGame(child.getMove(), ghosts.getMove(game, 0));

			// Repeat simulation till we find the next junction
			while (!game.isJunction(game.getPacmanCurrentNodeIndex())) {
				game.advanceGame(MyPacMan.nonJunctionSim(game), ghosts.getMove(game, 40));
			}
			// once again leave the junction before extending the simulation
			Constants.MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
			Random random = new Random();
			game.advanceGame(possibleMoves[random.nextInt(possibleMoves.length)],
					ghosts.getMove(game.copy(), 40));

			//award a bonus if the distance is less now
			double d = getDistanceToNeartestPill(game);
			
			if (d < distance)
				child.addScoreBonus(bonus);
			
			//restore the game state
			tree.popGameState();
		}
	}


	/**
	 * Get the closest pill to the current position
	 * @param game
	 */
	private double getDistanceToNeartestPill(Game game) {
		int[] pills = game.getActivePillsIndices();
		
		if (pills.length == 0)
			return Double.MAX_VALUE;
		
		int currentIndex = game.getPacmanCurrentNodeIndex();
		int closestPill = game.getClosestNodeIndexFromNodeIndex(currentIndex, pills, DM.MANHATTAN);
		
		//return the distance to it
		return game.getDistance(currentIndex, closestPill, DM.MANHATTAN);
	}
}
