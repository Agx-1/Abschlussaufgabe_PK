import sun.reflect.generics.tree.Tree;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by fabian on 15.01.16.
 */
public class DummyTerritory implements Territory {

    private final String name;
    private String capital;
    private int armies;
    private LinkedList<Polygon> patches = new LinkedList<Polygon>();
    //private LinkedList<Patch> patches = new LinkedList<Patch>();
    //private Polygon[] = new Polygon[]

//    public DummyTerritory(int armies){
//
//        this.armies = armies;
//    }

    /**
     *
     * @param patch initial patch of land, which is part of the territory
     */
    public DummyTerritory(String name, Polygon patch){

        this.name = name;
        patches.offer(patch);
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

    public void addPatch(Polygon patch){

        patches.offer(patch);
    }

    public LinkedList<Polygon> getPatches(){

        return patches;
    }
}
