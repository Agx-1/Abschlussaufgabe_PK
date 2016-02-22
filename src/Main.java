import java.util.LinkedList;
import java.util.Map;

/**
 * Created by fabian on 21.01.16.
 */
public class Main {

    public static void main(String[] args) {

        GameMap gameMap = new GameMap("maps/world.map");

        System.out.println(gameMap.toString());
        //gameMap.generateLostMap();
        TestingTerritory terr1 = new TestingTerritory(4);
        TestingTerritory terr2 = new TestingTerritory(2);

        //System.out.println(gameMap.toString());

    }
}
