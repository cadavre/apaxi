package pro.jazzy.paxi.entity.events;

import java.io.Serializable;

public interface OnRoadEvent extends Comparable<OnRoadEvent>, Serializable {
	
	/**
	 * On what distance the event was
	 */
	public int onWhatDistance();
}
