import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class GameMap {

    private Map<String, Territory> territories = new HashMap<>();
    private LinkedList<Continent> continents = new LinkedList<>();
    private AI computerOpponent = new AI();

    private int reinforcements = 0;
    private Territory origin;       //origin of attack or reinforcement-distribution
    private boolean armyMoved = false;
    private int winner = -1;
    private Territory moveFrom;
    private Territory moveTo;

    private JFrame mainMapFrame;
    private JPanel mainMapPanel;

    public boolean loadingFinished = false;

    private JLabel labelPhase          = new JLabel("",SwingConstants.CENTER);
    private JLabel labelInstr          = new JLabel("",SwingConstants.CENTER);
    private JLabel labelReinforcements = new JLabel("",SwingConstants.CENTER);
    private JLabel labelCounter        = new JLabel("",SwingConstants.LEFT);
    private JLabel labelPlayer         = new JLabel("",SwingConstants.LEFT);
    private JLabel horribleHackToRemove = new JLabel();                 //fix-up LayoutManager instead

    private JButton b = new JButton("end this round");


    public GameMap(String path) {

        initMainMapFrame();
        initMainMapPanel();

        createMap(readMapFile(path));

        initCapital();
        initTextField("Eroberungsphase:", "Such dir ein Territorium aus.");
        initPlayerField();
        initCounterField();
        initReinforcementsField();

        //mainMapFrame.add(horribleHackToRemove);
    }

    private class AI {

        void makeMove(){

            switch (GameLogic.phase){

                case -1:
                    claimPhase(claimTerritory());
                    break;
                case 0:
                    while (reinforcements > 0){
                        distributionPhase(distributeReinforcements());
                    }
                    break;
                case 1:
                    while (hasTerritoryToAttack()) {
                        attackMovePhase(getAttackTarget());
                    }
                    origin = null;
                    moveFrom = null;
                    moveTo = null;
                    armyMoved = false;

                    nextPhase();
                    break;
            }
        }


        /**
         * @param statusOccupied find territories with 'occupied = statusOccupied'
         * @param complement iff true, find ONLY territories with 'occupied != statusOccupied'
         * @return
         */
        LinkedList<Territory> findByOccupied(int statusOccupied, boolean complement){

            LinkedList<Territory> result = new LinkedList<>();

            for (Map.Entry<String, Territory> entry : GameMap.this.territories.entrySet()){

                    if(entry.getValue().getOccupied() == statusOccupied ^ complement){

                        result.add(entry.getValue());
                    }
            }

            return result;
        }

        Territory claimTerritory(){

//            boolean unclaimed;
//            int friendlyNeighbors;          //'friendly' means either unclaimed or own territory
//            LinkedList<Continent> unclaimedContinents = new LinkedList<>();
//
//            for (Continent continent : continents){
//
//                unclaimed = true;
//
//                for(Territory member : continent.getMembers()){
//
//                    if(member.getOccupied() != -1){
//
//                        unclaimed = false;
//                    }
//                }
//
//                if(unclaimed){
//
//                    unclaimedContinents.add(continent);
//                }
//            }
//
//            int continentChoice = (int)(Math.random()*unclaimedContinents.size())%unclaimedContinents.size();
//
//            for (Territory territory : unclaimedContinents.get(continentChoice).getMembers()){
//
//
//            }

            LinkedList<Territory> unclaimedTerritories = findByOccupied(-1, false);
            int choice = (int)(Math.random()*unclaimedTerritories.size())%unclaimedTerritories.size();
            return unclaimedTerritories.get(choice);
        }

        Territory distributeReinforcements(){

//            int choice = (int)(Math.random()*ownTerritories.size())%ownTerritories.size();
//            return ownTerritories.get(choice);

            //positive value: computer has 'deficit' armies more than possible attackers
            //negative value; computer has 'deficit' armies less than possible attackers
            int deficit;

            int maxDeficit = Integer.MAX_VALUE;

            Territory result = null;

                for(Territory territory : findByOccupied(0, false)){

                    deficit = territory.getArmies();

                    for(Territory neighbor : territory.getNeighbors()){

                        if(neighbor.getOccupied() != 0){

                            deficit -= neighbor.getArmies()-1;
                        }
                    }

                    if(deficit < maxDeficit){

                        maxDeficit = deficit;
                        result = territory;
                    }
            }

            return result;
        }

        Territory getAttackTarget() {

            Territory result = null;

            int deficit;
            int minDeficit = Integer.MIN_VALUE;       //positive: more attacking armies, negative: more defending armies

            for (Territory territory : findByOccupied(0, false)) {
                for (Territory neighbor : territory.getNeighbors()) {

                    if (neighbor.getOccupied() != 0) {

                        deficit = territory.getArmies() - 1;
                        deficit -= neighbor.getArmies();

                        System.out.println("minDeficit: " + minDeficit);
                        if (deficit > minDeficit) {

                            minDeficit = deficit;
                            origin = territory;
                            System.out.println("origin(AI): " + origin.getName() + ": " + origin.getArmies());
                            result = neighbor;
                            System.out.println("result(AI): " + result.getName() + ": " + result.getArmies());
                            System.out.println("new minDeficit: " + minDeficit);
                        }
                    }
                }

            }

            System.out.println("minDeficit after for-loop: " + minDeficit);
            if (minDeficit < 0) {
                System.out.println("returning null...");
                return null;
            } else {
                System.out.println("returning " + result.getName() + " with origin " + origin.getName());
                return result;
            }
        }

//        Territory getAttackOrigin(){
//
//            Territory result = null;
//
//            //positive value: computer has 'minDeficit' armies more than possible attackers
//            //negative value; computer has 'minDeficit' armies less than possible attackers
//            int minDeficit;
//
//            int minDeficit = Integer.MIN_VALUE;
//
//            for(Territory territory : findByOccupied(0, false)){
//
//                for(Territory neighbor : territory.getNeighbors()){
//
//                    if(neighbor.getOccupied() != 0){
//
//                        minDeficit = territory.getArmies()-1;
//                        minDeficit -= neighbor.getArmies();
//
//                        System.out.println("minDeficit: " + minDeficit);
//                        if(minDeficit > minDeficit){
//
//                            minDeficit = minDeficit;
//
//                            //System.out.println("origin(AI): " + origin.getName() + ": " + origin.getArmies());
//                            result = territory;
//                            System.out.println("result(AI): " + result.getName() + ": " + result.getArmies());
//                            System.out.println("minDeficit: " + minDeficit);
//                            System.out.println("new minDeficit: " + minDeficit);
//                        }
//                    }
//                }
//            }
//
//            System.out.println("minDeficit after for-loop: " + minDeficit);
//            if(minDeficit < 0){
//                System.out.println("returning null...");
//                return null;
//            } else {
//                return result;
//            }
//        }

        boolean hasTerritoryToAttack(){

            //positive value: computer has 'deficit' armies more than possible attackers
            //negative value; computer has 'deficit' armies less than possible attackers
            int deficit;

            int minDeficit = Integer.MIN_VALUE;

            for(Territory territory : findByOccupied(0, false)){

                for(Territory neighbor : territory.getNeighbors()){

                    if(neighbor.getOccupied() != 0){

                        deficit = territory.getArmies()-1;
                        deficit -= neighbor.getArmies();

                        if(deficit > minDeficit){

                            minDeficit = deficit;
                        }
                    }
                }
            }

            if(minDeficit < 0){
                return false;
            } else {
                return true;
            }
        }
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
        String neighbor;

        line = line.substring(line.indexOf(':') + 2);               //Name und Doppelpunkt wegstreichen

        while (line.indexOf('-') > 0) {

            neighbor = line.substring(0, line.indexOf('-') - 1);
            territories.get(territory).setNeighbor(territories.get(neighbor));
            territories.get(neighbor).setNeighbor(territories.get(territory));

            line = line.substring(line.indexOf('-') + 2);

        }

        territories.get(territory).setNeighbor(territories.get(line));
        territories.get(line).setNeighbor(territories.get(territory));
    }

    private void createContinent(String line){
        line = line.replace("continent ", "");

        String name = line.substring(0, line.indexOf(':') - 3);
//        System.out.println(name);
        int reinforcementBonus = Integer.parseInt(line.substring(name.length()+1,name.length()+2));
//        System.out.println(bonus);
        line = line.substring(name.length()+5);
        System.out.println(line);

        LinkedList<Territory> members = new LinkedList<>();         //zu "members" umbenannt, damit keine verwechslung mit territories aufkommt

        while (line.indexOf('-') > 0) {
            members.add(territories.get(line.substring(0, line.indexOf('-') - 1)));
            line = line.substring(line.indexOf('-') + 2);
        }
        members.add(territories.get(line));
//        System.out.println(members);

        continents.add(new Continent(name, reinforcementBonus, members));
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

        }

        mainMapFrame.repaint();
    }

    public void initButton(){

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

                updateCounterField();

                origin = null;
                moveFrom = null;
                moveTo = null;

                armyMoved = false;

                if (GameLogic.currentPlayer == 0) {
                    nextPhase();
                } else {
                    nextPlayer();
                }
            }
        });

    }

    public void initTextField(String phase, String instruction){

        labelPhase = new JLabel("", SwingConstants.CENTER);
        labelInstr = new JLabel("", SwingConstants.CENTER);

        mainMapFrame.add(labelPhase);
        mainMapFrame.add(labelInstr);

        labelPhase.setText(phase);
        labelPhase.setFont(new Font("Arial", Font.BOLD, 20));
        labelPhase.setSize(500, 30);
        labelPhase.setLocation(400, 570);
        labelPhase.setForeground(Color.BLACK);

        labelInstr.setText(instruction);
        labelInstr.setFont(new Font("Arial", Font.PLAIN, 17));
        labelInstr.setSize(700, 30);
        labelInstr.setLocation(300, 600);
        labelInstr.setForeground(Color.BLACK);
    }

    public void updateTextField(String phase, String instruction){

        labelPhase.setText(phase);
        labelInstr.setText(instruction);

        mainMapFrame.repaint();
    }

    public void initReinforcementsField() {

        labelReinforcements = new JLabel("", SwingConstants.CENTER);
        labelReinforcements.setText("(Du hast noch " + Integer.toString(reinforcements) + " Verstärkungen.)");
        labelReinforcements.setFont(new Font("Courier New", Font.PLAIN, 15));
        labelReinforcements.setSize(700, 30);
        labelReinforcements.setLocation(300, 618);
        labelReinforcements.setForeground(new Color(50, 50, 50));
        mainMapFrame.add(labelReinforcements);
    }

    private void updateReinforcementsField(){

        labelReinforcements.setText("Du hast noch " + Integer.toString(reinforcements) + " Verstärkungen");
        mainMapFrame.repaint();
    }

    public void initPlayerField(){

        labelPlayer = new JLabel("", SwingConstants.CENTER);

        mainMapFrame.add(labelPlayer);

        labelPlayer.setText("Player: " + Integer.toString(GameLogic.currentPlayer));
        labelPlayer.setFont(new Font("Arial", Font.PLAIN, 10));
        labelPlayer.setSize(100, 20);
        labelPlayer.setLocation(10, 610);
        labelPlayer.setForeground(new Color(97, 91, 97));
    }

    public void updatePlayerField(){

        labelPlayer.setText("Player: " + GameLogic.currentPlayer);
        mainMapFrame.repaint();
    }

    public void initCounterField(){
        //mainMapPanel.add(labelCounter);

        labelCounter.setVisible(false);
        labelCounter.setText("Runde: " + Integer.toString(GameLogic.round));
        labelCounter.setFont(new Font("Arial", Font.PLAIN, 10));
        labelCounter.setSize(100,20);
        labelCounter.setLocation(10, 625);
        labelCounter.setForeground(new Color(97, 91, 97));
        mainMapFrame.add(labelCounter);
    }

    public void updateCounterField() {

        labelCounter.setText("Runde: " + Integer.toString(GameLogic.round));
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
        if (b){
            ans = "Gewonnen!";

        } else {
            ans = "Verloren :(";
        }

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

        this.b.setVisible(false);
        labelCounter.setVisible(false);
        labelPlayer.setVisible(false);
        labelReinforcements.setVisible(false);
        labelPhase.setVisible(false);
        labelInstr.setVisible(false);
        mainMapFrame.repaint();
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

                Territory selectedTerritory = null;

                findClickedTerritory:
                for (Map.Entry<String, Territory> entry : territories.entrySet()){

                    for(Polygon p : entry.getValue().getPatches()){

                        if(p.contains(me.getPoint())){

                            selectedTerritory = entry.getValue();
                            break findClickedTerritory;
                        }
                    }
                }

                if(SwingUtilities.isLeftMouseButton(me) && selectedTerritory != null){

                    System.out.println("Left Click");

                    if (moveTo != null && moveFrom != null) {
                        if(!(origin == moveFrom || origin == moveTo)){

                            armyMoved = true;
                        }
                    }

                    switch (GameLogic.phase){

                        case -1:
                            claimPhase(selectedTerritory);
                            break;
                        case 0:
                            distributionPhase(selectedTerritory);
                            break;
                        case 1:
                            attackMovePhase(selectedTerritory);
                            break;
                    }

                    if(selectedTerritory != GameLogic.getCurrentlyConquered()){

                        GameLogic.setCurrentlyConquered(null);
                    }
                }

                if(SwingUtilities.isRightMouseButton(me) && selectedTerritory != null){

                    System.out.println("Right Click");

                    if(GameLogic.phase == 1 &&
                            selectedTerritory.getOccupied() == GameLogic.currentPlayer &&
                            selectedTerritory.isNeighborOf(origin)){

                        if(selectedTerritory == GameLogic.getCurrentlyConquered()){

                            origin.moveArmyTo(selectedTerritory);

                        } else{

                            if(moveTo == null && moveFrom == null){

                                moveTo = selectedTerritory;
                                moveFrom = origin;
                            }

                            if(selectedTerritory == moveTo   && origin == moveFrom ||
                                    selectedTerritory == moveFrom && origin == moveTo){

                                if((moveFrom.isNeighborOf(moveTo) ||
                                        moveTo.isNeighborOf(moveFrom)) && !armyMoved){

                                    origin.moveArmyTo(selectedTerritory);
                                }
                            }
                        }

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


            //TODO: change to foreach
//            for (int i = 0; i < entry.getValue().getNeighbors().size(); i++) {
//
//                String to = entry.getValue().getNeighbors().get(i).getName();                              //Nach...
//                int toX = territories.get(to).getCapitalLocation().x;
//                int toY = territories.get(to).getCapitalLocation().y;
//
//
//                if (from.equals("Alaska") && to.equals("Kamchatka")) {       //Außnahme behandeln
//                    g2d.drawLine(fromX, fromY, 0, fromY);
//                    g2d.drawLine(toX, toY, 1250, toY);
//                }
//                else {
//                    if (from.equals("Kamchatka") && to.equals("Alaska")) {
//                        g2d.drawLine(fromX,fromY,1250,fromY);
//                        g2d.drawLine(toX,toY,0,toY);
//                    }
//                    else{
//                        g2d.drawLine(fromX, fromY, toX, toY);
//                    }
//                }
//            }

            for(Territory neighbor : entry.getValue().getNeighbors()){

                String to = neighbor.getName();
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

                switch (entry.getValue().getOccupied()){

                    case -1: g2d.setColor(Color.LIGHT_GRAY);
                        break;
                    case 0: g2d.setColor(new Color(194,0,0));
                        break;
                    case 1: g2d.setColor(new Color(10,180,30));
                        break;
                }
                g2d.fillPolygon(p);

                g2d.setColor(Color.BLACK);
                g2d.drawPolygon(p);
            }
        }


        if (origin != null){

            for(Polygon p : origin.getPatches()){

                switch (GameLogic.currentPlayer){
                    case 0: g2d.setColor(new Color(245, 44, 24));
                        break;
                    case 1: g2d.setColor(new Color(92, 221, 73));
                        break;
                }

                g2d.fillPolygon(p);
                g2d.setColor(Color.WHITE);
                g2d.drawPolygon(p);
            }

            for (Territory neighbor : origin.getNeighbors()){
                for(Polygon p : territories.get(neighbor.getName()).getPatches()){

                    switch (territories.get(neighbor.getName()).getOccupied()){

                        case 0: g2d.setColor(new Color(245, 44, 24));
                            break;
                        case 1: g2d.setColor(new Color(92, 221, 73));
                            break;
                    }

                    g2d.fillPolygon(p);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(p);
                }
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

            result += "     neighbors: " + "\n";
            for(Territory territory : entry.getValue().getNeighbors()){

                result += "                " + territory.getName() + "\n";
            }

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

        for (Continent continent : continents){

            result += "Continent <" + continent.getName() + ">\n";
            result += "     bonus:   " + continent.getBonus() + "\n";

            result += "     members: ";
            for (Territory member : continent.getMembers()){

                result += member.getName();
                result += "\n              ";
            }

            result += "\n";
        }

        return  result;
    }

    public void claimPhase(Territory selectedTerritory){

        if (selectedTerritory.getOccupied() == -1) {

            //System.out.println("Clicked polygon");  //debugging only
            selectedTerritory.setOccupied(GameLogic.currentPlayer);
            selectedTerritory.addReinforcement();
            selectedTerritory.labelCapital.setText("" + selectedTerritory.getArmies());

            GameLogic.occupiedTerritories++;

            if(territories.size() == GameLogic.occupiedTerritories){

                nextPhase();
                labelCounter.setVisible(true);
                initButton();

            } else{

                nextPlayer();
            }
        }
    }

    private void distributionPhase(Territory selectedTerritory){

        if(selectedTerritory.getOccupied() == GameLogic.currentPlayer){

            if(reinforcements > 0){

                selectedTerritory.addReinforcement();
                reinforcements--;
                System.out.println("remaining reinforcements: " + reinforcements);

                if(reinforcements == 0){

                    if(GameLogic.currentPlayer == 0){

                        nextPhase();

                    } else{

                        nextPlayer();
                    }
                }
            }
        }

        updateReinforcementsField();
    }

    private void attackMovePhase(Territory selectedTerritory){

        if(selectedTerritory.getOccupied() == GameLogic.currentPlayer){

            origin = selectedTerritory;
            System.out.println("origin set");

        } else{
            if(selectedTerritory.getName() != "" && origin != null)
                if(origin.isNeighborOf(selectedTerritory)){

                    System.out.println("attacking...");
                    GameLogic.attack(origin, selectedTerritory);
                }
        }

        System.out.println("origin: " + (origin == null ? "null" : origin.getName()));
        System.out.println("selectedTerritory: " + selectedTerritory.getName());
        System.out.println("currentPlayer: " + GameLogic.currentPlayer);

        mainMapPanel.repaint();


        for (int i = 0; i < GameLogic.playerCount; i++) {

            if(winner == -1) {
                winner = i;
            } else{
                break;
            }

            for(Map.Entry<String, Territory> entry : territories.entrySet()){

                if(entry.getValue().getOccupied() != i){

                    winner = -1;
                    break;
                }
            }
        }

        if(winner > -1){

            switch (winner){

                case 0:
                    initEndField(false);
                    break;
                case 1:
                    initEndField(true);
                    break;
            }
        }
    }

    public void calculateReinforcements(){

        System.out.println("current player: " + GameLogic.currentPlayer);

        for(Continent continent : continents){

            boolean continentBonus = true;

            for (Territory territory : continent.getMembers()){

                if(!(territories.get(territory.getName()).getOccupied() == GameLogic.currentPlayer)){

                    continentBonus = false;
                    break;
                }
            }

            if(continentBonus){

                reinforcements += continent.getBonus();
                System.out.println("Reinforcements for " + continent.getName() + ": " + continent.getBonus());
            } else{

                System.out.println("Reinforcements for " + continent.getName() + ": 0");
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

        updateReinforcementsField();
    }

    private void nextPlayer(){

        GameLogic.currentPlayer++;
        GameLogic.currentPlayer %= GameLogic.playerCount;

        if(GameLogic.phase == 0){

            calculateReinforcements();
        }

        updatePlayerField();

        if(GameLogic.currentPlayer == 0){

            computerOpponent.makeMove();
        }
    }

    private void nextPhase(){

        GameLogic.currentPlayer = 1;
        updatePlayerField();

        GameLogic.phase++;
        if(GameLogic.phase / 2 == 1){

            GameLogic.round++;
            updateCounterField();
        }
        GameLogic.phase %= 2;

        if(GameLogic.phase == 0){

            b.setVisible(false);
            calculateReinforcements();
            labelReinforcements.setVisible(true);
            updateTextField("Verstärkungsphase", "Verteile deine Armeen");


        }

        if(GameLogic.phase == 1){

            b.setVisible(true);
            updateTextField("Attacking", "Let's start rumbling");
            labelReinforcements.setVisible(false);
        }

    }

    public void generateLostMap(){

        int counter = 0;
        for (Map.Entry<String, Territory> entry : territories.entrySet()){

            if(counter == 0){
                entry.getValue().setOccupied(0);
                entry.getValue().addReinforcement();
            } else{
                entry.getValue().setOccupied(1);
                entry.getValue().addReinforcement();
                entry.getValue().addReinforcement();
                entry.getValue().addReinforcement();
                entry.getValue().addReinforcement();
                entry.getValue().addReinforcement();
            }

            counter++;
        }

        GameLogic.phase = 1;
        GameLogic.currentPlayer = 1;
        initButton();
    }
}