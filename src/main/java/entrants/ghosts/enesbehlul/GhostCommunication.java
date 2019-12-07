package entrants.ghosts.enesbehlul;

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.comms.BasicMessage;
import pacman.game.comms.Message;
import pacman.game.comms.Messenger;

public class GhostCommunication extends IndividualGhostController {

    private int lastPacmanLocation;
    private int pacmanLocation;
    private boolean pacmanSeen;
    Messenger messenger;
    private int tickSeen = -1;

    public GhostCommunication(Constants.GHOST ghost) {
        super(ghost);
    }

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        pacmanLocation = game.getPacmanCurrentNodeIndex();

        if (pacmanLocation != -1){
            lastPacmanLocation = pacmanLocation;
            pacmanSeen = true;
            messenger = game.getMessenger();
        } else {
            //lastPacmanLocation = -1;
            pacmanSeen = false;
        }
        if (pacmanSeen){
            tickSeen = game.getCurrentLevelTime();
            messenger.addMessage(new BasicMessage(ghost,null, BasicMessage.MessageType.PACMAN_SEEN, lastPacmanLocation, tickSeen));
        }

        if (!pacmanSeen && messenger != null){
            for(Message message : messenger.getMessages(ghost)){
                if (message.getType() == BasicMessage.MessageType.PACMAN_SEEN){
                    System.out.println("Haberini aldim kardesim, simdi pacmani dovmeye gidiyorum.");
                    return game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),lastPacmanLocation, game.getGhostLastMoveMade(ghost), Constants.DM.PATH);
                }
            }
        }
        return null;
    }
}
