import sun.reflect.generics.tree.Tree;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by fabian on 15.01.16.
 */
public class DummyTerritory implements Territory {

    private final String name;
    private String capital;
    private int armies;
    private LinkedList<Patch> patches = new LinkedList<Patch>();

//    public DummyTerritory(int armies){
//
//        this.armies = armies;
//    }

    /**
     *
     * @param p initial patch of land contained in the Territory
     */
    public DummyTerritory(String name, Patch p){

        this.name = name;
        patches.offer(p);
    }

//    public DummyTerritory(String name, Queue<Patch> patches){
//
//        this.name = name;
//        while (patches.peek() != null){
//
//            this.patches.offer(patches.poll());
//        }
//    }


    public int getArmies(){

        return armies;
    }

    public void removeArmy(){

        armies--;
    }

    public void addReinforcement(){

        armies++;
    }

    public void addPatch(Patch p){

        patches.offer(p);
    }
}
