package melerospaw.deudapp.iu.dialogs;

import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public abstract class AnywhereDialog extends Dialog {

    private View view;
    private int xPositionOnScreen;
    private int yPositionOnScreen;

    /**
     * A itemView necessary to determine the dialog's position and the listener to get the selected
     * option.
     *
     * @param view  The itemView to set the dialog's position.
     */
    public AnywhereDialog(View view) {
        super(view.getContext());
        this.view = view;
    }

    /**
     * Shows a dialog with the folder options at the specified positions sent as parameter.
     */
    public void showDialog() {
        setWindowProperties();
        show();
    }


    /**
     * Prepares the window to display the dialog.
     */
    private void setWindowProperties() {
        setDialogPosition();
        setLayoutProperties();
    }


    /**
     * Locates the dialog in relation to the itemView received.
     */
    private void setDialogPosition() {
        int[] viewLocation = new int[2];
        this.view.getLocationOnScreen(viewLocation);
        this.xPositionOnScreen = viewLocation[0];
        this.yPositionOnScreen = viewLocation[1];
        this.yPositionOnScreen -= this.view.getMeasuredHeight();
    }


    /**
     * Determines the dialog window's properties.
     */
    private void setLayoutProperties() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayout());
        WindowManager.LayoutParams wmlp = this.getWindow().getAttributes();
        wmlp.x = this.xPositionOnScreen;
        wmlp.y = this.yPositionOnScreen;
        wmlp.gravity = Gravity.TOP | Gravity.START;
    }


    /**
     * Must return the layout resource for the dialog.
     *
     * @return The layout resource for the dialog.
     */
    public abstract int getLayout();
}

