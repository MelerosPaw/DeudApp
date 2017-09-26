package melerospaw.deudapp.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

public class TypefacesUtils {

    private TypefacesUtils(){}

    private static final Hashtable<String, Typeface> cache = new Hashtable<>();

    /**Devuelve una fuente de la carpeta <i>app/assets</i> de nuestro proyecto
     * y la guarda en una <i>Hashtable</i>. Para devolverla de ahí la próxima vez sin causar un
     * error ni consumir más memoria de la cuenta.
     * @param context Context desde donde estamos solicitando la tipografía.
     * @param fontName Nombre de la fuente, que debe estar almacenada en <i>assets</i>. Si está
     *                 dentro de una carpeta en <i>assets</i>, el nombre debe indicar la ruta. P.e.,
     *                 si está en <i>assets/melerospaw.deudapp.fonts</i>, este parámetro tiene que valer "melerospaw.deudapp.fonts/nombreFuente".
     * @return Devuelve el objeto Typeface con la tipografía solicitada*/
    public static Typeface get(Context context, String fontName) {
        synchronized (cache) {
            if (!cache.containsKey(fontName)) {
                try {
                    Typeface t = Typeface.createFromAsset(context.getAssets(),
                            fontName);
                    cache.put(fontName, t);
                } catch (Exception e) {
                    System.out.println("Could not get typeface '" + fontName
                            + "' because " + e.getMessage());
                    return null;

                }
            }
            return cache.get(fontName);
        }
    }
}
