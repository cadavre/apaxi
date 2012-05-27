package pro.jazzy.paxi;

import java.util.ArrayList;

import pro.jazzy.paxi.entity.Member;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MembersAdapter extends ArrayAdapter {

	Member[] membersList;

	public MembersAdapter(Context context, String[] simpleValues,
			Member[] membersList) {
		super(context, R.layout.member_element, R.id.tvName, simpleValues);
		this.membersList = membersList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View returnView = super.getView(position, convertView, parent);

		ImageView ivAvatar = (ImageView) returnView.findViewById(R.id.ivAvatar);
		TextView tvName = (TextView) returnView.findViewById(R.id.tvName);
		TextView tvCounter = (TextView) returnView.findViewById(R.id.tvCounter);

		ivAvatar.setImageURI(Uri.parse(membersList[position].getPhotoUri()));
		tvCounter
				.setText((int) Math.floor(membersList[position].getDistance() / 1000)
						+ "km");

		return returnView;
	}

}
