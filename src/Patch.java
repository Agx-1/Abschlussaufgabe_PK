import java.awt.*;

/**
 * Created by fabian on 19.01.16.
 */
public class Patch {

    private Polygon patch = new Polygon();
    private String territory;

    public Patch(String territory, int[] coordinates){

        this.territory = territory;

        for (int i = 0; i < coordinates.length - 1; i+= 2) {

            patch.addPoint(coordinates[i], coordinates[i+1]);
        }
    }
}
