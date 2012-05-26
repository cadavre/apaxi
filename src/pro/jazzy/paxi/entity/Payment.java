package pro.jazzy.paxi.entity;

import pro.jazzy.paxi.PaxiUtility;
import pro.jazzy.paxi.entity.events.OnRoadEventBasicImpl;

/**
 * Additional payment taken on the road
 * 
 * @author Zachi
 */
public class Payment extends OnRoadEventBasicImpl {

	/**
	 * On what distance this payment was taken
	 */
	private int distance;

	/**
	 * how much was it?
	 */
	private int amount;

	public Payment(int amount) {
		this.amount = amount;
		this.distance = PaxiUtility.CurrentRoute.getCurrentDistance();
	}

	public int onWhatDistance() {
		return this.distance;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
}
