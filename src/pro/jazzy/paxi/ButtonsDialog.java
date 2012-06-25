
package pro.jazzy.paxi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ButtonsDialog extends Dialog {

    private static final String TAG = "Paxi Dialog";

    private int layout;

    private int top;

    public ButtonsDialog(Context context, int theme, int layout, int top) {

        super(context, theme);
        this.layout = layout;
        this.top = top;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(layout);
        getWindow().setGravity(Gravity.TOP);
        LinearLayout llButtonsContainer = (LinearLayout) findViewById(R.id.llButtonsContainer);

        // set proper distance between buttons and top
        LinearLayout.LayoutParams llCurrentParams = (LayoutParams) llButtonsContainer
                .getLayoutParams();
        LinearLayout.LayoutParams llNewParams = new LinearLayout.LayoutParams(llCurrentParams);
        llNewParams.setMargins(0, top, 0, 0);

        // set dim background value
        WindowManager.LayoutParams dimAttrs = getWindow().getAttributes();
        dimAttrs.dimAmount = 0.5f;
        getWindow().setAttributes(dimAttrs);

        llButtonsContainer.setLayoutParams(llNewParams);
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

}
