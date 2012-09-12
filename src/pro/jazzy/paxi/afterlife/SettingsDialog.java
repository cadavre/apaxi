
package pro.jazzy.paxi.afterlife;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;

public class SettingsDialog extends Dialog {

    private static final String TAG = "Paxi Dialog";

    private int layout;

    public SettingsDialog(Context context, int theme, int layout) {

        super(context, theme);
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
