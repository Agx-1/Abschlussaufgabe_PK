import sun.reflect.generics.tree.Tree;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by fabian on 15.01.16.
 */
public class OccupiedTerritory implements Territory {

    private final String name;
    public Label capital = new Label("");
    private int armies;
    private List<Polygon> patches = new LinkedList<>();

    /**
     *
     * @param patch initial patch of land, which is part of the territory
     */
    public OccupiedTerritory(String name, Polygon patch){

        this.name = name;
        patches.add(patch);
    }

    public OccupiedTerritory(String name, int[] capitalCoordinates){

        this.name = name;
        this.capital.setLocation(capitalCoordinates[0], capitalCoordinates[1]);
    }

    public int getArmies(){

        return armies;
    }

    public void removeArmy(){

        armies--;
        capital.setText(Integer.toString(armies));
    }

    public void addReinforcement(){

        armies++;
        capital.setText(Integer.toString(armies));
    }

    public void addPatch(Polygon patch){

        patches.add(patch);
    }

    public List<Polygon> getPatches(){

        return patches;
    }

    public void addCapital(int[] capitalCoordinates){

        if (this.capital.getText() == ""){

            this.capital.setLocation(capitalCoordinates[0], capitalCoordinates[1]);
            capital.setText("0");
            capital.setVisible(true);
        }


    }
}