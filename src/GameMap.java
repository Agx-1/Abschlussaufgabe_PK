import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fabian on 15.01.16.
 */

public class GameMap extends JFrame{

    Map<String, OccupiedTerritory> territories = new HashMap<String, OccupiedTerritory>();

    public GameMap(String path){

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1250,650);
        this.setResizable(true);
        this.setVisible(true);

        MapHelper drawPanel = new MapHelper(territories);
        drawPanel.setPreferredSize(new Dimension(1250, 650));
        this.add(drawPanel);

        createMap(readMapFile(path));
    }

    public String[] readMapFile(String path){

        StringBuilder sb = new StringBuilder();

        try{

            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            while((line = br.readLine()) != null){
                sb.append(line);
                sb.append("\n");
            }
        }
        catch (IOException e){
            return null;
        }

        return sb.toString().split(System.getProperty("line.separator"));
    }

    public void createMap(String[] mapData) {

        //just for debugging
        for (int i = 0; i < mapData.length; i++) {
            System.out.println(mapData[i]);
        }
        //------------------

        String line;
        String territory;

        String[] helperCoordinates;
        int[] coordX;
        int[] coordY;

        for (int i = 0; i < mapData.length; i++) {

            if (mapData[i].startsWith("patch-of")) {

                //remove patch-of
                line = mapData[i].replace("patch-of ", "");

                //remove numbers
                territory = line.replaceAll("( [0-9]+)+", "");

                //remove territory
                line = mapData[i].replace("patch-of " + territory + " ", "");
                helperCoordinates = line.split(" ");

                //write content of String[] helperCoordinates into
                //two separate int[] coordinate-arrays

                coordX = new int[helperCoordinates.length/2];
                coordY = new int[helperCoordinates.length/2];

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

                if(territories.containsKey(territory)){

                    territories.get(territory).addPatch(new Polygon(coordX, coordY, coordX.length));

                } else{

                    territories.put(territory, new OccupiedTerritory(territory,
                                                                    new Polygon(coordX, coordY, coordX.length)));
                }
            }

            if (mapData[i].startsWith("capital-of")) {

                line = mapData[i].replace("capital-of ", "");
            }

            if (mapData[i].startsWith("neighbors-of")) {


            }

            if (mapData[i].startsWith("continent")) {


            }
        }

        repaint();
    }
}

