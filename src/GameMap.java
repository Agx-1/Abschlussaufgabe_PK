import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by fabian on 15.01.16.
 */

public class GameMap extends JFrame{

    Map<String, OccupiedTerritory> territories = new HashMap<String, OccupiedTerritory>();
    MapSurface drawPanel;

    public GameMap(String path){

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1250,650);
        this.setResizable(true);
        this.setVisible(true);

        drawPanel = new MapSurface(territories);
        drawPanel.setPreferredSize(new Dimension(1250, 650));
        this.add(drawPanel);
        drawPanel.addMouseListener(drawPanel);

        createMap(readMapFile(path));
    }

    public void createMap(LinkedList<String> mapData) {

//        just for debugging
        for (String line : mapData){
            System.out.println(line);
        }
        //------------------

        for (String line : mapData){

            if(line.startsWith("patch-of")){

                createPatch(line);
            }

            if (line.startsWith("capital-of")) {

                createCapital(line);
            }
        }

        repaint();
    }

    private void createPatch(String line){

        //saves the name of the territory in current line (if present)
        String territory;

        String[] helperCoordinates;

        //arrays to pass Patch-coordinates to the Polygon constructor
        int[] coordX;
        int[] coordY;

        //remove patch-of
        line = line.replace("patch-of ", "");

        //get territory by removing coordinates
        territory = line.replaceAll("( [0-9]+)+", "");

        //get coordinates by removing territory
        helperCoordinates = line.replace(territory + " ", "").split(" ");

        coordX = new int[helperCoordinates.length/2];
        coordY = new int[helperCoordinates.length/2];

        //fill the two arrays with corresponding coordinates
        for (int j = 0; j < coordX.length; j++) {

            try{
                coordX[j] = Integer.parseInt(helperCoordinates[2*j]);
            }
            catch (NumberFormatException nfe) {}
        }

        for (int j = 0; j < coordY.length; j++) {

            try{
                coordY[j] = Integer.parseInt(helperCoordinates[2*j+1]);
            }
            catch (NumberFormatException nfe) {}
        }

        //either create a new entry in the territories Map or add patch to existing Territory
        if(territories.containsKey(territory)){

            territories.get(territory).addPatch(new Polygon(coordX, coordY, coordX.length));

        } else{

            territories.put(territory, new OccupiedTerritory(territory,
                                                                new Polygon(coordX, coordY, coordX.length)));
        }
    }

    private void createCapital(String line){

        String territory;
        String[] helperCoordinates;

        //array to pass capital coordinates to .addCapital method
        int[] capitalCoordinates = new int[2];

        //remove capital-of
        line = line.replace("capital-of ", "");

        //get territory by removing coordinates
        territory = line.replaceAll("( [0-9]+)+", "");

        //get coordinates by removing territory
        helperCoordinates = line.replaceAll(territory + " ", "").split(" ");

        for (int j = 0; j < capitalCoordinates.length; j++) {

            try {
                capitalCoordinates[j] = Integer.parseInt(helperCoordinates[j]);
            }
            catch (NumberFormatException nfe) {}
        }

        if(territories.containsKey(territory)){

            territories.get(territory).addCapital(capitalCoordinates);

        } else{

            territories.put(territory, new OccupiedTerritory(territory, capitalCoordinates));
        }


    }

    private LinkedList<String> readMapFile(String path){

        LinkedList<String> result = new LinkedList<>();
        Scanner s;

        try {
            s = new Scanner(Paths.get(path));

            while(s.hasNextLine()){

                result.add(s.nextLine());
            }
        }
        catch (IOException e){
            System.out.println(".map file not found");
            return null;
        }

        return result;
    }

    @Override
    public String toString(){

        String result = "";

        for (Map.Entry<String, OccupiedTerritory> entry : territories.entrySet()){

            result += "Territory <" +  entry.getKey() + ">\n     ";
            result += "capital: [" + entry.getValue().capital.getLocation().x + ", " +
                                    entry.getValue().capital.getLocation().y + "]\n     ";
            result += "patches: ";

            for (Polygon p : entry.getValue().getPatches()){

                result += "{ ";
                for (int i = 0; i < p.xpoints.length; i++) {

                    result += "[" + p.xpoints[i] + ";";
                    result +=       p.ypoints[i] + "], ";
                }
                result += " }" + "\n              ";
            }

            result += "\n";
        }

        return  result;
    }
}

