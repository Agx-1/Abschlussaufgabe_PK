import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by fabian on 15.01.16.
 */
public class Territory implements VoidTerritory {

    private final String name;
    public JLabel capital = new JLabel("");
    private int armies;
    private int occupied = -1;
    private LinkedList<Polygon> patches = new LinkedList<>();
    private ArrayList<String> neighbors = new ArrayList<>();


    public Territory(String name, Polygon patch){

        this.name = name;
        patches.add(patch);
    }

    public Territory(String name, int[] capitalCoordinates){

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

    public LinkedList<Polygon> getPatches(){

        return patches;
    }

    public void addCapital(int[] capitalCoordinates){

        this.capital.setLocation(capitalCoordinates[0], capitalCoordinates[1]);
        capital.setText("42");
        capital.setVisible(true);
    }

    public void setOccupied(int occupied){

        this.occupied = occupied;
    }

    public int getOccupied(){

        return occupied;
    }

    public void setNeighbor(String s){          //set-Funktion die einzelnen Nachbarn hinzufügen lässt

        neighbors.add(s);
    }

    public ArrayList<String> getNeighbors(){    //gibt alle Nachbarn zurück

        return neighbors;
    }

    public boolean hasNeighbor(String s){

        return neighbors.contains(s);
    }

}
