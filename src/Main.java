import javax.swing.*;
import java.awt.*;

/**
 * Created by fabian on 21.01.16.
 */
public class Main {

    public static void main(String[] args) {

        GameMap gameMap = new GameMap("maps/squares.map");

        System.out.println(gameMap.toString());
    }
}
