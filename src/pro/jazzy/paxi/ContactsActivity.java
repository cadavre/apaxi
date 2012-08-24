
package pro.jazzy.paxi;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ContactsActivity extends Activity implements OnItemClickListener {

    private static final String TAG = "Paxi";

    private static final boolean SHOW_HIDDEN = false;

    Typeface fontface;

    ListView lvContactsList;

    Cursor contactsCursor;

    ArrayList<Long> alreadyOnList;

    int membersCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);

        this.fontface = Typeface.createFromAsset(getAssets(), "fonts/" + MainActivity.FONT_NAME);
        MainActivity.applyGlobalTypeface((RelativeLayout) findViewById(R.id.rlContactsContainer),
                fontface);

        alreadyOnList = new ArrayList<Long>();

        // set options
        this.membersCount = getIntent().getExtras().getInt("membersCount", 0);
        this.alreadyOnList = (ArrayList<Long>) getIntent().getExtras().get("alreadyOnList");

        lvContactsList = (ListView) findViewById(R.id.lvContactsList);
        getContacts();
        final TextView tvLetterHint = (TextView) findViewById(R.id.tvLetterHint);

        String[] fields = new String[] { ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.PHOTO_THUMBNAIL_URI };
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.contact_element,
                contactsCursor, fields, new int[] { R.id.tvName, R.id.ivAvatar }) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View returnView = super.getView(position, convertView, parent);
                returnView.setBackgroundResource((position % 2 == 0) ? R.drawable.list_zebra_dark
                        : R.drawable.list_zebra_light);

                MainActivity.applyGlobalTypeface((ViewGroup) returnView, fontface);

                return returnView;
            }
        };
        lvContactsList.setAdapter(adapter);
        lvContactsList.setOnItemClickListener(this);
        lvContactsList.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (scrollState == SCROLL_STATE_IDLE) {
                    tvLetterHint.setVisibility(View.INVISIBLE);
                } else {
                    tvLetterHint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {

                String firstLetter;
                try {
                    MatrixCursor cursor = (MatrixCursor) adapter.getItem(firstVisibleItem + 1);
                    firstLetter = cursor.getString(1).substring(0, 1);
                } catch (Exception e) {
                    firstLetter = "P";
                }
                tvLetterHint.setText(firstLetter.toUpperCase());
            }
        });

        adapter.setFilterQueryProvider(new FilterQueryProvider() {

            @Override
            public Cursor runQuery(CharSequence constraint) {

                return getContacts(constraint.toString());
            }
        });

        // Button btnVoiceSearch = (Button) findViewById(R.id.btnVoiceSearch);
        // btnVoiceSearch.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        //
        // Intent rcgnzer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // rcgnzer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        // RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // rcgnzer.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        // startActivityForResult(rcgnzer, 777);
        // }
        // });

        // EditText etFiler = (EditText) findViewById(R.id.etFilter);
        // etFiler.addTextChangedListener(new TextWatcher() {
        //
        // @Override
        // public void onTextChanged(CharSequence s, int start, int before, int count) {
        //
        // adapter.getFilter().filter(s);
        // }
        //
        // @Override
        // public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //
        // }
        //
        // @Override
        // public void afterTextChanged(Editable s) {
        //
        // }
        // });
        //
        // etFiler.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        //
        // ((EditText) v).setText("");
        // }
        // });

        ImageView btnDoneContacts = (ImageView) findViewById(R.id.btnDoneContacts);
        btnDoneContacts.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }

    // @Override
    // protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //
    // ArrayList<String> results = (ArrayList<String>) data.getExtras().get(
    // RecognizerIntent.EXTRA_RESULTS);
    // EditText etFilter = (EditText) findViewById(R.id.etFilter);
    // etFilter.setText(results.get(0));
    // }

    protected void getContacts() {

        this.contactsCursor = getContacts(alreadyOnList, "");
    }

    protected Cursor getContacts(String filterText) {

        return getContacts(alreadyOnList, filterText);
    }

    /**
     * Get contacts cursor
     * 
     * @param alreadyOnList ID's of already on list members - so don't show
     */
    private Cursor getContacts(ArrayList<Long> alreadyOnList, String filterText) {

        // prepare general cursor for (non-hidden) users contacts
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI };
        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
                + (SHOW_HIDDEN ? "0" : "1") + "'";
        // if (filterText != null && !filterText.isEmpty()) {
        // selection += " AND " + ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + filterText
        // + "%'";
        // }
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor contactsCursor = managedQuery(uri, projection, selection, selectionArgs, sortOrder);

        String[] columnNames = { ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI };

        MatrixCursor retCursor = new MatrixCursor(columnNames);
        String[] row = new String[3];

        // load driver data place it at first place in cursor
        uri = ContactsContract.Profile.CONTENT_URI;
        projection = new String[] { ContactsContract.Profile._ID,
                ContactsContract.Profile.DISPLAY_NAME, ContactsContract.Profile.PHOTO_THUMBNAIL_URI };
        selection = ContactsContract.Profile.IS_USER_PROFILE;
        // if (filterText != null && !filterText.isEmpty()) {
        // selection += " AND " + ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + filterText
        // + "%'";
        // }
        Cursor profileCursor = managedQuery(uri, projection, selection, selectionArgs, null);

        // drivers profile
        if (profileCursor != null && profileCursor.moveToFirst()) {
            profileCursor.moveToFirst();
            row[0] = profileCursor.getString(0);
            row[1] = profileCursor.getString(1);
            row[2] = (profileCursor.getString(2) == null) ? MainActivity.DEFAULT_AVATAR_URI
                    : profileCursor.getString(2);
        } else {
            row[0] = "-909090";
            row[1] = "Driver";
            row[2] = MainActivity.DEFAULT_AVATAR_URI;
        }
        if (!alreadyOnList.contains(Long.valueOf(row[0]))) {
            retCursor.addRow(row);
        }

        // create "Passenger #%d" at second place
        if (filterText != null && filterText.isEmpty()) {
            int next = (membersCount + 1);
            row[0] = String.format("%d", -999 - membersCount);
            row[1] = "Passenger #" + next;
            row[2] = MainActivity.DEFAULT_AVATAR_URI;
            retCursor.addRow(row);
        }

        // load contacts
        contactsCursor.moveToFirst();
        while (contactsCursor.isAfterLast() == false) {
            row[0] = contactsCursor.getString(0);
            row[1] = contactsCursor.getString(1);
            row[2] = (contactsCursor.getString(2) == null) ? MainActivity.DEFAULT_AVATAR_URI
                    : contactsCursor.getString(2);
            if (!alreadyOnList.contains(Long.valueOf(row[0]))) {
                retCursor.addRow(row);
            }
            contactsCursor.moveToNext();
        }

        return (Cursor) retCursor;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent();
        contactsCursor.moveToFirst();
        while (contactsCursor.isAfterLast() == false) {
            if (Long.parseLong(contactsCursor.getString(0)) == id) {
                intent.putExtra("id", contactsCursor.getString(0));
                intent.putExtra("name", contactsCursor.getString(1));
                String avatar = (contactsCursor.getString(2) == MainActivity.DEFAULT_AVATAR_URI) ? MainActivity.DEFAULT_MEMBER_AVATAR_URI
                        : contactsCursor.getString(2);
                intent.putExtra("photo", avatar);
                break;
            }
            contactsCursor.moveToNext();
        }
        setResult(RESULT_OK, intent);
        finish();
    }
}
