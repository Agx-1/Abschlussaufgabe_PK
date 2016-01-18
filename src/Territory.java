import java.awt.*;

/**
 * Created by fabian on 15.01.16.
 */
public interface Territory {

    //get the number of armies in current territory
    int getArmies();

    //decrease the number of armies in current territory by number
    void removeArmies(int number);

    //increase the number of armies in current territory by 1
    void addArmies(int number);

}