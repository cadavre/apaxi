package pro.jazzy.paxi.entity.events;


public abstract class OnRoadEventBasicImpl implements OnRoadEvent {
	public int compareTo(OnRoadEvent arg0) {
		return this.onWhatDistance() - arg0.onWhatDistance();
	}
}