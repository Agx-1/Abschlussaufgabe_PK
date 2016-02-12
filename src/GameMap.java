import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;



/**
 * Created by fabian on 15.01.16.
 */

public class GameMap {

    private Map<String, Territory> territories = new HashMap<String, Territory>();
    private Map<String, Continent> continents = new HashMap<>();

    private JFrame mainMap;


    public GameMap(String path) {

        mainMap = new JFrame();

        mainMap.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainMap.setSize(1250, 650);
        mainMap.setBackground(new Color(50, 60, 250));
        mainMap.setResizable(false);
        mainMap.setVisible(true);
        mainMap.setTitle("All Those Territories");


        JPanel p = new JPanel() {


            @Override
            protected void paintComponent(Graphics g) {

                //super.paintComponent(g);      //maybe needed if some Component is ONLY added to JFrame


                g.setColor(Color.WHITE);


                for (Map.Entry<String, Territory> entry : territories.entrySet()) {      //Zeichnet linien zwischen den Capitals der Nachbarn

                    String from = entry.getKey();                                       //Von...
                    int fromX = (int) entry.getValue().capital.getLocation().x;
                    int fromY = (int) entry.getValue().capital.getLocation().y;

                    for (Map.Entry<String, Territory> subEntry : territories.entrySet()) {
                        if (territories.get(from).hasNeighbor(subEntry.getKey())) {

                            String to = subEntry.getKey();                              //Nach...
                            int toX = (int) subEntry.getValue().capital.getLocation().x;
                            int toY = (int) subEntry.getValue().capital.getLocation().y;

                            if (from.equals("Alaska") && to.equals("Kamchatka")) {       //Außnahme behandeln
                                g.drawLine(fromX, fromY, 0, fromY);
                                g.drawLine(toX, toY, 1250, toY);
                            } else {
                                g.drawLine(fromX, fromY, toX, toY);
                            }
                        }
                    }

                }


                for (Map.Entry<String, Territory> entry : territories.entrySet()) {

                    for (Polygon p : entry.getValue().getPatches()) {

                        g.setColor(Color.LIGHT_GRAY);
                        g.fillPolygon(p);

                        g.setColor(Color.GREEN);
                        if (entry.getValue().occupied >= 0)
                            g.fillPolygon(p);
                        g.setColor(Color.BLACK);
                        g.drawPolygon(p);
                    }
                }


            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1250, 650);
            }


        };


        p.setLayout(null);

        for (Map.Entry<String, Territory> entry : territories.entrySet()) {      //Soll vorerst bei jedem Capital "1" anzeigen
                                                                                 //schleife wird nicht betreten
            String from = entry.getKey();
            int x = (int) entry.getValue().capital.getLocation().x;
            int y = (int) entry.getValue().capital.getLocation().y;

            JLabel label = new JLabel("1");
            p.add(label);
            label.setLocation(x,y);
            label.setFont(new Font("Arial", Font.BOLD, 15));
            label.setSize(20, 20);

        }


        for (int i = 0; i <= 5; i++) {                                            //wird normal ausgeführt

            JLabel label = new JLabel("1");
            p.add(label);
            label.setLocation(i * 100, i * 100);
            //label.setBounds(new Rectangle(new Point(i*100,i*100),label.getPreferredSize()));
            label.setFont(new Font("Arial", Font.BOLD, 15));
            label.setSize(20, 20);

        }

            MouseAdapter ma = new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent me) {

                    //super.mouseClicked(me);       //probably not needed, try to uncomment on strange mouse behaviour

                    for (Map.Entry<String, Territory> entry : territories.entrySet()) {

                        for (Polygon p : entry.getValue().getPatches()) {

                            if (p.contains(me.getPoint())) {

                                //System.out.println("Clicked polygon");  //debugging only
                                entry.getValue().setOccupied(0);
                            }
                        }
                    }


                    mainMap.repaint();
                }
            };


            p.addMouseListener(ma);


            mainMap.add(p);
            mainMap.pack();


            createMap(readMapFile(path));

    }

    public void createMap(LinkedList<String> mapData) {

//        just for debugging
        for (String line : mapData){
            System.out.println(line);
        }
//        ------------------

        for (String line : mapData){

            if(line.startsWith("patch-of")){

                createPatch(line);
            }

            if (line.startsWith("capital-of")) {

                createCapital(line);
            }
            if (line.startsWith("neighbors-of")){

                createNeighbors(line);
            }
        }

        mainMap.repaint();
    }

    private void createPatch(String line){

        //saves the name of the territory in current line
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
                coordY[j] = Integer.parseInt(helperCoordinates[2*j+1]);
            }
            catch (NumberFormatException nfe) {}
        }

        //either create a new entry in the territories Map or add patch to existing VoidTerritory
        if(territories.containsKey(territory)){

            territories.get(territory).addPatch(new Polygon(coordX, coordY, coordX.length));

        } else{

            territories.put(territory, new Territory(territory,
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

            territories.put(territory, new Territory(territory, capitalCoordinates));
        }


    }

    private void createNeighbors(String line){

        line = line.replace("neighbors-of ", "");

        String territory = line.substring(0,line.indexOf(':')-1);    //Name des Terretoriums
        line = line.substring(line.indexOf(':') + 2);               //Name und Doppelpunkt wegstreichen

        if (line.indexOf('-')>0) {

            do {
                territories.get(territory).setNeighbor(line.substring(0, line.indexOf('-') - 1));
                line = line.substring(line.indexOf('-') + 2);

            } while (line.indexOf('-') > 0);
            territories.get(territory).setNeighbor(line);
        }
        else{

            territories.get(territory).setNeighbor(line);
        }

    }

    private void createContinent(String line){


    }

    public Map<String, Continent> getContinents(){

        return continents;
    }

    public void setText(String s){                                               //unfinished
        for (Map.Entry<String, Territory> entry : territories.entrySet()){

            /*
            String from = entry.getKey();

            int x = (int)entry.getValue().capital.getLocation().x;
            int y = (int)entry.getValue().capital.getLocation().y;
            System.out.println("x = " + x + ", y = " + y);

            JLabel label = new JLabel("1");
            p.add(label);
            label.setBounds(new Rectangle(new Point(74-10,72-10),label.getPreferredSize()));
            label.setFont(new Font("Arial",Font.BOLD,15));
            label.setSize(20,20);
             */

            //System.out.println((String)entry.getKey());
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

            s.close();
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

        for (Map.Entry<String, Territory> entry : territories.entrySet()){

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

