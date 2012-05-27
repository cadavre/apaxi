package pro.jazzy.paxi.entity.events;

import java.io.Serializable;


public abstract class OnRoadEventBasicImpl implements OnRoadEvent, Serializable {
	public int compareTo(OnRoadEvent arg0) {
		return this.onWhatDistance() - arg0.onWhatDistance();
	}
}
