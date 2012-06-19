package pro.jazzy.paxi.entity;

import java.util.ArrayList;
import java.util.Collections;

import pro.jazzy.paxi.PaxiUtility;
import pro.jazzy.paxi.entity.events.MemberOutEvent;
import pro.jazzy.paxi.entity.events.OnRoadEvent;
import pro.jazzy.paxi.entity.events.OnRoadEventBasicImpl;

/**
 * Person in the car. Also as an event 'member get into a car'
 * 
 * @author Zachi
 */
public class Member extends OnRoadEventBasicImpl {

	/**
	 * Distance on member in
	 */
	private int signInOnDistance = 0;

	/**
	 * Distance on member off
	 */
	private int signOutOnDistance = -1;

	private String name;

	private String photoUri;

	/**
	 * Default constructor. Member get's into car on currentDistance
	 * 
	 * @param name
	 */
	public Member(String name) {
		this.name = name;
		this.signInOnDistance = PaxiUtility.currentRoute.getCurrentDistance();
	}

	public int getSignInOnDistance() {
		return signInOnDistance;
	}

	public void setSignInOnDistance(int signInOnDistance) {
		this.signInOnDistance = signInOnDistance;
	}

	public int getSignOutOnDistance() {
		return signOutOnDistance;
	}

	public void setSignOutOnDistance(int signOutOnDistance) {
		this.signOutOnDistance = signOutOnDistance;
	}

	public String getPhotoUri() {
		return photoUri;
	}

	public void setPhotoUri(String photoUri) {
		this.photoUri = photoUri;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float howMuchToPay() {
		// 1. sort events array
		Collections.sort(PaxiUtility.currentRoute.getRoadEvents());
		// 2. while not thisEvent - count members
		ArrayList<OnRoadEvent> allEvents = PaxiUtility.currentRoute
				.getRoadEvents();
		float toPay = 0;
		int i = 0; // firstEvent
		int membersOnboard = 0;
		int currentType = PaxiUtility.currentRoute.getCurrentRouteType();
		while (i < allEvents.size() && allEvents.get(i) != this) {
			OnRoadEvent onroadEvent = allEvents.get(i);
			if (onroadEvent instanceof Member) {
				membersOnboard++;
			} else if (onroadEvent instanceof MemberOutEvent) {
				membersOnboard--;
			} else if (onroadEvent instanceof RouteType) {
				currentType = ((RouteType) onroadEvent).getDefinedRouteType();
			}
			i++;
		}
		// now 'i' points to current event - user get in the car
		// find the event of member leaving the car
		int j = 0;
		if (this.isOnboard()) {
			j = allEvents.size() - 1;
		} else {
			for (j = allEvents.size() - 1; j > i; j--) {
				if (allEvents.get(j) instanceof MemberOutEvent) {
					if (((MemberOutEvent) allEvents.get(j)).getMember() == this) {
						// we have correct j - the moment user leaves
						break;
					}
				}
			}
		}
		int lastKnownDistance = allEvents.get(i).onWhatDistance();
		for (int k = i + 1; k <= j; k++) {
			OnRoadEvent onroadEvent = allEvents.get(k);
			// calculate how much it cost to get from lastKnownDistance to
			// onroadEvent.onWhatDistance();
			toPay += calculate(
					onroadEvent.onWhatDistance() - lastKnownDistance,
					currentType, membersOnboard);
			if (onroadEvent instanceof Member) {
				membersOnboard++;
			} else if (onroadEvent instanceof MemberOutEvent) {
				membersOnboard--;
			} else if (onroadEvent instanceof RouteType) {
				currentType = ((RouteType) onroadEvent).getDefinedRouteType();
			} else if (onroadEvent instanceof Payment) {
				toPay += ((Payment) onroadEvent).getAmount() / membersOnboard;
			}
			lastKnownDistance = allEvents.get(k).onWhatDistance();
		}
		// fakeEvent - some road after last event
		if (this.isOnboard()
				&& lastKnownDistance < PaxiUtility.currentRoute
						.getCurrentDistance()) {
			toPay += calculate(PaxiUtility.currentRoute.getCurrentDistance(),
					currentType, membersOnboard);
		}
		return toPay;
	}

	public int onWhatDistance() {
		return this.signInOnDistance;
	}

	private float calculate(int meters, int routeType, int persons) {
		if (persons != 0) {
			float result = 0.0f + (meters / 1000) * (PaxiUtility.pricePerFuel)
					* ((PaxiUtility.fuelPer100units[routeType]) / 100) / persons;
			return result;
		} else {
			return 0;
		}
	}

	public boolean isOnboard() {
		return signOutOnDistance == -1;
	}

	public int getDistance() {
		if (isOnboard()) {
			int t = PaxiUtility.getCurrentDistance();
			return t - signInOnDistance;
		} else {
			return signOutOnDistance - signInOnDistance;
		}
	}
}