
package pro.jazzy.paxi.entity;

public class MemberOut implements RoadEvent {

    int distance;

    String member;

    /**
     * Members kicked off a car - this member included
     */
    static int membersOut = 0;

    public MemberOut(String member) {

        this.member = member;
        membersOut++;
    }

    @Override
    public int getDistance() {

        return distance;
    }

    @Override
    public void setDistance(int distance) {

        this.distance = distance;
    }

    public String getMember() {

        return member;
    }

}
