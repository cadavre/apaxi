
package pro.jazzy.paxi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ButtonsDialog extends Dialog {

    private static final String TAG = "Paxi Dialog";

    private int layout;

    private int top;

    OnAcceptListener onAcceptListener = null;

    public interface OnAcceptListener {

        public abstract void onAccept(Dialog dialog);
    }

    public ButtonsDialog(Context context, int top) {

        super(context, R.style.DialogButtons);
        this.layout = R.layout.double_buttons;
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

        Button btnRight = (Button) findViewById(R.id.btnRight);
        btnRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                OnUserAccepted();
            }
        });
    }

    private void OnUserAccepted() {

        if (onAcceptListener != null) {
            onAcceptListener.onAccept(this);
        }
    }

    public void setOnAcceptListener(OnAcceptListener listener) {

        this.onAcceptListener = listener;
    }

}
