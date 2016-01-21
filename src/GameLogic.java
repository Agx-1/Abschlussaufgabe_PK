import java.util.Arrays;

/**
 * Created by Moritz on 15.01.2016.
 */
public class GameLogic {

    //attacker has to have >1 armies in his Territory
    public static void attack(Territory attacker, Territory defendant)
    {
        int[] attackerDices = new int[3];
        int[] defendantDices = new int[2];
        int iterAttack = 0;
        int iterDefend = 0;

        if(attacker.getArmies() > 3){

            //if enough armies are present, send maximum of 3 into battle
            for (int i = 0; i < 3; i++) {

                attackerDices[i] = rollDice();
            }

        } else{

            //send all armies except one, therefore -1
            for (int i = 0; i < attacker.getArmies() - 1; i++) {

                attackerDices[i] = rollDice();
            }
        }

        if(defendant.getArmies() >= 2){

            //if enough armies are present, send maximum of 2
            for (int i = 0; i < 2; i++) {

                defendantDices[i] = rollDice();
            }

        } else{

            for (int i = 0; i < defendant.getArmies(); i++) {

                defendantDices[i] = rollDice();
            }
        }

        Arrays.sort(attackerDices);
        Arrays.sort(defendantDices);

        //just for testing, remove if finished
        System.out.println("Attacker Array: ");
        for (int i = 0; i < attackerDices.length; i++) {

            System.out.print(attackerDices[i] + ", ");
        }

        System.out.println();
        System.out.println("Defendant Array: ");
        for (int i = 0; i < defendantDices.length; i++) {

            System.out.print(defendantDices[i] + ", ");
        }
    }

    private static int rollDice(){

        return ((int)(Math.random()*6))%6 + 1;      //tested and approved
    }

    //just for testing
    public static void main(String[] args) {

        //GameLogic.attack(new OccupiedTerritory(4), new OccupiedTerritory(1));
    }
}