import java.util.Arrays;

/**
 * Created by Moritz on 15.01.2016.
 */
public class GameLogic {

    //attacker has to have >1 armies in his Territory
    public void attack(Territory attacker, Territory defendant)
    {
        int[] attackerDices = new int[3];
        int[] defendantDices = new int[2];
        int iterAttack = 0;
        int iterDefend = 0;

        while (attacker.getArmys() > 1 && iterAttack < 3){

            attackerDices[iterAttack] = rollDice();
            iterAttack++;
        }

        while (defendant.getArmys() > 0 && iterDefend < 2){

            defendantDices[iterDefend] = rollDice();
            iterDefend++;
        }

        Arrays.sort(attackerDices);
        Arrays.sort(defendantDices);

        //just for testing, remove if finished
        System.out.println("Attacker Array: ");
        for (int i = 0; i < attackerDices.length; i++) {

            System.out.println(attackerDices[i] + ", ");
        }

        System.out.println("Defendant Array: ");
        for (int i = 0; i < defendantDices.length; i++) {

            System.out.println(defendantDices[i] + ", ");
        }
    }


    private int rollDice(){

        return ((int)(Math.random()*6))%6 + 1;
    }
}