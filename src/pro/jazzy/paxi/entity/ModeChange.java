
package pro.jazzy.paxi.entity;

public class ModeChange implements RoadEvent {

    int distance;

    int mode;

    public ModeChange(int mode) {

        this.mode = mode;
    }

    @Override
    public int getDistance() {

        return distance;
    }

    @Override
    public void setDistance(int distance) {

        this.distance = distance;
    }

    public int getMode() {

        return mode;
    }

}
