package pro.jazzy.paxi.entity.events;

public interface OnRoadEvent extends Comparable<OnRoadEvent> {
	
	/**
	 * On what distance the event was
	 */
	public int onWhatDistance();
}
