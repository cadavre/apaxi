
package pro.jazzy.paxi;

import pro.jazzy.paxi.entity.Member;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MembersAdapter extends ArrayAdapter<String> {
    
    private static final String TAG = "Paxi";

    private Member[] membersList;

    private int metrics;

    // metrics idenfitiers
    static final int KILOMETERS = 1;

    static final int MILES = 2;

    public MembersAdapter(Context context, String[] simpleValues, Member[] membersList, int metrics) {

        super(context, R.layout.member_element, R.id.tvName, simpleValues);
        this.membersList = membersList;
        this.metrics = metrics;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View returnView = super.getView(position, convertView, parent);

        ImageView ivAvatar = (ImageView) returnView.findViewById(R.id.ivAvatar);
        // TextView tvName = (TextView) returnView.findViewById(R.id.tvName);
        TextView tvCounter = (TextView) returnView.findViewById(R.id.tvCounter);

        if (membersList[position].getPhotoUri() != null) {
            ivAvatar.setImageURI(Uri.parse(membersList[position].getPhotoUri()));
        } else {
            // TODO set default hopek
        }

        double divider;
        String unit = "m";
        switch (this.metrics) {
            case MILES:
                divider = 1609.344;
                unit = "m";
                break;
            case KILOMETERS:
            default:
                divider = 1000.0;
                unit = "km";
                break;
        }
        tvCounter.setText((int) Math.floor(membersList[position].getDistance() / divider) + " "
                + unit);

        return returnView;
    }

}
