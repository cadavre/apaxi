
package pro.jazzy.paxi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import pro.jazzy.paxi.entity.Member;
import pro.jazzy.paxi.entity.Route;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MembersAdapter extends ArrayAdapter<String> {

    private static final String TAG = "Paxi";

    private ArrayList<Member> membersList;

    Route routeInstance;

    HashMap<Long, Float> summarizedIds;

    private float divider;

    private String unit;

    public MembersAdapter(Context context, String[] simpleValues, ArrayList<Member> membersList,
            Route route, HashMap<Long, Float> summarized, int metrics) {

        super(context, R.layout.member_element, R.id.tvName, simpleValues);
        this.membersList = membersList;
        this.routeInstance = route;
        this.summarizedIds = summarized;

        switch (metrics) {
            case Route.MILES:
                divider = Route.MILES_DIVIDER;
                unit = "m";
                break;
            case Route.KILOMETERS:
            default:
                divider = Route.KILOMETERS_DIVIDER;
                unit = "km";
                break;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View returnView = super.getView(position, convertView, parent);

        ImageView ivAvatar = (ImageView) returnView.findViewById(R.id.ivAvatar);
        // TextView tvName = (TextView) returnView.findViewById(R.id.tvName);
        TextView tvCounter = (TextView) returnView.findViewById(R.id.tvCounter);

        if (membersList.get(position).getAvatarUri() != null) {
            ivAvatar.setImageURI(Uri.parse(membersList.get(position).getAvatarUri()));
        } else {
            // TODO set default hopek
        }

        if (!summarizedIds.containsKey(membersList.get(position).getId())) {
            tvCounter.setText((int) Math.floor((routeInstance.getDistance() - membersList.get(
                    position).getDistance())
                    / divider)
                    + " " + unit);
        } else {
            returnView.setBackgroundColor(Color.LTGRAY);
            String currency = Currency.getInstance(Locale.getDefault()).getSymbol();
            DecimalFormat dfTwoDigits = new DecimalFormat("#.##");
            String value = dfTwoDigits.format(summarizedIds.get(getItemId(position)));
            tvCounter.setText(value + " " + currency);
        }

        return returnView;
    }

    @Override
    public boolean hasStableIds() {

        return true;
    }

    @Override
    public long getItemId(int position) {

        return this.membersList.get(position).getId();
    }
}
