package pro.jazzy.paxi.entity.events;

import java.io.Serializable;

import pro.jazzy.paxi.entity.Member;

public class MemberOutEvent extends OnRoadEventBasicImpl implements Serializable {

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
