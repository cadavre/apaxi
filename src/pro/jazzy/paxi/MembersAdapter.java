
package pro.jazzy.paxi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import pro.jazzy.paxi.entity.Member;
import pro.jazzy.paxi.entity.Route;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MembersAdapter extends ArrayAdapter<String> {

    private static final String TAG = "Paxi";

    private ArrayList<Member> membersList;

    Typeface fontface;

    Route routeInstance;

    HashMap<Long, Float> summarizedIds;

    private float divider;

    private String unit;

    public MembersAdapter(Context context, String[] simpleValues, ArrayList<Member> membersList,
            Route route, HashMap<Long, Float> summarized, int metrics) {

        super(context, R.layout.member_element, R.id.tvName, simpleValues);

        this.fontface = Typeface.createFromAsset(context.getAssets(), "fonts/UbuntuM.ttf");
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

        if (this.getCount() % 2 == 0) {
            returnView.setBackgroundResource((position % 2 == 0) ? R.drawable.list_zebra_light
                    : R.drawable.list_zebra_dark);
        } else {
            returnView.setBackgroundResource((position % 2 == 0) ? R.drawable.list_zebra_dark
                    : R.drawable.list_zebra_light);
        }

        ImageView ivAvatar = (ImageView) returnView.findViewById(R.id.ivAvatar);
        TextView tvName = (TextView) returnView.findViewById(R.id.tvName);
        TextView tvCounter = (TextView) returnView.findViewById(R.id.tvCounter);

        if (membersList.get(position).getAvatarUri() != null) {
            ivAvatar.setImageURI(Uri.parse(membersList.get(position).getAvatarUri()));
        }

        if (!summarizedIds.containsKey(membersList.get(position).getId())) {
            tvCounter.setText((int) FloatMath.floor((routeInstance.getDistance() - membersList.get(
                    position).getDistance())
                    / divider)
                    + " " + unit);
        } else {
            tvName.setTextColor(getContext().getResources().getColor(R.color.light_gray));
            if (!membersList.get(position).getAvatarUri()
                    .equals(MainActivity.DEFAULT_MEMBER_AVATAR_URI)) {
                try {
                    ivAvatar.setImageBitmap(toGrayscale(MediaStore.Images.Media.getBitmap(
                            getContext().getContentResolver(),
                            Uri.parse(membersList.get(position).getAvatarUri()))));

                } catch (FileNotFoundException e) {
                    // actually do nothing
                } catch (IOException e) {
                    // actually do nothing
                }
            } else {
                ivAvatar.setImageResource(R.drawable.passenger);
            }

            String currency = Currency.getInstance(Locale.getDefault()).getSymbol();
            DecimalFormat dfTwoDigits = new DecimalFormat("#.##");
            String value = dfTwoDigits.format(summarizedIds.get(getItemId(position)));
            tvCounter.setText(value + " " + currency);
        }

        MainActivity.applyGlobalTypeface((ViewGroup) returnView, this.fontface);

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

    public Bitmap toGrayscale(Bitmap original) {

        int width, height;
        height = original.getHeight();
        width = original.getWidth();

        Bitmap grayscaled = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(grayscaled);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        // ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        LightingColorFilter l = new LightingColorFilter(0xffffffff, 0x00555555);
        // paint.setColorFilter(f);
        // c.drawBitmap(original, 0, 0, paint);
        paint.setColorFilter(l);
        c.drawBitmap(original, 0, 0, paint);

        return grayscaled;
    }

}
