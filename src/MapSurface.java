import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by fabian on 21.01.16.
 */
public class MapSurface extends JPanel implements MouseListener{

    private Map<String, OccupiedTerritory> territories;

    private Graphics2D g2d = (Graphics2D)this.getGraphics();

    public MapSurface(Map<String, OccupiedTerritory> territories){

        this.territories = territories;
    }

    @Override
    public void paintComponent(Graphics g) {

        Graphics2D grph = (Graphics2D) g;

        for (Map.Entry<String, OccupiedTerritory> entry : territories.entrySet()) {

            for (Polygon p : entry.getValue().getPatches()) {

                grph.drawPolygon(p);
                if(entry.getValue().occupied)
                    grph.fillPolygon(p);
                System.out.println("paintComponent was called again");
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

        System.out.println("Click");
        for (Map.Entry<String, OccupiedTerritory> entry : territories.entrySet()) {

            for (Polygon p : entry.getValue().getPatches()) {

                if (p.contains(mouseEvent.getPoint())){

                    entry.getValue().setOccupied(true);
                    repaint();
                }

            }
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}
