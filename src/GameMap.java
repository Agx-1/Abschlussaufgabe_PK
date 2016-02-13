import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class GameMap {

    private Map<String, Territory> territories = new HashMap<String, Territory>();
    private Map<String, Continent> continents = new HashMap<>();

    private JFrame mainMapFrame;
    private JPanel mainMapPanel;
    private MouseAdapter ma;


    public GameMap(String path) {

        initMainMapFrame();
        initMainMapPanel();

        createMap(readMapFile(path));

        initCapital();
        initButton();
        initTextField("Alles mögliche kann hier geschrieben werden");
    }

    public void createMap(LinkedList<String> mapData) {

//        just for debugging
        for (String line : mapData){
            //System.out.println(line);
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

        mainMapFrame.repaint();
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

            territories.put(territory, new Territory(territory, new Polygon(coordX, coordY, coordX.length)));
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

        String territory = line.substring(0,line.indexOf(':')-1);   //Name des Terretoriums

        line = line.substring(line.indexOf(':') + 2);               //Name und Doppelpunkt wegstreichen

        if (line.indexOf('-')>0) {

            do {
                territories.get(territory).setNeighbor(line.substring(0, line.indexOf('-') - 1));
                territories.get(line.substring(0, line.indexOf('-') - 1)).setNeighbor(territory);
                line = line.substring(line.indexOf('-') + 2);

            } while (line.indexOf('-') > 0);
            territories.get(territory).setNeighbor(line);
            territories.get(line).setNeighbor(territory);
        }
        else{

            territories.get(territory).setNeighbor(line);
            territories.get(line).setNeighbor(territory);
        }

    }

    private void createContinent(String line){


    }

    public Map<String, Continent> getContinents(){

        return continents;
    }

    public void setText(String s){                                               //unfinished
        System.out.println("\n ANGEKOMMEN \n");
        for (Map.Entry<String, Territory> entry : territories.entrySet()){

            /*
            String from = entry.getKey();

            int x = (int)entry.getValue().capital.getLocation().x;
            int y = (int)entry.getValue().capital.getLocation().y;
            System.out.println("x = " + x + ", y = " + y);

            JLabel label = new JLabel("1");
            mainMapPanel.add(label);
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

    public void initCapital() {

        //Border border = BorderFactory.createLineBorder(Color.BLACK); //for showing the position of the Label

        for (Map.Entry<String, Territory> entry : territories.entrySet()) {      //Soll vorerst bei jedem Capital "1" anzeigen

             String from = entry.getKey();
            int x = entry.getValue().capital.getLocation().x;
            int y = entry.getValue().capital.getLocation().y;

            if (from.equals("Alaska")){                                          //Test
                entry.getValue().addReinforcement();
            }

            JLabel label = new JLabel(Integer.toString(entry.getValue().getArmies()));
            mainMapPanel.add(label);

            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);

            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setSize(20, 20);
            label.setLocation(x-label.getWidth()/2, y-label.getHeight()/2);
            label.setForeground(Color.BLACK);
            //label.setBorder(border);                  //shows the position of the Label

        }
    }

    public void initButton(){

        JButton b = new JButton("end this round");
        mainMapPanel.add(b);

        b.setSize(150,30);
        b.setLocation(1080,600);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(Color.LIGHT_GRAY);
        b.setForeground(Color.BLACK);

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"You have ended the round.");
                //TODO
            }
        });

    }

    public void initTextField(String s){
        JLabel textField = new JLabel(s);
        mainMapPanel.add(textField);

        textField.setFont(new Font("Arial", Font.PLAIN, 18));
        textField.setSize(500, 30);
        textField.setLocation(450,600);
        textField.setText(s);
        textField.setForeground(Color.BLACK);
    }

    private void initMainMapFrame(){

        mainMapFrame = new JFrame();

        mainMapFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainMapFrame.setSize(1250, 650);
        mainMapFrame.setBackground(new Color(0,153,204));
        mainMapFrame.setResizable(false);
        mainMapFrame.setVisible(true);
        mainMapFrame.setTitle("All Those Territories");
    }

    private void initMainMapPanel(){

        mainMapPanel = new JPanel() {

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
                            }

                            else {
                                if (from.equals("Kamchatka") && to.equals("Alaska")) {
                                    g.drawLine(fromX,fromY,1250,fromY);
                                    g.drawLine(toX,toY,0,toY);
                                }
                                else{
                                    g.drawLine(fromX, fromY, toX, toY);
                                }
                            }
                        }
                    }

                }


                for (Map.Entry<String, Territory> entry : territories.entrySet()) {

                    for (Polygon p : entry.getValue().getPatches()) {

                        g.setColor(Color.LIGHT_GRAY);
                        g.fillPolygon(p);

                        g.setColor(new Color(10,180,30));
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
        mainMapPanel.setLayout(null);

        initMouseAdapter();

        mainMapPanel.addMouseListener(ma);

        mainMapFrame.add(mainMapPanel);
        mainMapFrame.pack();
    }

    private void initMouseAdapter(){

        ma = new MouseAdapter() {

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

                mainMapFrame.repaint();
            }
        };
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

