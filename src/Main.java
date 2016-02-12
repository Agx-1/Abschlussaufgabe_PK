import javax.swing.*;
import java.awt.*;

/**
 * Created by fabian on 21.01.16.
 */
public class Main {

    public static void main(String[] args) {

        GameMap gameMap = new GameMap("maps/world.map");

        TestingTerritory terr1 = new TestingTerritory(4);
        TestingTerritory terr2 = new TestingTerritory(2);

        GameLogic.attack(terr1, terr2);
        //System.out.println(gameMap.toString());
    }
}
