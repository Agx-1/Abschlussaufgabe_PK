import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by fabian on 15.01.16.
 */
public class Territory implements Occupyable {

    private final String name;
    private Point capital;
    private JLabel labelCapital = new JLabel();
    private int armies;
    private int occupied = -1;
    private LinkedList<Polygon> patches = new LinkedList<>();
    private LinkedList<Territory> neighbors = new LinkedList<>();


    public Territory(String name, Polygon patch){

        this.name = name;
        this.armies = 0;
        patches.add(patch);
    }

    public Territory(String name, int[] capitalCoordinates){

        this.name = name;
        this.armies = 0;
        this.capital = new Point(capitalCoordinates[0], capitalCoordinates[1]);

        initCapital();
    }

    public int getArmies(){

        return armies;
    }

    public void removeArmy(){

        armies--;
        labelCapital.setText(Integer.toString(armies));
    }

    public void addReinforcement(){

        armies++;
//        System.out.println("armies in territory: " + armies);
        labelCapital.setText(Integer.toString(armies));
    }

    public void setOccupied(int occupied){

        this.occupied = occupied;
    }

    public int getOccupied(){

        return occupied;
    }

    public void occupy(int player, int armies){

        occupied = player;
        this.armies = armies;

        labelCapital.setText("" + armies);

    }

    public void addPatch(Polygon patch){

        patches.add(patch);
    }

    public LinkedList<Polygon> getPatches(){

        return patches;
    }

    public void addCapital(int[] capitalCoordinates){

        capital = new Point(capitalCoordinates[0], capitalCoordinates[1]);
        labelCapital.setText(Integer.toString(armies));

        initCapital();
    }

    public void addNeighbor(Territory s){

        neighbors.add(s);
    }

    public LinkedList<Territory> getNeighbors(){

        return neighbors;
    }

    public boolean isNeighborOf(Territory territory){

        return neighbors.contains(territory);
    }

    public Point getCapitalLocation(){

        return capital;
    }

    public void moveArmyTo(Territory target){

        if(this.armies > 1){

            this.removeArmy();
            target.addReinforcement();
        }
    }

    public String getName(){

        return name;
    }

    public JLabel getCapital(){

        return labelCapital;
    }

    public void clearCapital(){

        labelCapital.setText("");
    }

    private void initCapital(){

        labelCapital = new JLabel(Integer.toString(this.getArmies()));

        labelCapital.setHorizontalAlignment(SwingConstants.CENTER);
        labelCapital.setVerticalAlignment(SwingConstants.CENTER);
        labelCapital.setFont(new Font("Arial", Font.BOLD, 14));
        labelCapital.setForeground(Color.BLACK);
        labelCapital.setSize(20, 20);

        labelCapital.setLocation(capital.x - labelCapital.getWidth() / 2,
                                 capital.y - labelCapital.getHeight() / 2);
    }
}
