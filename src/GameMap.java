import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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

        MapSurface drawPanel = new MapSurface(territories);
        drawPanel.setPreferredSize(new Dimension(1250, 650));
        this.add(drawPanel);

        createMap(readMapFile(path));
    }

    public String[] readMapFile(String path){

        StringBuilder sb = new StringBuilder();
        Scanner s;

        try {
            s = new Scanner(Paths.get(path));

            while(s.hasNextLine()){

                sb.append(s.nextLine());
                sb.append("\n");
            }
        }
        catch (IOException e){
            System.out.println(".map file not found");
            return null;
        }
//        try{
//
//            BufferedReader br = new BufferedReader(new FileReader(path));
//            String line;
//            while((line = br.readLine()) != null){
//                sb.append(line);
//                sb.append("\n");
//            }
//        }
//        catch (IOException e){
//            return null;
//        }


        //maybe the line.separator property is NOT system-specific (although it should be, of course...)
        //please try the good old \n instead and report whether it works

        return sb.toString().split(System.getProperty("line.separator"));
        //return sb.toString().split(System.getProperty("\n"));
    }

    public void createMap(String[] mapData) {

        //just for debugging
//        for (int i = 0; i < mapData.length; i++) {
//            System.out.println(mapData[i]);
//        }
        //------------------

//        //saves modifications to current line of .map file
//        String line;
//
//        //saves the name of the territory in current line (if present)
//        String territory;
//
//        String[] helperCoordinates;

        for (int i = 0; i < mapData.length; i++) {

            if (mapData[i].startsWith("patch-of")) {

                createPatch(mapData, i);
            }

            if (mapData[i].startsWith("capital-of")) {

                createCapital(mapData, i);
            }

            if (mapData[i].startsWith("neighbors-of")) {


            }

            if (mapData[i].startsWith("continent")) {


            }
        }

        repaint();
    }

    private void createPatch(String[] mapData, int i){

        //saves modifications to current line of .map file
        String line;

        //saves the name of the territory in current line (if present)
        String territory;

        String[] helperCoordinates;

        //arrays to pass Patch-coordinates to the Polygon constructor
        int[] coordX;
        int[] coordY;


        //remove patch-of
        line = mapData[i].replace("patch-of ", "");

        //remove numbers
        territory = line.replaceAll("( [0-9]+)+", "");

        //remove territory
        line = mapData[i].replace("patch-of " + territory + " ", "");
        helperCoordinates = line.split(" ");


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

    private void createCapital(String[] mapData, int i){

        String line;
        String territory;
        String[] helperCoordinates;

        //array to pass capital coordinates to .addCapital method
        int[] capitalCoordinates = new int[2];

        //remove capital-of
        line = mapData[i].replace("capital-of ", "");

        territory = line.replaceAll("( [0-9]+)+", "");

        helperCoordinates = line.replaceAll(territory + " ", "").split(" ");

        for (int j = 0; j < helperCoordinates.length; j++) {

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

    @Override
    public String toString(){

        String result = "";

        for (Map.Entry<String, OccupiedTerritory> entry : territories.entrySet()){

            result += "Territory <" +  entry.getKey() + ">\n     ";
            result += "capital: [" + entry.getValue().capital.getLocation().x + ", " +
                                    entry.getValue().capital.getLocation().y + "]\n     ";
            result += "patches: { ";

            for (Polygon p : entry.getValue().getPatches()){

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

