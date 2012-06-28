package pro.jazzy.paxi.entity;

public class MemberOut implements RoadEvent {

	/**
	 * Members kicked off a car - this member included
	 */
	static int membersOut = 0;

	int distance;

	Member member;

	public MemberOut(Member member) {

		this.member = member;
		membersOut += 2; // one for -1 and one for new Member created when MemberOut
	}

	@Override
	public int getDistance() {

		return distance;
	}

	@Override
	public void setDistance(int distance) {

		this.distance = distance;
	}

	public Member getMember() {

		return member;
	}

}