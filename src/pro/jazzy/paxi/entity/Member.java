
package pro.jazzy.paxi.entity;

public class Member implements RoadEvent {

    /**
     * Members in da car - this member included
     */
    static int membersIn = 0;

    int distance;

    long id;

    String member;

    String avatarUri;

    boolean isOnboard = true;

    private static Member emptyInstance = new Member("", 0);

    public Member(String member, long id) {

        this.member = member;
        this.id = id;
        membersIn++;
    }

    public long getId() {

        return this.id;
    }

    private void setId(long id) {

        this.id = id;
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

    public String getMemberName() {

        return member;
    }

    @Override
    public String toString() {

        return this.member;
    }

    static public Member getInstance(long id) {

        emptyInstance.setId(id);
        return emptyInstance;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Member other = (Member) obj;
        if (id != other.id)
            return false;
        return true;
    }

}
