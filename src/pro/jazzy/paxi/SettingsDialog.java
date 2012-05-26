package pro.jazzy.paxi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;

public class SettingsDialog extends Dialog {

	private static final String TAG = "Paxi Dialog";

	private Context context;

	private int layout;

	public SettingsDialog(Context context, int theme, int layout) {
		super(context, theme);
		this.context = context;
		this.layout = layout;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout);
		getWindow().setGravity(Gravity.TOP);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

}
