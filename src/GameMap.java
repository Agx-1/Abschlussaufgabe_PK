import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.LineBreakMeasurer;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class GameMap {

    private Map<String, Territory> territories = new HashMap<>();
    private Map<String, Continent> continents = new HashMap<>();

    private int reinforcements = 0;
    private Territory origin;       //origin of attack or reinforcement-distribution

    private JFrame mainMapFrame;
    private JPanel mainMapPanel;
    public boolean loadingFinished = false;

    private JLabel labelPhase = new JLabel("",SwingConstants.CENTER);
    private JLabel labelInstr = new JLabel("",SwingConstants.CENTER);
    private JLabel labelReinforcements = new JLabel("",SwingConstants.CENTER);
    private JLabel labelCounter = new JLabel("",SwingConstants.LEFT);
    private JLabel labelPlayer = new JLabel("",SwingConstants.LEFT);


    public GameMap(String path) {

        initMainMapFrame();
        initMainMapPanel();

        createMap(readMapFile(path));

        initCapital();
        initTextField("Eroberungsphase:", "Such dir ein Territorium aus.");
//        claimPhase(path);
//        normalRound();
        initCounterField();
        initReinforcementsField();
        initPlayerField();
    }

    public void createMap(LinkedList<String> mapData) {

//        just for debugging
        for (String line : mapData){
//            System.out.println(line);
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

        String name = line.substring(0, line.indexOf(':') - 3);
//        System.out.println(name);
        int reinforcementBonus = Integer.parseInt(line.substring(name.length()+1,name.length()+2));
//        System.out.println(reinforcementBonus);
        line = line.substring(name.length()+5);
        System.out.println(line);

        LinkedList<String> members = new LinkedList<>();         //zu "members" umbenannt, damit keine verwechslung mit territories aufkommt

        while (line.indexOf('-') > 0) {
            members.add(line.substring(0, line.indexOf('-') - 1));
            line = line.substring(line.indexOf('-') + 2);
        }
        members.add(line);
//        System.out.println(members);

        continents.put(name, new Continent(reinforcementBonus, members));
//        System.out.println(continents.keySet());
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
                //JOptionPane.showMessageDialog(null,"You have ended the round.");
                if( GameLogic.round % 2 == 0){      //TODO: is it modolo 2 or 4 ?
                    initCounterField();
                }
                GameLogic.nextPhase();
                initEndField(true);
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
        labelInstr.setLocation(300, 600);
        labelInstr.setForeground(Color.BLACK);
    }

    public void setTextField(String phase, String instruction){

        labelPhase.setText(phase);
        labelInstr.setText(instruction);

        mainMapFrame.repaint();
    }

    public void initCounterField(){
        mainMapPanel.add(labelCounter);

        labelCounter.setText("Runde: " + (GameLogic.round -1 ) );       //it will now start with round 0
        labelCounter.setFont(new Font("Arial", Font.PLAIN, 10));
        labelCounter.setSize(100,20);
        labelCounter.setLocation(10, 625);
        labelCounter.setForeground(new Color(97, 91, 97));
    }

    public void initReinforcementsField(){
        mainMapPanel.add(labelReinforcements);

        labelReinforcements.setText("(Du hast noch " + Integer.toString(reinforcements) + " Verstärkungen.)");
        labelReinforcements.setFont(new Font("Arial", Font.PLAIN, 15));
        labelReinforcements.setSize(700,30);
        labelReinforcements.setLocation(300,618);
        labelReinforcements.setForeground(new Color(50,50,50));
    }

    public void initPlayerField(){
        mainMapPanel.add(labelPlayer);

        labelPlayer.setText("Player: " + Integer.toString(GameLogic.currentPlayer));
        labelPlayer.setFont(new Font("Arial", Font.PLAIN, 10));
        labelPlayer.setSize(100, 20);
        labelPlayer.setLocation(10, 610);
        labelPlayer.setForeground(new Color(97, 91, 97));
    }

    public void initEndField(boolean b){                //wird vorerst aufgerufen beim ersten drücken des Buttons "end this round"
        JLabel labelEnd = new JLabel("",SwingConstants.CENTER);
        JLabel labelEndFrame = new JLabel("",SwingConstants.CENTER);
        mainMapPanel.add(labelEnd);
        mainMapPanel.add(labelEndFrame);
        Border border = LineBorder.createBlackLineBorder();

        for (Map.Entry<String, Territory> entry : territories.entrySet()) {     //delets all Capitals in the Range of the Frame
            int x = entry.getValue().getCapitalLocation().x;
            int y = entry.getValue().getCapitalLocation().y;
            if ( (x > 400 && x < 850) && (y > 200 && y < 450 ) ) {
                entry.getValue().labelCapital.setText("");
            }

        }

        String ans;
        if (b) ans = "Gewonnen!";
        else ans = "Verloren :(";

        labelEnd.setText(ans);
        labelEnd.setFont(new Font("Arial", Font.BOLD, 40));
        labelEnd.setSize(400,200);
        labelEnd.setLocation(425,225);
        labelEnd.setForeground(Color.BLACK);
        labelEnd.setBackground(new Color(255, 133, 8));
        labelEnd.setOpaque(true);
        labelEnd.setBorder(border);

        labelEndFrame.setSize(440,240);
        labelEndFrame.setLocation(405,205);
        labelEndFrame.setBackground(new Color(205, 86, 11));
        labelEndFrame.setOpaque(true);
        labelEndFrame.setBorder(border);

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

                    drawMap(g2d);
                    //else highlightNeighborsOf(selectedTerritory);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1250, 650);
            }

        };
        mainMapPanel.setLayout(null);

        initMouseAdapter();

        mainMapFrame.add(mainMapPanel);
        mainMapFrame.pack();
    }

    private void initMouseAdapter(){

        MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {

                //super.mouseClicked(me);       //probably not needed, try to uncomment on strange mouse behaviour

                String selectedTerritoryName = "";
                Territory selectedTerritory = null;

                findClickedTerritory:
                for (Map.Entry<String, Territory> entry : territories.entrySet()){

                    for(Polygon p : entry.getValue().getPatches()){

                        if(p.contains(me.getPoint())){

                            selectedTerritory = entry.getValue();
                            selectedTerritoryName = entry.getKey();
                            break findClickedTerritory;
                        }
                    }
                }

                if(SwingUtilities.isLeftMouseButton(me) && selectedTerritory != null){

                    System.out.println("LEFT KLICK");
                    switch (GameLogic.phase){

                        case -1:
                            claimPhase(selectedTerritory);
                            break;
                        case 0:
                            distributeReinforcements(selectedTerritory);
                            break;
                        case 1:
                            attackMovePhase(selectedTerritoryName, selectedTerritory);
                            break;
                    }
                }

                if(SwingUtilities.isRightMouseButton(me) && selectedTerritory != null){

                    System.out.println("RIGHT KLICK");
                    switch (GameLogic.phase){

                        case 1:
                            //TODO: attackieren
                            break;
                        default:
                            break;
                    }
                }
            }
        };

        mainMapPanel.addMouseListener(ma);
    }

    private void drawMap(Graphics2D g2d){
        for (Map.Entry<String, Territory> entry : territories.entrySet()) {      //Zeichnet linien zwischen den Capitals der Nachbarn
            g2d.setColor(Color.WHITE);
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


        if (origin != null){

            for (int i = 0; i < origin.getNeighbors().size(); i++) {
                for( Polygon p : territories.get(origin.getNeighbors().get(i)).getPatches()){
                    if (territories.get(origin.getNeighbors().get(i)).getOccupied() == GameLogic.currentPlayer)
                        g2d.setColor(new Color(92, 221, 73));
                    else
                        g2d.setColor(new Color(255, 77, 47));
                    g2d.fillPolygon(p);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(p);
                }

            }

            for(Polygon p : origin.getPatches()){
                g2d.setColor(new Color(92, 221, 73));
                g2d.fillPolygon(p);
                g2d.setColor(Color.WHITE);
                g2d.drawPolygon(p);
            }
        }

        mainMapPanel.repaint();
    }

    @Override
    public String toString(){

        String result = "";

        for (Map.Entry<String, Territory> entry : territories.entrySet()){

            result += "Territory <" +  entry.getKey() + ">\n";
            result += "     capital:   [" + entry.getValue().getCapitalLocation().x + ", " +
                                    entry.getValue().getCapitalLocation().y + "]\n";
            result += "     neighbors: " + entry.getValue().getNeighbors() +  "\n";
            result += "     patches:   ";

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

        for (Map.Entry<String, Continent> entry : continents.entrySet()){

            result += "Continent <" + entry.getKey() + ">\n";
            result += "     bonus:   " + entry.getValue().reinforcementBonus + "\n";

            result += "     members: ";
            for (String member : entry.getValue().members){

                result += member;
                result += "\n              ";
            }

            result += "\n";
        }

        return  result;
    }

//    private boolean checkClaimPhase(){
//        int count = 0;
//
//
//        for (Map.Entry<String, Territory> entry : territories.entrySet()) {
//            if (entry.getValue().getOccupied() >= 0){
//                count++;
//            }
//            if (count == 42){
//                return false;
//            }
//        }
//        return true;
//    }

    private void attackMovePhase(String selectedTerritoryName, Territory selectedTerritory){

        if(selectedTerritory.getOccupied() == GameLogic.currentPlayer){

            origin = selectedTerritory;

        } else{

            if(selectedTerritoryName != "" && selectedTerritory != null && origin != null)
            if(origin.isNeighborOf(selectedTerritoryName)){

                GameLogic.attack(origin, selectedTerritory);
            }
        }
    }

    private void distributeReinforcements(Territory selectedTerritory){

        if(selectedTerritory.getOccupied() == GameLogic.currentPlayer){

            if(reinforcements > 0){

                selectedTerritory.addReinforcement();
                reinforcements--;
                System.out.println("remaining reinforcements: " + reinforcements);

                if(reinforcements == 0){

                    if(GameLogic.currentPlayer == GameLogic.playerCount - 1){

                        setTextField("Attacking", "Let's start rumbling");
                        GameLogic.phase++;

                    } else{

                        nextPlayer();
                    }
                }
            }
        }
    }

    private void claimPhase(Territory selectedTerritory){

        if (selectedTerritory.getOccupied() == -1) {

            //System.out.println("Clicked polygon");  //debugging only
            selectedTerritory.setOccupied(GameLogic.currentPlayer);
            selectedTerritory.addReinforcement();
            selectedTerritory.labelCapital.setText("" + selectedTerritory.getArmies());

            GameLogic.occupiedTerritories++;
            nextPlayer();

            if(territories.size() == GameLogic.occupiedTerritories){

                GameLogic.nextPhase();
                setTextField("Verstärkungsphase","Verteile deine Armeen");
                initButton();

                calculateReinforcements();
            }
        }
    }

//    private void claimPhase(String path){
//        createMap(readMapFile(path));
//        initCapital();
//        initTextField("Eroberungsphase:","Such dir ein Territorium aus.");
//        while(checkClaimPhase()){}
//
//    }

    public void calculateReinforcements(){

        System.out.println("current player: " + GameLogic.currentPlayer);

        for(Map.Entry<String, Continent> entry : continents.entrySet()){

            boolean continentBonus = true;

            for (String territory : entry.getValue().members){

                if(!(territories.get(territory).getOccupied() == GameLogic.currentPlayer)){

                    continentBonus = false;
                    break;
                }
            }

            if(continentBonus){

                reinforcements += entry.getValue().reinforcementBonus;
                System.out.println("Reinforcements for " + entry.getKey() + ": " + entry.getValue().reinforcementBonus);
            } else{

                System.out.println("Reinforcements for " + entry.getKey() + ": 0");
            }
        }

        int occupiedTerritories = 0;

        for (Map.Entry<String, Territory> entry : territories.entrySet()){

            if(GameLogic.currentPlayer == entry.getValue().getOccupied()){

                occupiedTerritories++;
            }
        }

        System.out.println("reinforcements for territories: " + occupiedTerritories/3);
        reinforcements += occupiedTerritories/3;
        reinforcements = Math.max(reinforcements, 3);           //player gets at least 3 reinforcements
        System.out.println("reinforcements total: " + reinforcements);

    }

    private void newRound(){

        GameLogic.round++;
        calculateReinforcements();
    }

    private void nextPlayer(){

        GameLogic.currentPlayer++;
        GameLogic.currentPlayer %= GameLogic.playerCount;

        if(GameLogic.phase == 0){

            calculateReinforcements();
        }
        labelPlayer.setText("Player: " + Integer.toString(GameLogic.currentPlayer));
        mainMapFrame.repaint();
    }
}