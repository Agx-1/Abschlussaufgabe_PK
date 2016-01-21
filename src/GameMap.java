import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
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
    //LinkedList<Patch> patches = null;

    Map<String, DummyTerritory> territories = new HashMap<String, DummyTerritory>();
    //int i;

    public static void main(String[] args) {

        GameMap gameMap = new GameMap();
        String[] mapData = readMapFile("maps/squares.map")
                .split(System.getProperty("line.separator"));

        gameMap.createMap(mapData);

        LinkedList<Polygon> patches;

        if (gameMap.territories.get("Southern").getPatches().peek() == null)
            System.out.println("Map empty at beginning!!!!!!!!!!!!!!!!!!!!!!!!!!11111!");

        for(Map.Entry<String, DummyTerritory> entry : gameMap.territories.entrySet()){

            System.out.println(entry.getKey() + ":");
            patches = entry.getValue().getPatches();

            while(patches != null && patches.peek() != null){

                System.out.println("x2: ");
                for (int i = 0; i < patches.peek().xpoints.length ; i++) {

                    System.out.println("Printing: x= " + patches.peek().xpoints[i]);
                }
                System.out.println();

                System.out.println("y2: ");

                for (int i = 0; i < patches.peek().ypoints.length ; i++) {

                    System.out.println("Printing: y= " + patches.peek().ypoints[i]);
                }
//                //grph.drawPolygon((Polygon) (entry.getValue().getPatches().poll()));
                patches.poll();
////                //entry.getValue().getPatches().poll();
            }

        }
        

        for (Map.Entry<String, DummyTerritory> entry : gameMap.territories.entrySet()){

            System.out.println(entry.getValue().getPatches().peek().xpoints[0]);
        }

        for(Map.Entry<String, DummyTerritory> entry : gameMap.territories.entrySet()){

            System.out.println(entry.getKey() + ":");
            patches = entry.getValue().getPatches();

            while(patches.peek() != null){

                System.out.print("x: ");
                for (int i = 0; i < patches.peek().xpoints.length; i++) {

                    System.out.print(patches.peek().xpoints[i] + " ");
                }
                System.out.println();

                System.out.print("y: ");

                for (int i = 0; i < patches.peek().ypoints.length; i++) {

                    System.out.print(patches.peek().ypoints[i] + " ");
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
        paintComponent(grph);
        grph.drawPolygon(new Polygon(new int[]{200, 400, 400, 200, 370}, new int[]{200, 200, 400, 400, 432}, 5));
        grph.drawPolygon(new Polygon(new int[]{200, 400, 200, 370, 120}, new int[]{200, 200, 400, 432, 720}, 5));

        for(Map.Entry<String, DummyTerritory> entry : territories.entrySet()){

            while(entry.getValue().getPatches().peek() != null){

                for (int i = 0; i < ((Polygon)(entry.getValue().getPatches().peek())).xpoints.length ; i++) {

                    System.out.println("Printing in paint(): x= " + ((Polygon)(entry.getValue().getPatches().peek())).xpoints[i]);
                }

                for (int i = 0; i < ((Polygon)(entry.getValue().getPatches().peek())).ypoints.length ; i++) {

                    System.out.println("Printing in paint(): y= " + ((Polygon)(entry.getValue().getPatches().peek())).ypoints[i]);
                }
                grph.drawPolygon((Polygon) (entry.getValue().getPatches().poll()));
//                //entry.getValue().getPatches().poll();
            }

        }

//        while (patches != null && patches.peek() != null){
//
//            grph.drawPolygon(patches.poll().getBoarders());
//        }

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

                    territories.put(territory, new DummyTerritory(territory,
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
    }

    private void neverCalledJustStorage(){

    }
}

