import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by fabian on 15.01.16.
 */

public class GameMap extends JFrame{

    Image img;
    Graphics grph;
    Graphics grph2;

    Map<String, DummyTerritory> territories = new HashMap();
    //int i;

    public static void main(String[] args) {

        GameMap gameMap = new GameMap();
        String[] mapData = readMapFile("maps/squares.map")
                .split(System.getProperty("line.separator"));

        gameMap.createMap(mapData);

        LinkedList<Patch> patches = null;

        for(Map.Entry<String, DummyTerritory> entry : gameMap.territories.entrySet()){

            System.out.println(entry.getKey() + ":");
            patches = entry.getValue().getPatches();

            while(patches.peek() != null){

                System.out.print("x: ");
                for (int i = 0; i < patches.peek().getBoarders().xpoints.length; i++) {

                    System.out.print(patches.peek().getBoarders().xpoints[i] + " ");
                }
                System.out.println();

                System.out.print("y: ");

                for (int i = 0; i < patches.peek().getBoarders().ypoints.length; i++) {

                    System.out.print(patches.peek().getBoarders().ypoints[i] + " ");
                }

                System.out.println();

                patches.poll();
            }
        }

    }

    public GameMap(){

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1250,650);
        this.setResizable(true);
        this.setVisible(true);
        //i = 0;
    }

    @Override
    public void paint(Graphics g){

        img = createImage(1250,650);
        grph = img.getGraphics();
        grph2 = img.getGraphics();
        paintComponent(grph);
        grph2.setColor(Color.green);
        grph2.drawRect(650, 100, 200, 200);

        LinkedList<Patch> patches = null;

        for(Map.Entry<String, DummyTerritory> entry : territories.entrySet()){

            patches = entry.getValue().getPatches();
        }

        while (patches != null && patches.peek() != null){

            g.drawPolygon(patches.poll().getBoarders());
        }

        g.drawImage(img, 0, 0, this);
    }

    public void paintComponent(Graphics g){


        g.setColor(new Color(0,0,150));
        g.fillRect(0, 0, 10,10);

        repaint();
    }

    public static String readMapFile(String path){

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

        return sb.toString();
    }

    public void createMap(String[] mapData) {

        for (int i = 0; i < mapData.length; i++) {
            System.out.println(mapData[i]);
        }
        String line;
        String territory;

        String[] helperCoordinates;
        int[] coordinates;

        for (int i = 0; i < mapData.length; i++) {

            if (mapData[i].startsWith("patch-of")) {

                //remove patch-of
                line = mapData[i].replace("patch-of ", "");

                //remove numbers
                territory = line.replaceAll("( [0-9]+)+", "");

                //remove territory
                line = mapData[i].replace("patch-of " + territory, "");
                helperCoordinates = line.split(" ");

                //write content of String[] helperCoordinates into int[] coordinates
                coordinates = new int[helperCoordinates.length];

                for (int j = 0; j < helperCoordinates.length; j++) {

                    try{
                        coordinates[j] = Integer.parseInt(helperCoordinates[j]);
                    }
                    catch (NumberFormatException nfe) {}
                }

                if(territories.containsKey(territory)){

                    territories.get(territory).addPatch(new Patch(territory, coordinates));

                } else{

                    territories.put(territory, new DummyTerritory(territory, new Patch(territory, coordinates)));
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
    }

    private void neverCalledJustStorage(){

    }
}
