
package pro.jazzy.paxi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import pro.jazzy.paxi.entity.Payment;
import pro.jazzy.paxi.entity.Route;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PaymentsAdapter extends ArrayAdapter<Payment> {

    private static final String TAG = "Paxi";

    private ArrayList<Payment> paymentsList;

    Route routeInstance;

    private float divider;

    private String unit;

    public PaymentsAdapter(Context context, ArrayList<Payment> paymentsList, Route route,
            int metrics) {

        super(context, R.layout.payment_element, R.id.tvDistance, paymentsList);
        this.paymentsList = paymentsList;
        this.routeInstance = route;

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

        TextView tvAmount = (TextView) returnView.findViewById(R.id.tvAmount);
        TextView tvDistance = (TextView) returnView.findViewById(R.id.tvDistance);

        String currency = Currency.getInstance(Locale.getDefault()).getSymbol();
        DecimalFormat dfTwoDigits = new DecimalFormat("#.##");
        String value = dfTwoDigits.format(paymentsList.get(position).getAmount());
        tvAmount.setText(value + " " + currency);

        tvDistance.setText("at "
                + (int) Math.floor((routeInstance.getDistance() - paymentsList.get(position)
                        .getDistance()) / divider) + " " + unit);

        return returnView;
    }

    @Override
    public boolean hasStableIds() {

        return true;
    }

    @Override
    public long getItemId(int position) {

        return this.paymentsList.get(position).getId();
    }

}
