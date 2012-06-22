
package pro.jazzy.paxi.entity.events;

/**
 * @author Zachi
 */
public interface OnRoadEvent extends Comparable<OnRoadEvent> {

    /**
     * On what distance the event happened
     */
    public int onWhatDistance();
}
