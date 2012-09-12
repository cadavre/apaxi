
package pro.jazzy.paxi.afterlife;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ButtonsDialog extends Dialog {

    private static final String TAG = "Paxi Dialog";

    private int layout;

    private int top;

    private int imgResource;

    OnAcceptListener onAcceptListener = null;

    public interface OnAcceptListener {

        public abstract void onAccept(Dialog dialog);
    }

    public ButtonsDialog(Context context, int top, int imgResource) {

        super(context, R.style.DialogButtons);
        this.layout = R.layout.popup_button;
        this.top = top;
        this.imgResource = imgResource;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);

        if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
            // Tapped outside so we finish the activity
            this.dismiss();
        }
        return super.dispatchTouchEvent(ev);
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

        // set dialog width fill_parent
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(this.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.FILL_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);

        llButtonsContainer.setLayoutParams(llNewParams);

        ImageView btnPopup = (ImageView) findViewById(R.id.btnPopup);
        btnPopup.setImageResource(imgResource);
        btnPopup.setOnClickListener(new View.OnClickListener() {

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
