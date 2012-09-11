
package pro.jazzy.paxi.entity;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

/**
 * @author
 */
public class Route {

    static final String TAG = "Paxi";

    /**
     * Mixed fuel consumption mode
     */
    public static final int MIXED_MODE = 0;

    /**
     * City fuel consumption mode
     */
    public static final int CITY_MODE = 1;

    /**
     * Highway fuel consumption mode
     */
    public static final int HIGHWAY_MODE = 2;

    /**
     * Metrics in kilometers (metric)
     */
    public static final int KILOMETERS = 0;

    /**
     * Metrics in miles (imperial)
     */
    public static final int MILES = 1;

    /**
     * Divider for meters->kilometers
     */
    public static final float KILOMETERS_DIVIDER = 1000;

    /**
     * Divider for meters->miles
     */
    public static final float MILES_DIVIDER = 1609.344f;

    /**
     * Current route distance
     */
    private int currentDistance = 0;

    /**
     * Current location
     */
    private Location currentLocation;

    private ArrayList<RoadEvent> roadEvents = new ArrayList<RoadEvent>();

    private float currentDivider = KILOMETERS_DIVIDER;

    private int currentMode = MIXED_MODE;

    private float[] fuelConsumption = { 0, 0, 0 };

    private float fuelPrice = 0;

    public Route(SharedPreferences preferences) {

        // load parameters
        fuelPrice = preferences.getFloat("fuelPrice", 0);
        fuelConsumption[MIXED_MODE] = preferences.getFloat("fuelMixed", 8.0f);
        fuelConsumption[CITY_MODE] = preferences.getFloat("fuelCity", fuelConsumption[MIXED_MODE]) == 0 ? fuelConsumption[MIXED_MODE]
                : preferences.getFloat("fuelCity", fuelConsumption[MIXED_MODE]);
        fuelConsumption[HIGHWAY_MODE] = preferences.getFloat("fuelHighway",
                fuelConsumption[MIXED_MODE]) == 0 ? fuelConsumption[MIXED_MODE] : preferences
                .getFloat("fuelHighway", fuelConsumption[MIXED_MODE]);
        currentDivider = preferences.getInt("metrics", KILOMETERS) == KILOMETERS ? KILOMETERS_DIVIDER
                : MILES_DIVIDER;
        // set route fuel consumption mode
        roadEvents.add(new ModeChange(preferences.getInt("mode", currentMode)));
        Member.membersIn = 0;
        MemberOut.membersOut = 0;
    }

    public void addDistance(int distance) {

        this.currentDistance += distance;
    }

    public void setDistance(int distance) {

        this.currentDistance = distance;
    }

    public int getDistance() {

        return currentDistance;
    }

    public void setLocation(Location location) {

        this.currentLocation = location;
    }

    public Location getLocation() {

        return this.currentLocation;
    }

    public void memberIn(Member memberIn) {

        memberIn.setDistance(currentDistance);
        memberIn.setLocation(currentLocation);
        roadEvents.add(memberIn);
    }

    public void addPayment(Payment payment) {

        payment.setDistance(currentDistance);
        payment.setLocation(currentLocation);
        roadEvents.add(payment);
    }

    public void changeMode(ModeChange modeChange) {

        modeChange.setDistance(currentDistance);
        modeChange.setLocation(currentLocation);
        roadEvents.add(modeChange);
    }

    public ArrayList<RoadEvent> getAllEvents() {

        return this.roadEvents;
    }

    public float memberOut(MemberOut memberOut) {

        float toPay = 0.0f;
        int routeMode = -1; // will represent actual mode
        int memberCount = 0; // will represent actual members count
        int currentDistance = -1; // will represent actual distance
        Member memberIn = null;

        memberOut.setDistance(this.currentDistance);
        memberOut.setLocation(currentLocation);
        roadEvents.add(memberOut);

        for (RoadEvent event : roadEvents) {
            if (event instanceof ModeChange) {
                routeMode = ((ModeChange) event).getMode();
            } else if (event instanceof Member) {
                if (((Member) event).equals(memberOut.getMember())) {
                    memberIn = (Member) event;
                    break;
                }
            }
        }

        memberCount = 0;
        int gotIn = memberIn.getDistance();
        int gotOut = memberOut.getDistance();
        currentDistance = gotIn;

        Log.e(TAG, "userOut: " + memberIn.getMemberName() + " traveled from " + gotIn + " to "
                + gotOut);

        for (RoadEvent event : roadEvents) {
            if (event.getDistance() < gotIn) {
                if (event instanceof Member) {
                    memberCount++;
                } else if (event instanceof MemberOut) {
                    memberCount--;
                }
            }
            if (event.getDistance() >= gotIn && event.getDistance() <= gotOut) {
                if (event instanceof Payment) {
                    toPay += ((Payment) event).getAmount() / memberCount;
                } else if (event instanceof ModeChange) {
                    toPay += calculate(event.getDistance() - currentDistance, memberCount,
                            routeMode);
                    routeMode = ((ModeChange) event).getMode();
                    currentDistance = event.getDistance();
                } else if (event instanceof Member) {
                    toPay += calculate(event.getDistance() - currentDistance, memberCount,
                            routeMode);
                    memberCount++;
                    currentDistance = event.getDistance();
                } else if (event instanceof MemberOut) {
                    toPay += calculate(event.getDistance() - currentDistance, memberCount,
                            routeMode);
                    memberCount--;
                    currentDistance = event.getDistance();
                    if (((MemberOut) event).getMember().equals(memberOut.getMember())) {
                        break;
                        /*
                         * Break further computing due to next events, like
                         * payments occurred on same distance as user get off.
                         * This protects user from paying a payment he shouldn't
                         * pay.
                         */
                    }
                }
            }
        }

        toPay *= 100f;
        return Math.round(toPay) / 100f;
    }

    private float calculate(int distance, int persons, int routeMode) {

        Log.w(TAG, "calculations data: price " + fuelPrice + " metrics " + currentDivider);
        Log.w(TAG, "calculations data: distance " + distance + " consumption "
                + fuelConsumption[routeMode] + " per " + persons);
        float payPart = 0;
        payPart = fuelPrice * (distance / currentDivider);
        if (currentDivider == KILOMETERS_DIVIDER) {
            payPart *= (fuelConsumption[routeMode] / 100);
        } else {
            payPart *= (1 / fuelConsumption[routeMode]);
        }
        payPart /= persons;
        return Float.isNaN(payPart) ? 0 : payPart;
    }

    public ArrayList<Member> getMembers() {

        ArrayList<Member> toReturn = new ArrayList<Member>();
        for (RoadEvent event : roadEvents) {
            if (event instanceof Member) {
                toReturn.add((Member) event);
            }
        }
        return toReturn;
    }

    public String[] getMemberNames() {

        ArrayList<String> memberNamesList = new ArrayList<String>();
        memberNamesList.ensureCapacity(getMembers().size());
        for (Member member : getMembers()) {
            memberNamesList.add(member.getMemberName());
        }
        String[] memberNamesArray = new String[getMembers().size()];
        memberNamesList.toArray(memberNamesArray);
        return memberNamesArray;
    }

    public ArrayList<Long> getMemberIds() {

        ArrayList<Long> memberIdsList = new ArrayList<Long>();
        memberIdsList.ensureCapacity(getMembers().size());
        for (Member member : getMembers()) {
            memberIdsList.add(member.getId());
        }
        return memberIdsList;
    }

    public boolean removeMember(Member member) {

        for (RoadEvent event : roadEvents) {
            if (event instanceof MemberOut) {
                if (((MemberOut) event).getMember().equals(member)) {
                    roadEvents.remove(event);
                }
            }
        }
        Member.membersIn--;
        // MemberOut.membersOut--; Really? Cannot remove member after he got out of the car...
        return roadEvents.remove(member);
    }

    public ArrayList<Payment> getPayments() {

        ArrayList<Payment> toReturn = new ArrayList<Payment>();
        for (RoadEvent event : roadEvents) {
            if (event instanceof Payment) {
                toReturn.add((Payment) event);
            }
        }
        return toReturn;
    }

    public boolean removePayment(Payment payment) {

        return roadEvents.remove(payment);
    }

    public int getMembersCountOnboard() {

        return Member.membersIn - MemberOut.membersOut;
    }

}
