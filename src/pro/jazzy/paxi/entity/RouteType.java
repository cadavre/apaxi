
package pro.jazzy.paxi.entity;

import pro.jazzy.paxi.PaxiUtility;
import pro.jazzy.paxi.entity.events.OnRoadEventBasicImpl;

/**
 * Route type shows how much you pay for a distance
 * 
 * @author Zachi
 */
public class RouteType extends OnRoadEventBasicImpl {

    private int from;

    private int to = -1;

    private int definedRouteType;

    public RouteType() {

        this.from = PaxiUtility.currentRoute.getCurrentDistance();
        this.definedRouteType = PaxiUtility.DEFAULT_ROUTE_TYPE;
    }

    public int getDistance() {

        if (to != -1) {
            return this.to - this.from;
        } else {
            return PaxiUtility.currentRoute.getCurrentDistance() - this.from;
        }
    }

    public int getFrom() {

        return from;
    }

    public void setFrom(int from) {

        this.from = from;
    }

    public int getTo() {

        return to;
    }

    public void setTo(int to) {

        this.to = to;
    }

    public int getDefinedRouteType() {

        return definedRouteType;
    }

    public void setDefinedRouteType(int definedRouteType) {

        this.definedRouteType = definedRouteType;
    }

    public int onWhatDistance() {

        return this.getFrom();
    }
}
