/**
 * Created by fabian on 15.01.16.
 */
public class DummyTerritory implements Territory {

    int armies;

    DummyTerritory(int armies){

        this.armies = armies;
    }


    public int getArmies(){

        return armies;
    }

    public void removeArmy(){

        armies--;
    }

    public void addReinforcement(){

        armies++;
    }
}
