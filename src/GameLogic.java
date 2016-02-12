import java.util.Arrays;

/**
 * Created by Moritz on 15.01.2016.
 */
public class GameLogic {

    //attacker has to have >1 armies in his VoidTerritory
    public static void attack(VoidTerritory attacker, VoidTerritory defender)
    {
        int[] attackerDices;
        int[] defenderDices;

        final int maxAttackers = 3;      //how many armies can attack at once
        final int maxDefenders = 2;      //how many armies can defend at once

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
        System.out.println("Attacker Array: ");
        for (int i = 0; i < attackerDices.length; i++) {

            System.out.print(attackerDices[i] + ", ");
        }

        System.out.println();
        System.out.println("Defender Array: ");
        for (int i = 0; i < defenderDices.length; i++) {

            System.out.print(defenderDices[i] + ", ");
        }
        System.out.println();
        System.out.println();

        //taking the minimum assures the index staying in the array, even if there are more defenders than attackers
        for (int i = 0; i < Math.min(defenderDices.length, attackerDices.length); i++) {

                if (attackerDices[i] > defenderDices[i]) {

                    defender.removeArmy();
                    System.out.println("One defender died.");

                } else {

                    attacker.removeArmy();
                    System.out.println("One attacker died.");
                }

            if(defender.getArmies() == 0){

                System.out.println("Attacker occupied territory with " + attackerDices.length + " armies.");
            }
        }

        System.out.println("Armies defender: " + defender.getArmies());
        System.out.println("Armies attacker: " + attacker.getArmies());
    }

    public static void distributeReinforcements(){

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
}