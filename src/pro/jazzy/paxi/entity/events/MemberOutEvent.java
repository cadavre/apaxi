package pro.jazzy.paxi.entity.events;

import pro.jazzy.paxi.entity.Member;

public class MemberOutEvent extends OnRoadEventBasicImpl {

	private Member member;
	
	public MemberOutEvent(Member member) {
		this.member = member;
	}
	
	public int onWhatDistance() {
		return member.getSignOutOnDistance();
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}
	
	

}
