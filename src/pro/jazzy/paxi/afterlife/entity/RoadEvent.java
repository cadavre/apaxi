
package pro.jazzy.paxi.afterlife.entity;

import android.location.Location;

/**
 * Interface for road event
 * 
 * @author Seweryn Zeman <seweryn.zeman@jazzy.pro>
 */
public class RoadEvent {

    int distance;

    Location location;

    public int getDistance() {

        return distance;
    }

    public void setDistance(int distance) {

        this.distance = distance;
    }

    public Location getLocation() {

        return location;
    }

    public void setLocation(Location location) {

        this.location = location;
    }

}
