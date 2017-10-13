package melerospaw.deudapp.utils;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

public class ScreenUtils {

    private ScreenUtils() {
    }

    // Ajusta el ancho de la ventana. Si el ancho del dialog en modo wrap_content es más grande que
    // la mitad de la pantalla, el ancho se pone a esa cantidad + la mitad de la parte que queda
    // libre de la pantalla. En cualquier otro caso, se pone al ancho de la pantalla menos 16dp por cada lado.
    public static void ajustarAncho(DialogFragment dialogFragment) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialogFragment.getDialog().getWindow();
        lp.copyFrom(window.getAttributes());

        int wrapContentWidth, screenWidth, halfScreen, finalWidth;

        window.getDecorView().measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        wrapContentWidth = window.getDecorView().getMeasuredWidth();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        dialogFragment.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        halfScreen = screenWidth / 2;

        if (screenWidth < wrapContentWidth || halfScreen > wrapContentWidth) {
            finalWidth = screenWidth - (int) dialogFragment.getContext().getResources().getDisplayMetrics().density * 16;
        } else {
            finalWidth = wrapContentWidth + (screenWidth - wrapContentWidth) / 2;
        }

        lp.width = finalWidth;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }

    /**
     * Pone la altura y anchura del DialogFragment a MATCH_PARENT. Hay que llamar a este método en
     * el {@link DialogFragment#onStart()} del DialogFragment para que funcione.
     *
     * @param dialogFragment        El DialogFragment al que se le va a aplicar el cambio.
     * @param transparentBackground Indica si queremos que el elemento raíz del layout sea
     *                              transparente o no.
     */
    public static void pantallaCompleta(DialogFragment dialogFragment, boolean transparentBackground) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialogFragment.getDialog().getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        if (transparentBackground) {
            dialogFragment.getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

    }
}
