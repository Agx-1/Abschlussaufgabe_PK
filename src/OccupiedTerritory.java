import sun.reflect.generics.tree.Tree;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

/**
 * Created by fabian on 15.01.16.
 */
public class OccupiedTerritory implements Territory {

    private final String name;
    private String capital;
    private int armies;
    private List<Polygon> patches = new LinkedList<>();
    //private LinkedList<Patch> patches = new LinkedList<Patch>();
    //private Polygon[] = new Polygon[]

//    public OccupiedTerritory(int armies){
//
//        this.armies = armies;
//    }

    /**
     *
     * @param patch initial patch of land, which is part of the territory
     */
    public OccupiedTerritory(String name, Polygon patch){

        this.name = name;
        patches.add(patch);
    }

//    public OccupiedTerritory(String name, Queue<Patch> patches){
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

        patches.add(patch);
    }

    public List<Polygon> getPatches(){

        return patches;
    }
}
