
package pro.jazzy.paxi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import pro.jazzy.paxi.entity.Member;
import pro.jazzy.paxi.entity.Payment;
import pro.jazzy.paxi.entity.Route;
import pro.jazzy.paxi.entity.RouteType;
import pro.jazzy.paxi.entity.events.MemberOutEvent;

/**
 * PaxiUtility Class
 * 
 * @author Zachi
 * @author Seweryn Zeman <seweryn.zeman@jazzy.pro>
 */
public class PaxiUtility {

    public static Route currentRoute = new Route();

    public static int DEFAULT_ROUTE_TYPE = 0;

    public static Hashtable<String, Member> members = new Hashtable<String, Member>();

    /**
     * Price per 1 unit of fuel
     */
    public static float pricePerFuel = 0.0f;

    /**
     * Fuel burning
     */
    public static float[] fuelPer100units = new float[] { 7.0f, 8.0f, 9.0f };

    public final static int ROUTE_TYPE_CITY = 0;

    public final static int ROUTE_TYPE_HIGHWAY = 1;

    public final static int ROUTE_TYPE_MIXED = 2;

    /**
     * New journey, New Quest! Wipe out last remembered road and everything connected to it.
     */
    public static void newRoute() {

        currentRoute = new Route();
        members = new Hashtable<String, Member>();
    }

    /**
     * New member get in
     * 
     * @param memberName
     */
    public static Member memberIn(String memberName) {

        if (currentRoute == null) {
            currentRoute = new Route();
        }
        Member member = new Member(memberName);
        member.setSignInOnDistance(getCurrentDistance());
        members.put(memberName, member);
        currentRoute.getRoadEvents().add(member);
        return member;
    }

    /**
     * Member get out
     * 
     * @param memberName
     * @return how Much he has to pay
     */
    public static float memberOut(String memberName) {

        if (currentRoute == null) {
            currentRoute = new Route();
        }
        Member m = members.get(memberName);
        m.setSignOutOnDistance(currentRoute.getCurrentDistance());
        currentRoute.getRoadEvents().add(new MemberOutEvent(m));
        return m.howMuchToPay();
    }

    public static void removeMember(String memberName) {

        members.remove(memberName);
    }

    /**
     * Get all members as names array
     * 
     * @return memberNames
     */
    public static String[] getMemberNames() {

        if (members == null || members.size() == 0) {
            String[] s = new String[0];
            return s;
        }
        Iterator<String> it = members.keySet().iterator();
        ArrayList<String> memberNamesList = new ArrayList<String>();
        while (it.hasNext()) {
            memberNamesList.add(it.next());
        }
        String[] memberNamesArray = new String[memberNamesList.size()];
        memberNamesList.toArray(memberNamesArray);
        return memberNamesArray;
    }

    /**
     * Get all members as array
     * 
     * @return
     */
    public static Member[] getMembers() {

        if (members == null || members.size() == 0) {
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
     * Self-explanatory
     * 
     * @param memberName
     * @return
     */
    public static Member getMemberByName(String memberName) {

        if (members == null) {
            return null;
        }
        if (members.containsKey(memberName)) {
            return members.get(memberName);
        }
        return null;
    }

    /**
     * Self-explanatory
     * 
     * @return
     */
    public static int getCurrentDistance() {

        if (currentRoute == null) {
            return 0;
        }
        return currentRoute.getCurrentDistance();
    }

    /**
     * Just add distance to current road
     * 
     * @param distance (in meters)
     */
    public static void addDistance(int distance) {

        if (currentRoute == null) {
            currentRoute = new Route();
        }
        currentRoute.addDistance(distance);
    }

    /**
     * Add Payment on road
     * 
     * @param payment
     */
    public static void addPayment(float payment) {

        if (currentRoute == null) {
            currentRoute = new Route();
        }
        Payment p = new Payment(payment);
        currentRoute.getRoadEvents().add(p);
    }

    /**
     * Change route type : PaxiUtility.ROUTE_TYPE_CITY PaxiUtility.ROUTE_TYPE_HIGHWAY PaxiUtility.ROUTE_TYPE_MIXED
     * 
     * @param newRouteType
     */
    public static void changeRouteType(int newRouteType) {

        if (currentRoute == null) {
            currentRoute = new Route();
        }
        currentRoute.setCurrentRouteType(newRouteType);
        RouteType rt = new RouteType();
        rt.setDefinedRouteType(newRouteType);
        currentRoute.getRoadEvents().add(rt);
    }

    /**
     * Fuel burning per 100 km
     * 
     * @param routeType
     * @param burning
     */
    public static void setBurnByRoutetype(int routeType, int burning) {

        fuelPer100units[routeType] = burning;
    }

    /**
     * Everybody leaves!
     */
    public static void stopRoute() {

        if (currentRoute == null) {
            currentRoute = new Route();
        }
        for (String s : members.keySet()) {
            Member m = members.get(s);
            if (m.isOnboard()) {
                memberOut(s);
            }
        }
    }

}
