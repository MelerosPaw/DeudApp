package melerospaw.deudapp.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Juan José Melero on 17/01/2016.
 */
public class TecladoUtils {

    public static void ocultarTeclado(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }


    public static void mostrarTeclado(final View view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                view.requestFocus();
                inputMethodManager.showSoftInput(view, 0);
            }
        }, 50);
    }
}
