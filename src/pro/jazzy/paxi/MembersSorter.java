
package pro.jazzy.paxi;

import java.util.Comparator;
import java.util.HashMap;

import pro.jazzy.paxi.entity.Member;
import android.util.Log;

public class MembersSorter implements Comparator {

    HashMap<Long, Float> summarizedIds;

    public MembersSorter(HashMap<Long, Float> summarizedIds) {

        this.summarizedIds = summarizedIds;
    }

    @Override
    public int compare(Object lhs, Object rhs) {

        if (!summarizedIds.containsKey(((Member) lhs).getId()) && !summarizedIds.containsKey(((Member) rhs).getId())) {
            return 0;
        } else if (summarizedIds.containsKey(((Member) lhs).getId()) && !summarizedIds.containsKey(((Member) rhs).getId())) {
            return 1;
        } else if (!summarizedIds.containsKey(((Member) lhs).getId()) && summarizedIds.containsKey(((Member) rhs).getId())) {
            return -1;
        }
        return 0;
    }

}
