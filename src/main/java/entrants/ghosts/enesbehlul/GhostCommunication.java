package entrants.ghosts.enesbehlul;

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.comms.BasicMessage;
import pacman.game.comms.Message;
import pacman.game.comms.Messenger;

public class GhostCommunication extends IndividualGhostController {

    private static int lastPacmanLocation;
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
        if (game.getGhostCurrentNodeIndex(ghost) == lastPacmanLocation){
            lastPacmanLocation = -1;
        }
        //System.out.println("pacman last loc " + ghost + " " + lastPacmanLocation);
        if (pacmanLocation != -1){
            lastPacmanLocation = pacmanLocation;
            pacmanSeen = true;
            messenger = game.getMessenger();
            tickSeen = game.getCurrentLevelTime();
            messenger.addMessage(new BasicMessage(ghost,null, BasicMessage.MessageType.PACMAN_SEEN, lastPacmanLocation, tickSeen));
        } else {
            //lastPacmanLocation = -1;
            pacmanSeen = false;
        }

        //eger pacmani gormuyorsam, goren arkadasim mesaj gondermis mi
        if (!pacmanSeen && messenger != null){
            for(Message message : messenger.getMessages(ghost)){
                if (message.getType() == BasicMessage.MessageType.PACMAN_SEEN ){
                    //System.out.println(message.getSender() + "goruldu.");
                    lastPacmanLocation = message.getData();
                }
            }
            if (lastPacmanLocation != 0 && lastPacmanLocation != -1)
                return game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),lastPacmanLocation, game.getGhostLastMoveMade(ghost), Constants.DM.PATH);
        }
        if (lastPacmanLocation != 0 && lastPacmanLocation != -1)
            return game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),lastPacmanLocation, game.getGhostLastMoveMade(ghost), Constants.DM.PATH);
        return Constants.MOVE.UP;
    }
}
