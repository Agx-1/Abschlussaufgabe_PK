import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Created by fabian on 21.01.16.
 */
public class MapSurface extends JPanel {

    private Map<String, OccupiedTerritory> territories;

    public MapSurface(Map<String, OccupiedTerritory> territories){

        this.territories = territories;
    }

    @Override
    public void paintComponent(Graphics g) {

        Graphics2D grph = (Graphics2D) g;

        for (Map.Entry<String, OccupiedTerritory> entry : territories.entrySet()) {

            for (Polygon p : entry.getValue().getPatches()) {

                grph.drawPolygon(p);
            }
        }
    }
}
