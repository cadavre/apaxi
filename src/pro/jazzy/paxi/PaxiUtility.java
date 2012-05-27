package pro.jazzy.paxi;

import java.util.ArrayList;
import java.util.Hashtable;

import pro.jazzy.paxi.entity.Member;
import pro.jazzy.paxi.entity.Payment;
import pro.jazzy.paxi.entity.Route;
import pro.jazzy.paxi.entity.RouteType;
import pro.jazzy.paxi.entity.events.MemberOutEvent;

/**
 * PaxiLogic class :)
 * 
 * @author Zachi
 */
public class PaxiUtility {
	public static Route CurrentRoute = new Route();
	public static int DEFAULT_ROUTE_TYPE = 0;
	public static Hashtable<String, Member> members = new Hashtable<String, Member>();

	/**
	 * price per 1 fuel x 100
	 */
	public static int pricePerFuel = 535;

	/**
	 * fuel per 1000 distance * 100 (100 km)
	 */
	public static int[] fuelPerDistance = new int[] { 700, 800, 750 };

	public final static int ROUTE_TYPE_CITY = 0;
	public final static int ROUTE_TYPE_HIGHWAY = 1;

	/**
	 * Avg. fuel burning
	 */
	public final static int ROUTE_TYPE_MIXED = 2;

	/**
	 * New journey, New Quest ;) Wipe out last remembered road and everything
	 * about it.
	 */
	public static void newRoute() {
		CurrentRoute = new Route();
		members = new Hashtable<String, Member>();
	}

	/**
	 * new member gets into car
	 * 
	 * @param memberName
	 */
	public static void memberIn(String memberName) {
		if(CurrentRoute == null) {
			CurrentRoute = new Route();
		}
		Member member = new Member(memberName);
		member.setSignInOnDistance(getCurrentDistance());
		members.put(memberName, member);
		CurrentRoute.getRoadEvents().add(member);
	}

	/**
	 * Member get out of this car
	 * 
	 * @param memberName
	 * @return how Much he has to pay
	 */
	public static double memberOut(String memberName) {
		if(CurrentRoute == null) {
			CurrentRoute = new Route();
		}
		Member m = members.get(memberName);
		m.setSignOutOnDistance(CurrentRoute.getCurrentDistance());
		CurrentRoute.getRoadEvents().add(new MemberOutEvent(m));
		return m.howMuchToPay();
	}

	/**
	 * @return memberNames
	 */
	public static String[] getMemberNames() {
		if(members == null || members.size() == 0) {
			String[] s = new String[0];
			return s;
		}
		return (String[]) members.keySet().toArray();
	}

	/**
	 * get all members as array
	 * 
	 * @return
	 */
	public static Member[] getMembers() {
		if(members == null || members.size() == 0) {
			Member[] m = new Member[0];
			return m;
		}
		ArrayList<Member> memb = new ArrayList<Member>();
		for (String key : members.keySet()) {
			memb.add(members.get(key));
		}
		Member[] memberArray = new Member[members.size()];
		
		memb.toArray(memberArray);
		return memberArray;
	}

	/**
	 * self-explanatory
	 * 
	 * @param memberName
	 * @return
	 */
	public static Member getMemberForName(String memberName) {
		if(members == null) {
			return null;
		}
		if(members.containsKey(memberName)) {
			return members.get(memberName);
		}
		return null;
	}

	/**
	 * self-explanatory
	 * 
	 * @return
	 */
	public static int getCurrentDistance() {
		if(CurrentRoute == null) {
			return 0;
		}
		return CurrentRoute.getCurrentDistance();
	}

	/**
	 * just add distance to current road
	 * 
	 * @param distance (in meters)
	 */
	public static void addDistance(int distance) {
		if(CurrentRoute == null) {
			CurrentRoute = new Route();
		}
		CurrentRoute.addDistance(distance);
	}

	/**
	 * add Payment on road
	 * @param payment
	 */
	public static void addPayment(int payment) {
		if(CurrentRoute == null) {
			CurrentRoute = new Route();
		}
		Payment p = new Payment(payment);
		CurrentRoute.getRoadEvents().add(p);
	}

	/**
	 * change route type : PaxiUtility.ROUTE_TYPE_CITY
	 * PaxiUtility.ROUTE_TYPE_HIGHWAY PaxiUtility.ROUTE_TYPE_MIXED
	 * 
	 * @param newRouteType
	 */
	public static void changeRouteType(int newRouteType) {
		if(CurrentRoute == null) {
			CurrentRoute = new Route();
		}
		CurrentRoute.setCurrentRouteType(newRouteType);
		RouteType rt = new RouteType();
		rt.setDefinedRouteType(newRouteType);
		CurrentRoute.getRoadEvents().add(rt);
	}

	/**
	 * Fuel burning * 100 per 100 km
	 * @param routeType
	 * @param burning
	 */
	public static void setBurnForRoutetype(int routeType, int burning) {
		fuelPerDistance[routeType] = burning;
	}

	/**
	 * Everybody leaves!
	 */
	public static void stopRoute() {
		if(CurrentRoute == null) {
			CurrentRoute = new Route();
		}
		for (String s : members.keySet()) {
			Member m = members.get(s);
			if (m.isOnboard()) {
				memberOut(s);
			}
		}
	}
}