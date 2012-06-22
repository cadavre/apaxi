
package pro.jazzy.paxi.entity;

import pro.jazzy.paxi.PaxiUtility;
import pro.jazzy.paxi.entity.events.OnRoadEventBasicImpl;

/**
 * Additional payment taken on the road
 * 
 * @author Zachi
 * @author Seweryn Zeman <seweryn.zeman@jazzy.pro>
 */
public class Payment extends OnRoadEventBasicImpl {

    private int distance;

    private float amount;

    public Payment(float amount) {

        this.amount = amount;
        this.distance = PaxiUtility.currentRoute.getCurrentDistance();
    }

    public int onWhatDistance() {

        return this.distance;
    }

    public float getAmount() {

        return this.amount;
    }

    public void setAmount(float amount) {

        this.amount = amount;
    }
}
