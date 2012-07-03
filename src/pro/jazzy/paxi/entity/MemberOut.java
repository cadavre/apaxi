
package pro.jazzy.paxi.entity;

public class MemberOut extends RoadEvent {

    /**
     * Members kicked off a car - this member included
     */
    static int membersOut = 0;

    Member member;

    public MemberOut(Member member) {

        this.member = member;
        membersOut++;
    }

    public Member getMember() {

        return member;
    }

}
