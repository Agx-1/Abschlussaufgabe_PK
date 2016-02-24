import java.util.Arrays;

/**
 * Created by Moritz on 15.01.2016.
 */
public class Game {        //class is probably obsolete, remove if finished

    // -1 stands for claim phase, 0 for reinforce, 1 for attacking and moving
    public static int phase = -1;
    public static final int playerCount = 2;    //number of players participating in the game
    public static int currentPlayer = 1;       //0 for computer, upcoming integers for human players
    public static int occupiedTerritories = 0;
    public static int round = 1;
    private static Occupyable currentlyConquered = null;

    //attacker has to have >1 armies in his VoidTerritory
    public static void attack(Occupyable attacker, Occupyable defender) {
        int[] attackerDices;
        int[] defenderDices;

        final int maxAttackers = 3;      //how many armies can attack at once
        final int maxDefenders = 2;      //how many armies can defend at once

        System.out.printf("Player %d attacks <%s> from <%s> \n",
                Game.currentPlayer, defender.getName(), attacker.getName());

        if(attacker.getArmies() > maxAttackers){

            attackerDices = new int[maxAttackers];

            //if enough armies are present, send maximum into battle
            for (int i = 0; i < attackerDices.length; i++) {

                attackerDices[i] = rollDice();
            }

        } else{

            //send all armies except one, therefore -1
            attackerDices = new int[attacker.getArmies() - 1];

            for (int i = 0; i < attackerDices.length; i++) {

                attackerDices[i] = rollDice();
            }
        }

        if(defender.getArmies() >= maxDefenders){

            defenderDices = new int[maxDefenders];

            //if enough armies are present, send maximum of 2
            for (int i = 0; i < defenderDices.length; i++) {

                defenderDices[i] = rollDice();
            }

        } else{

            defenderDices = new int[defender.getArmies()];

            for (int i = 0; i < defenderDices.length; i++) {

                defenderDices[i] = rollDice();
            }
        }

        Arrays.sort(attackerDices);
        Arrays.sort(defenderDices);

        attackerDices = reverse(attackerDices);
        defenderDices = reverse(defenderDices);

        //just for testing, remove if finished
        System.out.print("Attacker Array: ");
        for (int i = 0; i < attackerDices.length; i++) {

            System.out.print(attackerDices[i] + ", ");
        }

        System.out.println();
        System.out.print("Defender Array: ");
        for (int i = 0; i < defenderDices.length; i++) {

            System.out.print(defenderDices[i] + ", ");
        }

        //taking the minimum assures the index staying in the array, even if there are more defenders than attackers
        for (int i = 0; i < Math.min(defenderDices.length, attackerDices.length); i++) {

                if (attackerDices[i] > defenderDices[i]) {

                    defender.removeArmy();
                    //System.out.println("One defender died.");

                } else {

                    attacker.removeArmy();
                    //System.out.println("One attacker died.");
                }

            if(defender.getArmies() == 0){

                defender.occupy(currentPlayer, attackerDices.length);
                setCurrentlyConquered(defender);

                for (int j = 0; j < attackerDices.length; j++) {
                    attacker.removeArmy();
                }

                System.out.println();
                System.out.println();
                System.out.printf("<%s> occupied <%s> with %d armies.\n\n",
                        attacker.getName(), defender.getName(), attackerDices.length);
            }
        }

        if(defender.getOccupied() != attacker.getOccupied()){

//            System.out.printf("Armies defender: %2d\n", defender.getArmies());
//            System.out.printf("Armies attacker: %2d", attacker.getArmies());
            System.out.println();
            System.out.println();
        }
    }

    private static int rollDice(){

        return ((int)(Math.random()*6))%6 + 1;      //tested and approved

    }

    private static int[] reverse(int[] array){

        int[] result = new int[array.length];

        for (int i = 0; i < array.length; i++) {

            result[i] = array[array.length - i - 1];
        }

        return result;
    }

    public static void setCurrentlyConquered(Occupyable territory){

        currentlyConquered = territory;
    }

    public static Occupyable getCurrentlyConquered(){

        return currentlyConquered;
    }

}