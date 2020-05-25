package entrants.ghosts.enesbehlul;

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.comms.Messenger;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GhostCommunication extends IndividualGhostController {


    public static Set<Integer> availablePillsIndices = new HashSet<Integer>();
    public static Set<Integer> eatedPowerPillsIndices = new HashSet<Integer>();


    private static Constants.GHOST[] protectedPowerPill = null;
    public static int[] powerPillIndices;
    private static int lastPacmanLocation;
    private static boolean isPacmanSeen;
    private static int kovalamaTimeOut = 0;
    private static int currentLevel = 0 ;
    public Constants.GHOST ghostType;
    public int currentGhostLocation;


    private int pacmanLocation;

    Messenger messenger;



    public int myDestinition = -1;
    public int [] myDestinitionPath = null;
    public String myDestinationReason = "";


    public GhostCommunication(Constants.GHOST ghost) {
        super(ghost);
        this.ghostType = ghost;
    }

    public int getPillIndex(int[] list, int index){
        for (int i = 0; i < list.length; i++){
            if (list[i] == index){
                return i;
            }
        }
        return -1;
    }


    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        //YAPILACAKLAR !!
        /*powerpill bittiyse diğer powerpille mi gitsin yoksa random pacmani görene kadar dolaşsın mı?liteden bakıp normal pilere yönelsin
        //en yakın pille git!!
        ??neden çünkü powerpillin etrafında çok toplanırsak toplu yenilebilir hayaletler çok puan aldırır.*/

        //pacman çok yakınsa powerpille buraya gelmesin diğer hayaletler.
        //pacmani gordugumuzde konumunun en yakın olduğu powerpill yenmişse diğer hayaletleri üzerine salalım

        //timer ekle bakalım 40 ms yi geçiyor mu!!!!!

        //burası pilerin tutulduğu yer
        Constants.GHOST myType = this.ghostType;
        currentGhostLocation = game.getGhostCurrentNodeIndex(this.ghostType);

        int pillIndex = getPillIndex(game.getPillIndices(), currentGhostLocation);
        if (pillIndex != -1) {
            System.out.println("ghost konumu: " + currentGhostLocation);
            if (game.isPillStillAvailable(pillIndex)) {
                GhostCommunication.availablePillsIndices.add(currentGhostLocation);
            } else {
                GhostCommunication.availablePillsIndices.remove(currentGhostLocation);
            }
        }


        int powerPillIndex = getPillIndex(game.getPowerPillIndices(), currentGhostLocation);

        if (powerPillIndex != -1) {
            if (!game.isPowerPillStillAvailable(powerPillIndex)) {
                GhostCommunication.eatedPowerPillsIndices.add(currentGhostLocation);
            }

        }

        if (GhostCommunication.eatedPowerPillsIndices.contains(myDestinition)) {
            myDestinition = -1;
            leavePowerPillTarget();
            myDestinationReason = "";

        }

        //----- buraya kadar -----

        //1292 baslangıc noktası

        pacmanLocation = game.getPacmanCurrentNodeIndex();
        GhostCommunication.powerPillIndices = game.getPowerPillIndices();
        //buradan
        for( int i = 0 ; i< GhostCommunication.powerPillIndices.length ; i++  ){
            if( GhostCommunication.eatedPowerPillsIndices.contains(GhostCommunication.powerPillIndices[i]) ){
                GhostCommunication.powerPillIndices[i] = -1;
            }

        }
        //buraya
        if (currentLevel != game.getCurrentLevel()) {
            currentLevel++;
            System.out.println("**YENI LEVEL'A GECILDI**");
            GhostCommunication.protectedPowerPill = new Constants.GHOST[GhostCommunication.powerPillIndices.length];
            myDestinationReason = "";
            myDestinition = -1;
        }
        if (GhostCommunication.protectedPowerPill == null)
            GhostCommunication.protectedPowerPill = new Constants.GHOST[GhostCommunication.powerPillIndices.length];


        if (currentGhostLocation == game.getGhostInitialNodeIndex()) {
            leavePowerPillTarget();
            myDestinationReason = "";
            myDestinition = -1;
        }

        if (currentGhostLocation == myDestinition) {
            leavePowerPillTarget();
            myDestinationReason = "";
            myDestinition = -1;
        }
        //pacmeni kovaladım ve yedim diyelim tekrar köşelere gidebilmek icin kovalama modundan çıkaya çalışıyorum böylelikle tekrar köşelere  gideceğim
        if (game.wasPacManEaten()) {
            leavePowerPillTarget();
            myDestinationReason = "";
            myDestinition = -1;
        }
        if (GhostCommunication.isPacmanSeen) {
            if (GhostCommunication.kovalamaTimeOut == 5 * 4) {
                //bu şey demekbir kere gördük ama artık görmüyorsak
                myDestinition = -1;
                GhostCommunication.isPacmanSeen = false;
                myDestinationReason = "we couldnt see any more";
            } else {
                GhostCommunication.kovalamaTimeOut++;
                leavePowerPillTarget();
                myDestinition = GhostCommunication.lastPacmanLocation;
                myDestinationReason = "Someone saw pacmen";
            }
        }

        //eğer birisi görürse zaten tekrar görünür kılar
        if (game.getPacmanCurrentNodeIndex() != -1) {

            //pacman bir yerde görüldü demektir.
            //kovalamam lazım
            //myDestination_pacmen oluyor.
            leavePowerPillTarget();
            myDestinition = game.getPacmanCurrentNodeIndex();
            //yetmez tüm ekip o namussuzu kovalamalıyız.
            GhostCommunication.isPacmanSeen = true;
            GhostCommunication.lastPacmanLocation = myDestinition;

            myDestinationReason = "I saw pacmen";
            GhostCommunication.kovalamaTimeOut = 0;

        }

        // artık hala hedeyif yoksa en yakın korunmayan Power Pill i korumaya gitmelyim
        if (myDestinition == -1) {
            for (int i = 0; i < GhostCommunication.powerPillIndices.length; i++) {
                //burası
                if(GhostCommunication.powerPillIndices[i] == -1) {
                    continue;
                }
                if (GhostCommunication.protectedPowerPill[i] != null && GhostCommunication.eatedPowerPillsIndices.size()>0 ){
                    myDestinition = GhostCommunication.powerPillIndices[i];
                    myDestinationReason = "Closest Power Pill, I am second guardian";
                    break;
                }
                //buraya
                if (GhostCommunication.protectedPowerPill[i] == null) {
                    GhostCommunication.protectedPowerPill[i] = this.ghostType;
                    myDestinition = GhostCommunication.powerPillIndices[i];
                    myDestinationReason = "Closest Power Pill";
                    break;
                }
            }
        }

        String txt = String.format("IAM %8s, IAM at %5d, My Destination %5d , Reason : %20s", this.ghostType, currentGhostLocation, myDestinition, myDestinationReason);
        System.out.println(txt);

        return go_there(currentGhostLocation, myDestinition, game);
    }

    public void leavePowerPillTarget(){
        for (int i = 0; i < GhostCommunication.powerPillIndices.length; i++) {
            if (GhostCommunication.protectedPowerPill[i] == this.ghostType) {
                GhostCommunication.protectedPowerPill[i] = null;
                break;
            }
        }
    }



    public Constants.MOVE go_there( int myLoc, int Des, Game game){
        try{myDestinitionPath = game.getShortestPath(myLoc, Des);}
        catch (Exception e){
            int a = 5;
        }
        if( game.isGhostEdible(this.ghostType) )
            return game.getNextMoveAwayFromTarget(myLoc, Des, game.getGhostLastMoveMade(this.ghostType),Constants.DM.MANHATTAN);
        return game.getNextMoveTowardsTarget(myLoc, Des, game.getGhostLastMoveMade(this.ghostType),Constants.DM.MANHATTAN);
        /*
        for(int i = 0 ; i< myDestinitionPath.length ; i++){
            if (game.isJunction(myDestinitionPath[i]))
                return game.getNextMoveTowardsTarget(myLoc, myDestinitionPath[i], game.getGhostLastMoveMade(this.ghostType),Constants.DM.MANHATTAN);
        }
        return null;
        */
    }


}