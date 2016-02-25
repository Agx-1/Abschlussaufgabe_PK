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

//ActionListener interface is necessary to get selected file from dialogue
public class GameMap implements ActionListener {

    private Map<String, Territory> territories = new HashMap<>();
    private LinkedList<Continent> continents = new LinkedList<>();
    private Logic logic = new Logic();
    private AI computerOpponent;

    //reinforcements for current player
    private int reinforcements;

    private boolean armyMoved;      //true iff the army-movement in this move was made
    private int winner = -1;        //-1 until somebody wins
    private Territory origin;       //origin of attack or reinforcement-distribution

    //territories that are allowed to move armies to each other, as long as no other territory gets selected
    private Territory moveFrom;
    private Territory moveTo;

    private JFrame mainMapFrame;
    private JPanel mainMapPanel;
    private String path;

    //used to display if player won or lost
    private JLabel labelEnd = new JLabel("", SwingConstants.CENTER);
    private JLabel labelEndFrame = new JLabel("", SwingConstants.CENTER);

    //extended JFileChooser class to enable responding to a menu-click
    private ListeningJFileChooser fileChooser = new ListeningJFileChooser("maps/");

    private JMenuBar jMenuBar = new JMenuBar();
    private JMenu loadMenu = new JMenu("Load...");
    private JMenuItem loadItem = new JMenuItem("Load from file...");

    //prevent that map is only partially drawn
    private boolean loadingFinished = false;

    private JLabel labelPhase          = new JLabel("");
    private JLabel labelInstr          = new JLabel("");
    private JLabel labelReinforcements = new JLabel("");
    private JLabel labelRound          = new JLabel("");
    private JLabel labelPlayer         = new JLabel("");

    private JButton b = new JButton("end this round");


    public GameMap(String path) {

        this.path = path;
        initMembers();
        initMainMapFrame();
        initMainMapPanel();
        initMenu();

        loadMap(readMapFile(this.path));

        initCapital();
        initTextField("Claim Phase:", "Select a territory");
        initPlayerField();
        initRoundLabel();
        initButton();
        initReinforcementsField();

        mainMapFrame.setLayout(null);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        initMembers();
        path = fileChooser.getSelectedFile().getPath();

        restartGame();
    }

    private class ListeningJFileChooser extends JFileChooser implements ActionListener {

        public ListeningJFileChooser(String path) {

            super(path);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            fileChooser.showOpenDialog(mainMapFrame);
        }
    }

    private class AI {

        void makeMove() {

            switch (logic.phase) {

                case -1:
                    logic.claimPhase(claimTerritory());
                    break;
                case 0:
                    while (reinforcements > 0) {
                        logic.reinforcePhase(distributeReinforcements());
                    }
                    break;
                case 1:
                    while (hasTerritoryToAttack()) {
                        logic.attackMovePhase(getAttackTarget());
                    }
                    origin = null;
                    moveFrom = null;
                    moveTo = null;
                    armyMoved = false;

                    logic.nextPhase();
                    break;
            }
        }


        /**
         * @param statusOccupied find territories with 'occupied = statusOccupied'
         * @param complement     iff true, find ONLY territories with 'occupied != statusOccupied'
         *
         */
        LinkedList<Territory> findByOccupied(int statusOccupied, boolean complement) {

            LinkedList<Territory> result = new LinkedList<>();

            for (Map.Entry<String, Territory> entry : GameMap.this.territories.entrySet()) {

                if (entry.getValue().getOccupied() == statusOccupied ^ complement) {

                    result.add(entry.getValue());
                }
            }

            return result;
        }

        Territory claimTerritory() {

            Territory territoryChoice = null;

            if (continents.size() != 0) {

                Continent continentChoice = claimContinent(getClaimableContinents());

                if (continentChoice != null) {

                    territoryChoice = claimNearOwnTerritory(continentChoice);

                } else {

                    LinkedList<Continent> availableContinents = getAvailableContinents();

                    if (availableContinents.size() != 0) {

                        int choice = (int) (Math.random() * availableContinents.size()) % availableContinents.size();
                        continentChoice = availableContinents.get(choice);

                        territoryChoice = claimNearOwnTerritory(continentChoice);

                    } else {

                        System.out.println("Error: AI was called when all Continents were full");
                    }
                }
            }else {

                LinkedList<Territory> unclaimedTerritories = findByOccupied(-1, false);
                int choice = (int) (Math.random() * unclaimedTerritories.size()) % unclaimedTerritories.size();
                territoryChoice = unclaimedTerritories.get(choice);

            }

            return territoryChoice;
        }

        Territory distributeReinforcements() {

//            int choice = (int)(Math.random()*ownTerritories.size())%ownTerritories.size();
//            return ownTerritories.get(choice);

            //positive value: computer has 'deficit' armies more than possible attackers
            //negative value; computer has 'deficit' armies less than possible attackers
            int deficit;

            int maxDeficit = Integer.MAX_VALUE;

            Territory result = null;

            for (Territory territory : findByOccupied(0, false)) {

                deficit = territory.getArmies();

                for (Territory neighbor : territory.getNeighbors()) {

                    if (neighbor.getOccupied() != 0) {

                        deficit -= neighbor.getArmies() - 1;
                    }
                }

                if (deficit < maxDeficit) {

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

                        if (deficit > minDeficit) {

                            minDeficit = deficit;
                            origin = territory;
                            result = neighbor;
                        }
                    }
                }

            }

            if (minDeficit < 0) {
                return null;
            } else {
                return result;
            }
        }

        boolean hasTerritoryToAttack() {

            int deficit;        //positive: more attacking armies, negative: more defending armies

            int minDeficit = Integer.MIN_VALUE;

            for (Territory territory : findByOccupied(0, false)) {

                for (Territory neighbor : territory.getNeighbors()) {

                    if (neighbor.getOccupied() != 0) {

                        deficit = territory.getArmies() - 1;
                        deficit -= neighbor.getArmies();

                        if (deficit > minDeficit) {

                            minDeficit = deficit;
                        }
                    }
                }
            }

            return minDeficit >= 0;
        }

        LinkedList<Continent> getClaimableContinents(){

            boolean claimed;
            boolean full;
            LinkedList<Continent> claimableContinents = new LinkedList<>();

            //search continents for which bonus could still be acquired
            for (Continent continent : continents) {

                claimed = false;
                full = true;

                for (Territory member : continent.getMembers()) {

                    if (member.getOccupied() != -1 && member.getOccupied() != 0) {
                        claimed = true;
                    }

                    if(member.getOccupied() == -1){
                        full = false;
                    }
                }

                if (!claimed && !full) {

                    claimableContinents.add(continent);
                }
            }

            return claimableContinents;
        }

        LinkedList<Continent> getAvailableContinents(){

            LinkedList<Continent> result = new LinkedList<>();

            //find continents with at least one unclaimed territory
            for (Continent continent : continents) {
                for (Territory member : continent.getMembers()) {

                    if (member.getOccupied() == -1) {
                        result.add(continent);
                        break;
                    }
                }
            }

            return result;
        }

        Continent claimContinent(LinkedList<Continent> claimableContinents){

            Continent result = null;

            boolean preferred;                  //true iff one territory on this continent is already claimed by AI

            for (Continent claimableContinent : claimableContinents) {

                preferred = false;

                for (Territory member : claimableContinent.getMembers()) {

                    if (member.getOccupied() == 0) {
                        preferred = true;
                    }
                }

                //if AI has claimed one territory of a continent, continue claiming on that continent
                if (preferred) {

                    result = claimableContinent;
                    break;
                }
            }

            if(claimableContinents.size() != 0){
                if(result == null){
                    int choice = (int)(Math.random()*claimableContinents.size())%claimableContinents.size();
                    result = claimableContinents.get(choice);
                }
            }

            return result;
        }

        Territory claimNearOwnTerritory(Continent continentChoice){

            int maxOwnNeighbors = -1;
            int currentOwnNeighbors;

            Territory result = null;

            //try to claim territories near own claimed territories
            for (Territory member : continentChoice.getMembers()) {

                currentOwnNeighbors = 0;

                if (member.getOccupied() == -1) {
                    for (Territory neighbor : member.getNeighbors()) {

                        if (neighbor.getOccupied() == 0) {
                            currentOwnNeighbors++;
                        }
                    }

                    if (currentOwnNeighbors > maxOwnNeighbors) {
                        result = member;
                        maxOwnNeighbors = currentOwnNeighbors;
                    }
                }
            }

            return result;
        }
    }

    private class Logic {

        final int playerCount = 2;    //number of players participating in the logic
        int currentPlayer = 1;        //0 for computer, upcoming integers for human players
        int occupiedTerritories = 0;
        int round = 1;

        // -1 stands for claim phase, 0 for reinforce, 1 for attacking and moving
        int phase = -1;
        Occupyable currentlyConquered = null;

        //attacker has to have >1 armies in his VoidTerritory
        void attack(Occupyable attacker, Occupyable defender) {
            int[] attackerDices;
            int[] defenderDices;

            final int maxAttackers = 3;      //how many armies can attack at once
            final int maxDefenders = 2;      //how many armies can defend at once

            System.out.printf("Player %d attacks <%s> from <%s> \n",
                    logic.currentPlayer, defender.getName(), attacker.getName());

            if(attacker.getArmies() > maxAttackers){

                attackerDices = new int[maxAttackers];

                //if enough armies are present, send maximum into battle
                for (int i = 0; i < attackerDices.length; i++) {

                    attackerDices[i] = rollDice();
                }

            } else{

                //send all armies except one, therefore -1
                attackerDices = new int[attacker.getArmies() - 1];

                for (int i = 0; i < attackerDices.length; i++) {

                    attackerDices[i] = rollDice();
                }
            }

            if(defender.getArmies() >= maxDefenders){

                defenderDices = new int[maxDefenders];

                //if enough armies are present, send maximum of 2
                for (int i = 0; i < defenderDices.length; i++) {

                    defenderDices[i] = rollDice();
                }

            } else{

                defenderDices = new int[defender.getArmies()];

                for (int i = 0; i < defenderDices.length; i++) {

                    defenderDices[i] = rollDice();
                }
            }

            Arrays.sort(attackerDices);
            Arrays.sort(defenderDices);

            attackerDices = reverse(attackerDices);
            defenderDices = reverse(defenderDices);

            //just for testing, remove if finished
            System.out.print("Attacker Array: ");
            for (int attackerDice : attackerDices) {
                System.out.print(attackerDice + ", ");
            }

            System.out.println();
            System.out.print("Defender Array: ");
            for (int defenderDice : defenderDices) {

                System.out.print(defenderDice + ", ");
            }

            //taking the minimum assures the index staying in the array, even if there are more defenders than attackers
            for (int i = 0; i < Math.min(defenderDices.length, attackerDices.length); i++) {

                if (attackerDices[i] > defenderDices[i]) {

                    defender.removeArmy();
                    //System.out.println("One defender died.");

                } else {

                    attacker.removeArmy();
                    //System.out.println("One attacker died.");
                }

                if(defender.getArmies() == 0){

                    defender.occupy(currentPlayer, attackerDices.length);
                    setCurrentlyConquered(defender);

                    for (int attackerDice : attackerDices) {
                        attacker.removeArmy();
                    }

                    System.out.println();
                    System.out.println();
                    System.out.printf("<%s> occupied <%s> with %d armies.\n\n",
                            attacker.getName(), defender.getName(), attackerDices.length);
                }
            }

            if(defender.getOccupied() != attacker.getOccupied()){

//            System.out.printf("Armies defender: %2d\n", defender.getArmies());
//            System.out.printf("Armies attacker: %2d", attacker.getArmies());
                System.out.println();
                System.out.println();
            }
        }

        void setCurrentlyConquered(Occupyable territory){

            currentlyConquered = territory;
        }

        void claimPhase(Territory selectedTerritory) {

            if (selectedTerritory.getOccupied() == -1) {

                selectedTerritory.setOccupied(currentPlayer);
                selectedTerritory.addReinforcement();

                occupiedTerritories++;

                if (territories.size() == occupiedTerritories) {

                    nextPhase();
                    labelRound.setVisible(true);

                } else {

                    nextPlayer();
                }
            }
        }

        void reinforcePhase(Territory selectedTerritory) {

            if (selectedTerritory.getOccupied() == currentPlayer) {

                if (reinforcements > 0) {

                    selectedTerritory.addReinforcement();
                    reinforcements--;
                    //System.out.println("remaining reinforcements: " + reinforcements);

                    if (reinforcements == 0) {

                        if (currentPlayer == 0) {

                            nextPhase();

                        } else {

                            nextPlayer();
                        }
                    }
                }
            }

            updateReinforcementsLabel();
        }

        void attackMovePhase(Territory selectedTerritory) {

            if (selectedTerritory.getOccupied() == currentPlayer) {

                origin = selectedTerritory;

            } else {
                if (!Objects.equals(selectedTerritory.getName(), "") && origin != null)
                    if (origin.isNeighborOf(selectedTerritory) && origin.getArmies() > 1) {

                        attack(origin, selectedTerritory);
                    }
            }

            mainMapPanel.repaint();

            for (int i = 0; i < playerCount; i++) {

                if (winner == -1) {
                    winner = i;
                } else {
                    break;
                }

                for (Map.Entry<String, Territory> entry : territories.entrySet()) {

                    if (entry.getValue().getOccupied() != i) {

                        winner = -1;
                        break;
                    }
                }
            }

            if (winner > -1) {

                displayEndMessage(winner);
            }
        }

        void calculateReinforcements() {

            //System.out.println("Calculating reinforcements for player " + logic.currentPlayer);
            System.out.println("Reinforcements: ");
            for (Continent continent : continents) {

                boolean continentBonus = true;

                for (Territory territory : continent.getMembers()) {

                    if (!(territories.get(territory.getName()).getOccupied() == logic.currentPlayer)) {

                        continentBonus = false;
                        break;
                    }
                }

                if (continentBonus) {

                    reinforcements += continent.getBonus();
                    System.out.printf("     Bonus for %-15s: %2d \n", continent.getName(), continent.getBonus());
                }
            }

            int occupiedTerritories = 0;

            for (Map.Entry<String, Territory> entry : territories.entrySet()) {

                if (logic.currentPlayer == entry.getValue().getOccupied()) {

                    occupiedTerritories++;
                }
            }

            System.out.printf("     Bonus for %-2d %-12s: %2d\n", occupiedTerritories, "territories", occupiedTerritories / 3);
            reinforcements += occupiedTerritories / 3;
            reinforcements = Math.max(reinforcements, 3);           //player gets at least 3 reinforcements
            System.out.println("     -----------------------------");
            System.out.printf("     Reinforcements total     : %2d \n\n", reinforcements);

            updateReinforcementsLabel();
        }

        void nextPlayer() {

            logic.currentPlayer++;
            logic.currentPlayer %= logic.playerCount;

            if (logic.phase != -1) {
                System.out.println("Current Player: " + logic.currentPlayer);
                System.out.println("------------------");
            }

            if (logic.phase == 0) {

                calculateReinforcements();
            }

            updatePlayerLabel();

            if (logic.currentPlayer == 0) {

                computerOpponent.makeMove();
            }
        }

        void nextPhase() {

            logic.currentPlayer = 1;
            updatePlayerLabel();

            System.out.println("Current Player: " + logic.currentPlayer);
            System.out.println("------------------");

            logic.phase++;
            if (logic.phase / 2 == 1) {

                logic.round++;
                updateRoundLabel();
            }
            logic.phase %= 2;

            if (logic.phase == 0) {

                if (winner == -1) {
                    b.setVisible(false);
                }
                calculateReinforcements();
                labelReinforcements.setVisible(true);
                updateTextField("Reinforcement phase", "Distribute your reinforcements");
            }

            if (logic.phase == 1) {

                b.setVisible(true);
                updateTextField("Attacking phase", "Right click to move armies");
                labelReinforcements.setVisible(false);
            }

        }

        Occupyable getCurrentlyConquered(){

            return currentlyConquered;
        }

        int[] reverse(int[] array){

            int[] result = new int[array.length];

            for (int i = 0; i < array.length; i++) {

                result[i] = array[array.length - i - 1];
            }

            return result;
        }

        int rollDice(){

            return ((int)(Math.random()*6))%6 + 1;      //tested and approved

        }
    }

    private void loadMap(LinkedList<String> mapData) {

        for (String line : mapData) {

            if (line.startsWith("patch-of")) {
                loadPatch(line);
            }
            if (line.startsWith("capital-of")) {
                loadCapital(line);
            }
            if (line.startsWith("neighbors-of")) {
                loadNeighbors(line);
            }
            if (line.startsWith("continent")) {
                loadContinent(line);
            }
        }

        loadingFinished = true;
        mainMapFrame.repaint();
    }

    private void loadPatch(String line) {

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

        coordX = new int[helperCoordinates.length / 2];
        coordY = new int[helperCoordinates.length / 2];

        //fill the two arrays with corresponding coordinates
        for (int j = 0; j < coordX.length; j++) {

            try {
                coordX[j] = Integer.parseInt(helperCoordinates[2 * j]);
                coordY[j] = Integer.parseInt(helperCoordinates[2 * j + 1]);
            } catch (NumberFormatException ignored) {
            }
        }

        //either create a new entry in the territories Map or add patch to existing Territory
        if (territories.containsKey(territory)) {

            territories.get(territory).addPatch(new Polygon(coordX, coordY, coordX.length));

        } else {

            territories.put(territory, new Territory(territory, new Polygon(coordX, coordY, coordX.length)));
        }
    }

    private void loadCapital(String line) {

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
            } catch (NumberFormatException ignored) {
            }
        }

        if (territories.containsKey(territory)) {

            territories.get(territory).addCapital(capitalCoordinates);

        } else {

            territories.put(territory, new Territory(territory, capitalCoordinates));
        }


    }

    private void loadNeighbors(String line) {
        line = line.replace("neighbors-of ", "");

        String territory = line.substring(0, line.indexOf(':') - 1);   //name of territory
        String neighbor;

        line = line.substring(line.indexOf(':') + 2);               //delete name and colon

        while (line.indexOf('-') > 0) {

            neighbor = line.substring(0, line.indexOf('-') - 1);
            territories.get(territory).addNeighbor(territories.get(neighbor));
            territories.get(neighbor).addNeighbor(territories.get(territory));

            line = line.substring(line.indexOf('-') + 2);

        }

        territories.get(territory).addNeighbor(territories.get(line));
        territories.get(line).addNeighbor(territories.get(territory));
    }

    private void loadContinent(String line) {
        line = line.replace("continent ", "");

        String name = line.substring(0, line.indexOf(':') - 3);

        int reinforcementBonus = Integer.parseInt(line.substring(name.length() + 1, name.length() + 2));

        line = line.substring(name.length() + 5);

        LinkedList<Territory> members = new LinkedList<>();     //called 'members' to avoid confusion with 'territories'

        while (line.indexOf('-') > 0) {
            members.add(territories.get(line.substring(0, line.indexOf('-') - 1)));
            line = line.substring(line.indexOf('-') + 2);
        }
        members.add(territories.get(line));

        continents.add(new Continent(name, reinforcementBonus, members));
    }

    private LinkedList<String> readMapFile(String path) {

        LinkedList<String> result = new LinkedList<>();
        Scanner s;

        try {
            s = new Scanner(Paths.get(path));

            while (s.hasNextLine()) {

                result.add(s.nextLine());
            }

            s.close();
        } catch (IOException e) {
            System.out.println(".map file not found");
            return null;
        }

        return result;
    }

    private void drawMap(Graphics2D g2d) {

        for (Map.Entry<String, Territory> entry : territories.entrySet()) { //draws lines between capitals of neighbors
            g2d.setColor(Color.WHITE);
            String from = entry.getKey();
            int fromX = entry.getValue().getCapitalLocation().x;
            int fromY = entry.getValue().getCapitalLocation().y;

            for (Territory neighbor : entry.getValue().getNeighbors()) {

                String to = neighbor.getName();
                int toX = territories.get(to).getCapitalLocation().x;
                int toY = territories.get(to).getCapitalLocation().y;

                if (from.equals("Alaska") && to.equals("Kamchatka")) {       //special case
                    g2d.drawLine(fromX, fromY, 0, fromY);
                    g2d.drawLine(toX, toY, 1250, toY);
                } else {
                    if (from.equals("Kamchatka") && to.equals("Alaska")) {
                        g2d.drawLine(fromX, fromY, 1250, fromY);
                        g2d.drawLine(toX, toY, 0, toY);
                    } else {
                        g2d.drawLine(fromX, fromY, toX, toY);
                    }
                }
            }

        }

        for (Map.Entry<String, Territory> entry : territories.entrySet()) {

            for (Polygon p : entry.getValue().getPatches()) {

                switch (entry.getValue().getOccupied()) {

                    case -1:
                        g2d.setColor(Color.LIGHT_GRAY);
                        break;
                    case 0:
                        g2d.setColor(new Color(194, 0, 0));
                        break;
                    case 1:
                        g2d.setColor(new Color(10, 180, 30));
                        break;
                }
                g2d.fillPolygon(p);

                g2d.setColor(Color.BLACK);
                g2d.drawPolygon(p);
            }
        }


        if (origin != null) {

            for (Polygon p : origin.getPatches()) {

                switch (logic.currentPlayer) {
                    case 0:
                        g2d.setColor(new Color(245, 44, 24));
                        break;
                    case 1:
                        g2d.setColor(new Color(92, 221, 73));
                        break;
                }

                g2d.fillPolygon(p);
                g2d.setColor(Color.WHITE);
                g2d.drawPolygon(p);
            }

            for (Territory neighbor : origin.getNeighbors()) {
                for (Polygon p : territories.get(neighbor.getName()).getPatches()) {

                    switch (territories.get(neighbor.getName()).getOccupied()) {

                        case 0:
                            g2d.setColor(new Color(245, 44, 24));
                            break;
                        case 1:
                            g2d.setColor(new Color(92, 221, 73));
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

    private void initButton() {

        mainMapPanel.add(b);

        b.setSize(150, 30);
        b.setLocation(1080, 585);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(new Color(0, 0, 102));
        b.setForeground(Color.WHITE);
        b.setVisible(false);

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (winner == -1) {
                    updateRoundLabel();

                    origin = null;
                    moveFrom = null;
                    moveTo = null;

                    armyMoved = false;

                    if (logic.currentPlayer == 0) {
                        logic.nextPhase();
                    } else {
                        logic.nextPlayer();
                    }
                } else {
                    initMembers();
                    restartGame();
                }
            }
        });

    }

    private void initCapital() {

        for (Map.Entry<String, Territory> entry : territories.entrySet()) {

            mainMapPanel.add(entry.getValue().getCapital());
        }

        mainMapFrame.repaint();
    }

    private void initRoundLabel() {

        labelRound.setVisible(false);
        labelRound.setText("Round: " + Integer.toString(logic.round));
        labelRound.setFont(new Font("Arial", Font.PLAIN, 10));
        labelRound.setSize(100, 20);
        labelRound.setLocation(10, 605);
        labelRound.setForeground(new Color(97, 91, 97));
        mainMapPanel.add(labelRound);
    }

    private void initMainMapFrame() {

        mainMapFrame = new JFrame();

        mainMapFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainMapFrame.setSize(1250, 650);
        mainMapFrame.setResizable(false);
        mainMapFrame.setVisible(true);
        mainMapFrame.setTitle("All Those Territories");

    }

    private void initMainMapPanel() {

        mainMapPanel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                if (loadingFinished) {

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    drawMap(g2d);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1250, 650);
            }

        };

        mainMapPanel.setLayout(null);
        mainMapPanel.setBackground(new Color(0, 153, 204));

        initMouseAdapter();
        mainMapFrame.add(mainMapPanel);
        mainMapFrame.pack();
    }

    private void initMembers() {

        computerOpponent = new AI();

        reinforcements = 0;
        armyMoved = false;
        winner = -1;
        labelReinforcements.setVisible(false);

        origin = null;
        moveTo = null;
        moveFrom = null;

        logic.phase = -1;
        logic.currentPlayer = 1;
        logic.occupiedTerritories = 0;
        logic.round = 1;
        logic.setCurrentlyConquered(null);

        for (Map.Entry<String, Territory> entry : territories.entrySet()) {

            entry.getValue().clearCapital();
        }

        labelEnd.setVisible(false);
        labelEndFrame.setVisible(false);

        territories = new HashMap<>();
        continents = new LinkedList<>();

        b.setText("end this round");
    }

    private void initMenu(){

        //mainMapPanel.add(jMenuBar);
        mainMapFrame.setJMenuBar(jMenuBar);
        jMenuBar.add(loadMenu);
        loadMenu.add(loadItem);
        loadItem.addActionListener(fileChooser);
        fileChooser.addActionListener(this);
    }

    private void initMouseAdapter() {

        MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent me) {

                Territory selectedTerritory = null;

                findClickedTerritory:
                for (Map.Entry<String, Territory> entry : territories.entrySet()) {

                    for (Polygon p : entry.getValue().getPatches()) {

                        if (p.contains(me.getPoint())) {

                            selectedTerritory = entry.getValue();
                            break findClickedTerritory;
                        }
                    }
                }

                if (SwingUtilities.isLeftMouseButton(me) && selectedTerritory != null) {

                    if (moveTo != null && moveFrom != null) {
                        if (!(origin == moveFrom || origin == moveTo)) {

                            armyMoved = true;
                        }
                    }

                    switch (logic.phase) {
                        case -1:
                            logic.claimPhase(selectedTerritory);
                            break;
                        case 0:
                            logic.reinforcePhase(selectedTerritory);
                            break;
                        case 1:
                            logic.attackMovePhase(selectedTerritory);
                            break;
                    }

                    if (selectedTerritory != logic.getCurrentlyConquered()) {

                        logic.setCurrentlyConquered(null);
                    }
                }

                if (SwingUtilities.isRightMouseButton(me) && selectedTerritory != null) {

                    if (logic.phase == 1 &&
                            selectedTerritory.getOccupied() == logic.currentPlayer &&
                            selectedTerritory.isNeighborOf(origin)) {

                        if (selectedTerritory == logic.getCurrentlyConquered()) {

                            origin.moveArmyTo(selectedTerritory);

                        } else {

                            if (moveTo == null && moveFrom == null) {

                                moveTo = selectedTerritory;
                                moveFrom = origin;
                            }

                            if (selectedTerritory == moveTo && origin == moveFrom ||
                                    selectedTerritory == moveFrom && origin == moveTo) {

                                if ((moveFrom.isNeighborOf(moveTo) ||
                                        moveTo.isNeighborOf(moveFrom)) && !armyMoved) {

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

    private void initPlayerField() {

        labelPlayer = new JLabel("");

        mainMapPanel.add(labelPlayer);

        labelPlayer.setText("Player: " + Integer.toString(logic.currentPlayer));
        labelPlayer.setFont(new Font("Arial", Font.PLAIN, 10));
        labelPlayer.setSize(100, 20);
        labelPlayer.setLocation(10, 590);
        labelPlayer.setForeground(new Color(97, 91, 97));
    }

    private void initReinforcementsField() {

        labelReinforcements = new JLabel("", SwingConstants.CENTER);
        labelReinforcements.setText("(You have " + Integer.toString(reinforcements) + " reinforcements left.)");
        labelReinforcements.setFont(new Font("Arial", Font.PLAIN, 15));
        labelReinforcements.setSize(700, 30);
        labelReinforcements.setLocation(300, 603);
        labelReinforcements.setForeground(new Color(50, 50, 50));
        labelReinforcements.setVisible(false);
        mainMapPanel.add(labelReinforcements);
    }

    private void initTextField(String phase, String instruction) {

        labelPhase = new JLabel("", SwingConstants.CENTER);
        labelInstr = new JLabel("", SwingConstants.CENTER);

        mainMapPanel.add(labelPhase);
        mainMapPanel.add(labelInstr);

        labelPhase.setText(phase);
        labelPhase.setFont(new Font("Arial", Font.BOLD, 20));
        labelPhase.setSize(500, 30);
        labelPhase.setLocation(400, 555);
        labelPhase.setForeground(Color.BLACK);

        labelInstr.setText(instruction);
        labelInstr.setFont(new Font("Arial", Font.PLAIN, 17));
        labelInstr.setSize(700, 30);
        labelInstr.setLocation(300, 585);
        labelInstr.setForeground(Color.BLACK);
    }

    private void updateRoundLabel() {

        labelRound.setText("Round: " + Integer.toString(logic.round));
    }

    private void updatePlayerLabel() {

        labelPlayer.setText("Player: " + logic.currentPlayer);
    }

    private void updateReinforcementsLabel() {

        labelReinforcements.setText("(You have " + Integer.toString(reinforcements) + " reinforcements left.)");
        mainMapFrame.repaint();
    }

    private void updateTextField(String phase, String instruction) {

        labelPhase.setText(phase);
        labelInstr.setText(instruction);

        mainMapFrame.repaint();
    }

    //only call with valid player-numbers
    private void displayEndMessage(int player) {

        mainMapPanel.add(labelEnd);
        mainMapPanel.add(labelEndFrame);
        Border border = LineBorder.createBlackLineBorder();
        Color inner = Color.white;
        Color outer = Color.white;

        //deletes all Capitals in the Range of the Frame
        for (Map.Entry<String, Territory> entry : territories.entrySet()) {
            int x = entry.getValue().getCapitalLocation().x;
            int y = entry.getValue().getCapitalLocation().y;
            if ((x > 400 && x < 850) && (y > 200 && y < 450)) {
                entry.getValue().clearCapital();
            }

        }

        String ans;

        switch (player) {
            case 0:
                ans = "You lost :(";
                inner = new Color(245, 44, 24);
                outer = new Color(194, 0, 0);
                break;
            case 1:
                ans = "You won!";
                inner = new Color(92, 221, 73);
                outer = new Color(10, 180, 30);
                break;
            default:
                ans = "ERROR";
        }

        labelEnd.setText(ans);
        labelEnd.setFont(new Font("Arial", Font.BOLD, 40));
        labelEnd.setSize(400, 200);
        labelEnd.setLocation(425, 225);
        labelEnd.setForeground(Color.BLACK);
        labelEnd.setBackground(inner);
        labelEnd.setOpaque(true);
        labelEnd.setBorder(border);

        labelEndFrame.setSize(440, 240);
        labelEndFrame.setLocation(405, 205);
        labelEndFrame.setBackground(outer);
        labelEndFrame.setOpaque(true);
        labelEndFrame.setBorder(border);

        //labelRound.setVisible(false);
        labelPlayer.setVisible(false);
        labelReinforcements.setVisible(false);
        labelPhase.setVisible(false);
        labelInstr.setVisible(false);
        labelEndFrame.setVisible(true);
        labelEnd.setVisible(true);

        b.setVisible(true);
        b.setText("restart");
        mainMapFrame.repaint();
    }

    private void restartGame() {

        try {
            loadMap(readMapFile(path));
        } catch (NullPointerException npe) {

            System.out.println("File not found/not valid.");
        }

        initCapital();

        updateTextField("", "");
        labelPlayer.setText("");
        initTextField("Claim Phase:", "Select a territory");
        initPlayerField();
        initRoundLabel();
        initReinforcementsField();

        System.out.println();
        System.out.println("=============Started a new game=============");
        System.out.println();
        mainMapFrame.repaint();
    }

    //for testing purposes
    public void generateLostMap() {

        int counter = 0;
        for (Map.Entry<String, Territory> entry : territories.entrySet()) {

            if (counter == 0) {
                entry.getValue().setOccupied(0);
                entry.getValue().addReinforcement();
            } else {
                entry.getValue().setOccupied(1);
                entry.getValue().addReinforcement();
                entry.getValue().addReinforcement();
                entry.getValue().addReinforcement();
                entry.getValue().addReinforcement();
                entry.getValue().addReinforcement();
            }

            counter++;
        }

        logic.phase = 1;
        logic.currentPlayer = 1;
        initButton();
    }

    @Override
    public String toString() {

        String result = "";

        for (Map.Entry<String, Territory> entry : territories.entrySet()) {

            result += "Territory <" + entry.getKey() + ">\n";
            result += "     capital:   [" + entry.getValue().getCapitalLocation().x + ", " +
                    entry.getValue().getCapitalLocation().y + "]\n";

            result += "     neighbors: " + "\n";
            for (Territory territory : entry.getValue().getNeighbors()) {

                result += "                " + territory.getName() + "\n";
            }

            result += "     patches:   ";
            for (Polygon p : entry.getValue().getPatches()) {

                result += "{ ";
                for (int i = 0; i < p.xpoints.length; i++) {

                    result += "[" + p.xpoints[i] + ";";
                    result += p.ypoints[i] + "], ";
                }
                result += " }" + "\n                ";
            }

            result += "\n";
        }

        for (Continent continent : continents) {

            result += "Continent <" + continent.getName() + ">\n";
            result += "     bonus:   " + continent.getBonus() + "\n";

            result += "     members: ";
            for (Territory member : continent.getMembers()) {

                result += member.getName();
                result += "\n              ";
            }

            result += "\n";
        }

        return result;
    }
}