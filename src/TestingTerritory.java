/**
 * Created by fabian on 10.02.16.
 */
public class TestingTerritory implements Territory {

    private int armies = 0;

    public TestingTerritory(int armies){

        this.armies = armies;
    }

    @Override
    public int getArmies() {
        return armies;
    }

    @Override
    public void removeArmy() {

        armies--;
    }

    @Override
    public void addReinforcement() {

        armies++;
    }
}
