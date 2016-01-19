import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by fabian on 15.01.16.
 */
public class DummyTerritory implements Territory {

    String name;
    String capital;
    int armies;
    LinkedList<Patch> patches = new LinkedList<Patch>();

    public DummyTerritory(int armies){

        this.armies = armies;
    }

    public DummyTerritory(String name, Queue<Patch> patches){

        this.name = name;
        while (patches.peek() != null){

            this.patches.offer(patches.poll());
        }
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
