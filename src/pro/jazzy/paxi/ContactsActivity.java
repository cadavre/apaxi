package pro.jazzy.paxi;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ContactsActivity extends Activity implements OnItemClickListener {

	private static final String TAG = "Paxi";

	static final int PICK_CONTACT_REQUEST = 0;

	private static final boolean showHidden = false;

	ListView lvContactsList;

	int membersCount = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);

		this.membersCount = getIntent().getExtras().getInt("membersCount", 0);

		lvContactsList = (ListView) findViewById(R.id.lvContactsList);

		Cursor cursor = getContacts(true);
		String[] fields = new String[] { ContactsContract.Data.DISPLAY_NAME,
				ContactsContract.Data.PHOTO_THUMBNAIL_URI };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.contact_element, cursor, fields, new int[] {
						R.id.tvName, R.id.ivAvatar });
		lvContactsList.setAdapter(adapter);
		lvContactsList.setOnItemClickListener(this);
	}

	private Cursor getContacts(boolean withMe) {
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.PHOTO_THUMBNAIL_URI };
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
				+ (showHidden ? "0" : "1") + "'";
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		Cursor contactsCursor = managedQuery(uri, projection, selection,
				selectionArgs, sortOrder);

		String[] columnNames = { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.PHOTO_THUMBNAIL_URI };
		MatrixCursor retCursor = new MatrixCursor(columnNames);

		String[] row = new String[3];

		if (withMe) {
			uri = ContactsContract.Profile.CONTENT_URI;
			projection = new String[] { ContactsContract.Profile._ID,
					ContactsContract.Profile.DISPLAY_NAME,
					ContactsContract.Profile.PHOTO_THUMBNAIL_URI };
			selection = ContactsContract.Profile.IS_USER_PROFILE;
			Cursor profileCursor = managedQuery(uri, projection, selection,
					selectionArgs, null);

			profileCursor.moveToFirst();
			row[0] = profileCursor.getString(0);
			row[1] = profileCursor.getString(1);
			row[2] = profileCursor.getString(2);
			retCursor.addRow(row);
		}

		int next = (membersCount + 1);
		row[0] = String.format("%d", 999999 - membersCount);
		row[1] = "Passenger #" + next;
		row[2] = "";
		retCursor.addRow(row);

		contactsCursor.moveToFirst();
		while (contactsCursor.isAfterLast() == false) {
			row[0] = contactsCursor.getString(0);
			row[1] = contactsCursor.getString(1);
			row[2] = contactsCursor.getString(2);
			retCursor.addRow(row);
			contactsCursor.moveToNext();
		}

		return (Cursor) retCursor;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		Intent intent = new Intent();
		Cursor contactsCursor = getContacts(true);
		contactsCursor.moveToFirst();
		while (contactsCursor.isAfterLast() == false) {
			if (Long.parseLong(contactsCursor.getString(0)) == id) {
				intent.putExtra("name", contactsCursor.getString(1));
				intent.putExtra("photo", contactsCursor.getString(2));
				break;
			}
			contactsCursor.moveToNext();
		}
		setResult(RESULT_OK, intent);
		finish();
	}
}
