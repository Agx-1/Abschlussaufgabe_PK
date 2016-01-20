import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by fabian on 15.01.16.
 */

public class Map extends JFrame{

    Image img;
    Graphics grph;
    LinkedList<Territory> territories = new LinkedList<>();
    int i;

    public static void main(String[] args) {

        Map map = new Map();
        createMap(map);

    }

    public Map(){
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1250,650);
        this.setResizable(true);
        this.setVisible(true);
        i = 0;
    }

    public void paint(Graphics g){

        img = createImage(1250,650);
        grph = img.getGraphics();
        paintComponent(grph);
        g.drawImage(img, 0, 0, this);

    }

    public void paintComponent(Graphics g){


        g.setColor(new Color(0,0,150));
        g.fillRect(0, 0, 1250,650);


        repaint();
    }

    public String readMapFile(String path){

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

    public static void createMap(Map map) {

        String[] mapData = map.readMapFile("maps/world.map")
                .split(System.getProperty("line.separator"));

        String line;
        String territory;

        String[] helperCoordinates;
        int[] coordinates;

        Queue<Patch> patches = new LinkedList<Patch>();

        for (int i = 0; i < mapData.length; i++) {

            if (mapData[i].startsWith("patch-of")) {

                //remove patch-of
                line = mapData[i].replace("patch-of ", "");

                //remove numbers
                territory = line.replaceAll("( [0-9]+)+", "");

                System.out.println(territory);

                //remove territory
                line = mapData[i].replace("patch-of " + territory, "");
                helperCoordinates = line.split(" ");

                coordinates = new int[helperCoordinates.length];

                for (int j = 0; j < helperCoordinates.length; j++) {

                    try{
                        coordinates[j] = Integer.parseInt(helperCoordinates[j]);
                    }
                    catch (NumberFormatException nfe) {}
                }

                patches.offer(new Patch(territory, coordinates));
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

}

