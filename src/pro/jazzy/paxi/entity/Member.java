
package pro.jazzy.paxi.entity;

public class Member implements RoadEvent {

    int distance;

    String member;

    String avatarUri;

    /**
     * Members in da car - this member included
     */
    static int membersIn = 0;

    public Member(String member) {

        this.member = member;
        membersIn++;
    }

    @Override
    public int getDistance() {

        return distance;
    }

    @Override
    public void setDistance(int distance) {

        this.distance = distance;
    }

    public String getAvatarUri() {

        return avatarUri;
    }

    public void setAvatarUri(String uri) {

        this.avatarUri = uri;
    }

    public String getMember() {

        return member;
    }

    @Override
    public String toString() {

        return this.member;
    }

}
