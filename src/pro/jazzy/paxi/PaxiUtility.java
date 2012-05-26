package pro.jazzy.paxi;

import java.util.ArrayList;
import java.util.Hashtable;

import pro.jazzy.paxi.entity.Member;
import pro.jazzy.paxi.entity.Route;
import pro.jazzy.paxi.entity.events.MemberOutEvent;

/**
 * PaxiLogic class :)
 * @author Zachi
 */
public class PaxiUtility {
	public static Route CurrentRoute;
	public static int DEFAULT_ROUTE_TYPE = 0;
	public static Hashtable<String, Member> members = new Hashtable<String, Member>();
	
	/**
	 * price per 1 fuel x 100 
	 */
	public static int pricePerFuel = 535;
	/**
	 * fuel per 1 distance
	 */
	public static int[] fuelPerDistance = new int[] { 700, 800, 750 }; 
	
	public final static int ROUTE_TYPE_CITY = 0;
	public final static int ROUTE_TYPE_HIGHWAY = 1;
	
	/**
	 * Avg. fuel burning
	 */
	public final static int ROUTE_TYPE_MIXED = 2;
	
	/**
	 * New journey, New Quest ;)
	 * Wipe out last remembered road and everything about it. 
	 */
	public static void newRoute() {
		CurrentRoute = new Route();
	}
	
	public static void memberIn(String memberName) {
		Member member = new Member(memberName);
		member.setSignInOnDistance(getCurrentDistance());
		members.put(memberName, member);
		CurrentRoute.getRoadEvents().add(member);
	}
	
	/**
	 * Member get out of this car
	 * @param memberName
	 * @return how Much he has to pay
	 */
	public static int memberOut(String memberName) {
		Member m = members.get(memberName);
		m.setSignOutOnDistance(CurrentRoute.getCurrentDistance());
		CurrentRoute.getRoadEvents().add(new MemberOutEvent(m));
		return m.howMuchToPay();
	}
	
	/**
	 * @return memberNames
	 */
	public static String[] getMemberNames() {
		return (String[]) members.keySet().toArray();
	}
	
	public static Member[] getMembers() {
		ArrayList<Member> memb = new ArrayList<Member>();
		for(String key : members.keySet()) {
			memb.add(members.get(key));
		}
		return (Member[])memb.toArray();
	}
	
	public static Member getMemberForName(String memberName) {
		return members.get(memberName);
	}
	
	public static int getCurrentDistance() {
		return CurrentRoute.getCurrentDistance();
	}
	
	public static void addDistance(int distance) {
		CurrentRoute.addDistance(distance);
	}
	
	
}