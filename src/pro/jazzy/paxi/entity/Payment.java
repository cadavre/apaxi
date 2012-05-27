package pro.jazzy.paxi.entity;

import java.io.Serializable;

import pro.jazzy.paxi.PaxiUtility;
import pro.jazzy.paxi.entity.events.OnRoadEventBasicImpl;

/**
 * Additional payment taken on the road
 * 
 * @author Zachi
 */
public class Payment extends OnRoadEventBasicImpl implements Serializable {

	/**
	 * On what distance this payment was taken
	 */
	private int distance;

	/**
	 * how much was it?
	 */
	private float amount;

	public Payment(float amount) {
		this.amount = amount;
		this.distance = PaxiUtility.CurrentRoute.getCurrentDistance();
	}

	public int onWhatDistance() {
		return this.distance;
	}

	public float getAmount() {
		return this.amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}
}
