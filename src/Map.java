import javax.swing.*;
import java.awt.*;

/**
 * Created by fabian on 15.01.16.
 */

public class Map extends JFrame{

    Image img;
    Graphics grph;
    int i;

    public static void main(String[] args) {
        Map map = new Map();
    }

    public Map(){
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1250,650);
        this.setResizable(true);
        this.setVisible(true);
        i = 0;
    }

    public void paint(Graphics g){

        img = createImage(1250,650);
        grph = img.getGraphics();
        paintComponent(grph);
        g.drawImage(img, 0, 0, this);

    }


    public void paintComponent(Graphics g){


        g.setColor(new Color(0,0,150));
        g.fillRect(0, 0, 1250,650);


        repaint();
    }
}
