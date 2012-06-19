package pro.jazzy.paxi.entity;

import java.util.ArrayList;

import pro.jazzy.paxi.PaxiUtility;
import pro.jazzy.paxi.entity.events.OnRoadEvent;

/**
 * Route implementation
 * 
 * @author Zachi
 * @author Seweryn Zeman <seweryn.zeman@jazzy.pro>
 */
public class Route {
	private int currentDistance = 0;
	private ArrayList<OnRoadEvent> roadEvents;
	private int currentRouteType;

	public Route() {
		this.currentDistance = 0;
		this.roadEvents = new ArrayList<OnRoadEvent>();
		this.currentRouteType = PaxiUtility.DEFAULT_ROUTE_TYPE;
	}

	public void addDistance(int addedDistance) {
		this.currentDistance += addedDistance;
	}

	public void changeRouteType(int routeType) {
		this.currentRouteType = routeType;
	}

	public int getCurrentDistance() {
		return new Double(currentDistance).intValue();
	}

	public void setCurrentDistance(int currentDistance) {
		this.currentDistance = currentDistance;
	}

	public ArrayList<OnRoadEvent> getRoadEvents() {
		return roadEvents;
	}

	public void setRoadEvents(ArrayList<OnRoadEvent> roadEvents) {
		this.roadEvents = roadEvents;
	}

	public int getCurrentRouteType() {
		return currentRouteType;
	}

	public void setCurrentRouteType(int currentRouteType) {
		this.currentRouteType = currentRouteType;
	}
}