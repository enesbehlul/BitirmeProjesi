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
    public static int[] powerPillIndices = new int[4];
    private static int lastPacmanLocation;
    private static boolean isPacmanSeen;
    private static int kovalamaTimeOut = 0;
    private static int currentLevel = 0 ;
    public Constants.GHOST ghostType;
    public int currentGhostLocation;

    Messenger messenger;



    public int myDestinition = -1;
    public int [] myDestinitionPath = null;
    public String myDestinationReason = "";


    public GhostCommunication(Constants.GHOST ghost) {
        super(ghost);
        this.ghostType = ghost;
    }

    void copyPowerPillIndicesArray(int[] array){
        for (int i = 0; i<array.length; i++){
            GhostCommunication.powerPillIndices[i] = array[i];
        }
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


        //1292 baslangıc noktası
        currentGhostLocation = game.getGhostCurrentNodeIndex(this.ghostType);
        //bunu current maze loc çalışmadığı için yaptım


        if (GhostCommunication.protectedPowerPill == null || GhostCommunication.protectedPowerPill == null || game.gameOver() == true || game.getTotalTime() == 0){

            copyPowerPillIndicesArray(game.getPowerPillIndices());
            GhostCommunication.protectedPowerPill = new Constants.GHOST[GhostCommunication.powerPillIndices.length];
            GhostCommunication.availablePillsIndices.clear();
            GhostCommunication.eatedPowerPillsIndices.clear();
            if(game.gameOver() || game.getTotalTime() == 0 ){
                GhostCommunication.currentLevel = 0;
            }
        }

        //yeni level kontrolü
        if (GhostCommunication.currentLevel != game.getCurrentLevel() ) {
            GhostCommunication.currentLevel++;
            System.out.println("YENI LEVEL'A GECILDI");
            GhostCommunication.availablePillsIndices.clear();
            GhostCommunication.eatedPowerPillsIndices.clear();
            copyPowerPillIndicesArray(game.getPowerPillIndices());
            GhostCommunication.protectedPowerPill = new Constants.GHOST[GhostCommunication.powerPillIndices.length];
            myDestinationReason = "";
            myDestinition = -1;
        }


        if ( (1292 == currentGhostLocation && currentLevel == 0) || (currentLevel ==1 && 1318 == currentGhostLocation) ){
            System.out.println("I am at MAZEEE!!!!");
            return null;
        }

        //burası pillerin tutulduğu yer
        int pillIndex = getPillIndex(game.getPillIndices(), currentGhostLocation);
        if (pillIndex != -1) {
            //System.out.println("ghost konumu: " + currentGhostLocation);
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

        //----- buraya kadar -----


        /**/
        //secound guardian için mydes belirleme
        if (GhostCommunication.eatedPowerPillsIndices.contains(myDestinition)) {
            myDestinition = -1;
            myDestinationReason = "";
            for (int i = 0; i < GhostCommunication.powerPillIndices.length; i++) {
                if (GhostCommunication.protectedPowerPill[i] == this.ghostType) {
                    GhostCommunication.protectedPowerPill[i] = null;
                    GhostCommunication.powerPillIndices[i] = -1;
                    break;
                }
            }
        }




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

        if (GhostCommunication.isPacmanSeen) {

            if (GhostCommunication.kovalamaTimeOut == 4 * 4) {
                //bu şey demek bir kere gördük ama artık görmüyorsak
                myDestinition = -1;
                GhostCommunication.isPacmanSeen = false;
                myDestinationReason = "we couldnt see any more";
            }
            else {
                GhostCommunication.kovalamaTimeOut++;
                leavePowerPillTarget();
                myDestinition = GhostCommunication.lastPacmanLocation;
                myDestinationReason = "Someone saw pacmen";
            }
            if(game.isGhostEdible(this.ghostType)){


            }
        }




        // artık hala hedeyif yoksa en yakın korunmayan Power Pill i korumaya gitmelyim
        if (myDestinition == -1) {
            for (int i = 0; i < GhostCommunication.powerPillIndices.length; i++) {
                if(GhostCommunication.powerPillIndices[i] == -1) {
                    continue;
                }
                if (GhostCommunication.protectedPowerPill[i] != null && GhostCommunication.eatedPowerPillsIndices.size()>0 && countNonProtectedPowerPills() == GhostCommunication.eatedPowerPillsIndices.size() ){
                    myDestinition = GhostCommunication.powerPillIndices[i];
                    myDestinationReason = "Closest Power Pill, I am second guardian";
                    break;
                }
                if (GhostCommunication.protectedPowerPill[i] == null) {
                    GhostCommunication.protectedPowerPill[i] = this.ghostType;
                    myDestinition = GhostCommunication.powerPillIndices[i];
                    myDestinationReason = "Closest Power Pill";
                    break;
                }
            }
        }

        //System.out.println(txt);
        /*
        eğer her hesaplamaya rağmen hedef hala yoksa yani 4 powr pil bitmiş kimse second gardiyan olamıyorsa
        asagıda en yakın nromal fill i buluyorum ve ona gönlendiriyorum adamı

         */
        int _pillIndex = -1;
        int min = 9999, targetPill = -1;
        try {
            if (myDestinition == -1) {

                for (int i = 0; i < GhostCommunication.availablePillsIndices.size(); i++) {
                    _pillIndex = (int) GhostCommunication.availablePillsIndices.toArray()[i];
                    if (_pillIndex > 0) {

                        if (game.getShortestPath(currentGhostLocation, _pillIndex).length <= min) {
                            targetPill = _pillIndex;
                            myDestinition = targetPill;
                            myDestinationReason = "Closest normal Pill";
                        }
                    }
                }

            }
        }catch (Exception e){
            System.out.println("HATAAA");
            System.out.println("currentGhostLocation" + currentGhostLocation);
            System.out.println("_pillIndex" + _pillIndex);
            System.out.println(e);
            return null;
        }
        System.out.println(GhostCommunication.eatedPowerPillsIndices);
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
    public int countNonProtectedPowerPills(){
        int count = 0 ;
        for(int i = 0 ; i< GhostCommunication.protectedPowerPill.length;i++){
            if(GhostCommunication.protectedPowerPill[i] == null){
                count++;
            }
        }
        return count;
    }


    public Constants.MOVE go_there( int myLoc, int Des, Game game) {
        if (Des == -1)
            return null;
        try {
            myDestinitionPath = game.getShortestPath(myLoc, Des);
        } catch (Exception e) {
            int a = 5;
        }
        if (game.isGhostEdible(this.ghostType))
            return game.getNextMoveAwayFromTarget(myLoc, Des, game.getGhostLastMoveMade(this.ghostType), Constants.DM.MANHATTAN);
        return game.getNextMoveTowardsTarget(myLoc, Des, game.getGhostLastMoveMade(this.ghostType), Constants.DM.EUCLID);

        /*
        for(int i = 0 ; i< myDestinitionPath.length ; i++){
            if (game.isJunction(myDestinitionPath[i]))
                return game.getNextMoveTowardsTarget(myLoc, myDestinitionPath[i], game.getGhostLastMoveMade(this.ghostType),Constants.DM.MANHATTAN);
        }
        return null;
        */
    }




}