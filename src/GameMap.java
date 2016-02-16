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
    public boolean loadingFinished = false;

    private JLabel labelPhase = new JLabel("",SwingConstants.CENTER);
    private JLabel labelInstr = new JLabel("",SwingConstants.CENTER);


    public GameMap(String path) {

        initMainMapFrame();
        initMainMapPanel();

        createMap(readMapFile(path));

        initCapital();
//        claimPhase(path);
//        normalRound();

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
            if (line.startsWith("continent")){
                createContinent(line);
            }
        }

        loadingFinished = true;
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

        while (line.indexOf('-') > 0) {

            territories.get(territory).setNeighbor(line.substring(0, line.indexOf('-') - 1));
            territories.get(line.substring(0, line.indexOf('-') - 1)).setNeighbor(territory);
            line = line.substring(line.indexOf('-') + 2);

        }

        territories.get(territory).setNeighbor(line);
        territories.get(line).setNeighbor(territory);
    }

    private void createContinent(String line){
        line = line.replace("continent ", "");

        String name = line.substring(0,line.indexOf(':')-3);
//        System.out.println(name);
        int reinforcementBonus = Integer.parseInt(line.substring(name.length()+1,name.length()+2));
//        System.out.println(reinforcementBonus);
        line = line.substring(name.length()+5);
//        System.out.println(line);

        LinkedList<Territory> members = new LinkedList<>();         //zu "members" umbenannt, damit keine verwechslung mit territories aufkommt

        while (line.indexOf('-') > 0) {
            members.add(territories.get(line.substring(0, line.indexOf('-') - 1)));
            line = line.substring(line.indexOf('-') + 2);
        }
        members.add(territories.get(line));
//        System.out.println(members);

        continents.put(name,new Continent(reinforcementBonus,members));
//        System.out.println(continents.keySet());
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

//        Border border = BorderFactory.createLineBorder(Color.BLACK); //for showing the position of the Label

        for (Map.Entry<String, Territory> entry : territories.entrySet()) {

            String from = entry.getKey();

            int x = entry.getValue().getCapitalLocation().x;
            int y = entry.getValue().getCapitalLocation().y;



            entry.getValue().labelCapital = new JLabel(Integer.toString(entry.getValue().getArmies()));


            entry.getValue().labelCapital.setHorizontalAlignment(SwingConstants.CENTER);
            entry.getValue().labelCapital.setVerticalAlignment(SwingConstants.CENTER);

            entry.getValue().labelCapital.setFont(new Font("Arial", Font.BOLD, 14));
            entry.getValue().labelCapital.setSize(20, 20);
            entry.getValue().labelCapital.setLocation(x - entry.getValue().labelCapital.getWidth() / 2,
                    y - entry.getValue().labelCapital.getHeight() / 2);
            entry.getValue().labelCapital.setForeground(Color.BLACK);

            mainMapPanel.add(entry.getValue().labelCapital);



//            entry.getValue().capital = new JLabel(Integer.toString(entry.getValue().getArmies()));
//
//
//            entry.getValue().capital.setHorizontalAlignment(SwingConstants.CENTER);
//            entry.getValue().capital.setVerticalAlignment(SwingConstants.CENTER);
//
//            entry.getValue().capital.setFont(new Font("Arial", Font.BOLD, 14));
//            entry.getValue().capital.setSize(20, 20);
//            entry.getValue().capital.setLocation(x - entry.getValue().capital.getWidth() / 2,
//                    y - entry.getValue().capital.getHeight() / 2);
//            entry.getValue().capital.setForeground(Color.BLACK);

//            entry.getValue().labelCapital.setBorder(border);                  //shows the position of the Label

        }

        mainMapFrame.repaint();
    }

    public void initButton(){

        JButton b = new JButton("end this round");
        mainMapPanel.add(b);

        b.setSize(150,30);
        b.setLocation(1080,600);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(new Color(0,0,102));
        b.setForeground(Color.WHITE);

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"You have ended the round.");
                //TODO
            }
        });

    }

    public void initTextField(String phase, String instruction){

        mainMapPanel.add(labelPhase);
        mainMapPanel.add(labelInstr);

        labelPhase.setText(phase);
        labelPhase.setFont(new Font("Arial", Font.BOLD, 20));
        labelPhase.setSize(500, 30);
        labelPhase.setLocation(400,570);
        labelPhase.setForeground(Color.BLACK);

        labelInstr.setText(instruction);
        labelInstr.setFont(new Font("Arial", Font.PLAIN, 17));
        labelInstr.setSize(700,30);
        labelInstr.setLocation(300,600);
        labelInstr.setForeground(Color.BLACK);
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
                if (loadingFinished) {

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2d.setColor(Color.WHITE);

                    for (Map.Entry<String, Territory> entry : territories.entrySet()) {      //Zeichnet linien zwischen den Capitals der Nachbarn

                        String from = entry.getKey();                                       //Von...
                        int fromX = entry.getValue().getCapitalLocation().x;
                        int fromY = entry.getValue().getCapitalLocation().y;

                        for (int i = 0; i < entry.getValue().getNeighbors().size(); i++) {

                            String to = entry.getValue().getNeighbors().get(i);                              //Nach...
                            int toX = territories.get(to).getCapitalLocation().x;
                            int toY = territories.get(to).getCapitalLocation().y;


                            if (from.equals("Alaska") && to.equals("Kamchatka")) {       //Außnahme behandeln
                                g2d.drawLine(fromX, fromY, 0, fromY);
                                g2d.drawLine(toX, toY, 1250, toY);
                            }
                            else {
                                if (from.equals("Kamchatka") && to.equals("Alaska")) {
                                    g2d.drawLine(fromX,fromY,1250,fromY);
                                    g2d.drawLine(toX,toY,0,toY);
                                }
                                else{
                                    g2d.drawLine(fromX, fromY, toX, toY);
                                }
                            }
                        }

                    }


                    for (Map.Entry<String, Territory> entry : territories.entrySet()) {

                        for (Polygon p : entry.getValue().getPatches()) {

                            if (entry.getValue().getOccupied() == -1) {
                                g2d.setColor(Color.LIGHT_GRAY);
                                g2d.fillPolygon(p);
                            }

                            if (entry.getValue().getOccupied() == 0){
                                g2d.setColor(new Color(194,0,0));
                                g2d.fillPolygon(p);
                            }
                            if (entry.getValue().getOccupied() == 1){
                                g2d.setColor(new Color(10,180,30));
                                g2d.fillPolygon(p);
                            }
                            g2d.setColor(Color.BLACK);
                            g2d.drawPolygon(p);
                        }
                    }

                    mainMapPanel.repaint();

                } else {

                    System.out.println("Still loading map, please wait...");
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1250, 650);
            }

        };
        mainMapPanel.setLayout(null);

        MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {

                //super.mouseClicked(me);       //probably not needed, try to uncomment on strange mouse behaviour

                if(GameLogic.phase == 0){       //occupy Territories

                    for (Map.Entry<String, Territory> entry : territories.entrySet()) {

                        for (Polygon p : entry.getValue().getPatches()) {

                            if (p.contains(me.getPoint()) && entry.getValue().getOccupied() == -1) {

                                //System.out.println("Clicked polygon");  //debugging only
                                entry.getValue().setOccupied(GameLogic.move % GameLogic.playerCount);
                                entry.getValue().addReinforcement();
                                entry.getValue().labelCapital.setText("" + entry.getValue().getArmies());

                                GameLogic.occupiedTerritories++;
                                GameLogic.move++;
                                GameLogic.currentPlayer = GameLogic.move % GameLogic.playerCount;

                                if(territories.size() == GameLogic.occupiedTerritories){

                                    GameLogic.phase = 1;
                                }
                            }
                        }
                    }

                }

                if(GameLogic.phase == 1){

                    initTextField("Verstärkungsphase","Verteile deine Armeen");
                    initButton();
                }
            }
        };

        mainMapPanel.addMouseListener(ma);

        mainMapFrame.add(mainMapPanel);
        mainMapFrame.pack();
    }

    @Override
    public String toString(){

        String result = "";

        for (Map.Entry<String, Territory> entry : territories.entrySet()){

            result += "Territory <" +  entry.getKey() + ">\n     ";
            result += "capital:   [" + entry.getValue().getCapitalLocation().x + ", " +
                                    entry.getValue().getCapitalLocation().y + "]\n     ";
            result += "neighbors: " + entry.getValue().getNeighbors() +  "\n     ";
            result += "patches:   ";

            for (Polygon p : entry.getValue().getPatches()){

                result += "{ ";
                for (int i = 0; i < p.xpoints.length; i++) {

                    result += "[" + p.xpoints[i] + ";";
                    result +=       p.ypoints[i] + "], ";
                }
                result += " }" + "\n                ";
            }

            result += "\n";
        }

        return  result;
    }

    private void claimPhase(String path){
        createMap(readMapFile(path));
        initCapital();
        initTextField("Eroberungsphase:","Such dir ein Territorium aus.");
        while(checkClaimPhase()){}

    }

    private boolean checkClaimPhase(){
        int count = 0;


        for (Map.Entry<String, Territory> entry : territories.entrySet()) {
            if (entry.getValue().getOccupied() >= 0){
                count++;
            }
            if (count == 42){
                return false;
            }
        }
        return true;
    }

    private void normalRound(){
        initTextField("Verstärkungsphase","Verteile deine Armeen");
        initButton();
    }
}