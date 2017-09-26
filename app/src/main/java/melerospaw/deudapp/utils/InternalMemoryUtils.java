package melerospaw.deudapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Juan José Melero on 26/05/2015.
 */
public class InternalMemoryUtils {

    /**Devuelve un <i>Bitmap</i> a partir de una imagen almacenada en <i>data/data/nuestraApp/files</i>.
     *  @param context
     *  @param rutaImagen Donde se encuentra la imagen a partir de <i>data/data/nuestraApp/files</i>.
     *  @return Devuelve el <i>Bitmap</i> de la imagen indicada o <i>null</i> si no existe la imagen.*/
    public static Bitmap obtenerImagenBitmap(Context context, String rutaImagen){
        try{
            FileInputStream fileInputStream = new FileInputStream(context.getFilesDir() + "/" + rutaImagen);
            return BitmapFactory.decodeStream(fileInputStream);
        }catch(IOException e){
            System.out.println("No se ha podido obtener la imagen en " + rutaImagen);
            return null;
        }
    }


    /**Guarda una imagen en la memoria interna del dispositivo, en <i>data/data/app/files</i>,
     * comprobando antes que no exista ya una igual.
     * @param context
     * @param bitmap Objeto <i>Bitmap</i> de la imagen. Sus medidas deben ser de 142px x 142px
     * @param nombreImagen Nombre que que tenga la imagen en <i>data/data/nuestraApp/files</i>.
     *                   No debe contener espacios.
     * @return Devuelve un booleano que indica si la imagen se ha guardado correctamente o no.*/
    public static boolean guardarImagen(Context context, Bitmap bitmap, String nombreImagen){

        if (!isArchivoRepetido(context, nombreImagen) && bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            try {
                FileOutputStream fileOutputStream = context.openFileOutput(nombreImagen, Context.MODE_PRIVATE);
                fileOutputStream.write(byteArray);
                fileOutputStream.close();
            } catch (IOException e) {
                System.out.println("No se ha podido guardar la imagen " + nombreImagen);
                return false;
            }

            return true;
        }

        System.out.println("No se ha podido guardar la imagen " + nombreImagen + ". " +
                "Puede deberse a que la imagen sea nula o a que ya hay guardada una" +
                " con el mismo nombre.");
        return false;
    }


    /**Devuelve un booleano indicando si el archivo ya existe en el directorio <i>data/data/app/files</i>
     * @param context
     * @param nombreArchivo El nombre del archivo en la carpeta <i>/data/data/nuestraApp/files</i>, empezando sin barra. Por ejemplo:
     *                   <i>imagen.jpg</i>.
     * @return boolean indicando si existe o no el archivo.*/
    private static boolean isArchivoRepetido(Context context, String nombreArchivo) {
        File file = new File(context.getFilesDir().getPath() + "/" + nombreArchivo);
        return file.exists()? true : false;
    }


    /**Obtiene una imagen en forma de archivo <i>Bitmap</i> desde la carpeta assets
     * @param context
     * @param nommbreImagen Nombre de la imagen en la carpeta assets, empezando sin barra. Por ejemplo,
     *                      <i>imagen.jpg</i>.
     * @return La imagen convertida en <i>Bitmap</i>*/
    public static Bitmap obtenerImagenDesdeAssets(Context context, String nommbreImagen){
        try {
            return BitmapFactory.decodeStream(context.getAssets().open(nommbreImagen));
        }catch(IOException e){
            System.out.println("No se ha podido encontrar la imagen en assets");
            e.printStackTrace();
            return null;
        }
    }


    /**Elimina un archivo de <i>data/data/nuestraApp/files</i>
     * @param context
     * @param nombre Nombre del archivo que queremos borrar
     * @return Devuelve un booleano indicando si se ha borrado el archivo o no. Si no se ha borrado
     * es porque la ruta está mal indicada*/
    public static boolean eliminarArchivoInterno(Context context, String nombre){
        return new File(context.getFilesDir().getPath() + "/" + nombre).delete();
    }


    /**Convierte la imagen a un tamaño de 5cm (142px x 142px).
     * @param bitmap <i>Bitmap</i> con la imagen que queremos redimensionar.
     * @returns Devuelve un bitmap escalado a 142px x 142px*/
    public static Bitmap prepararBitmap(Bitmap bitmap){
        Bitmap bitmapRecortado = null;

        // Primero la recorta el lado mas largo para que tenga un ratio de aspecto 1:1
        if (bitmap.getWidth() != bitmap.getHeight()){
            int anchura = bitmap.getWidth();
            int altura = bitmap.getHeight();
            if (anchura < altura){
                int alturaSobrante = altura - anchura;
                int y = alturaSobrante / 2;
                bitmapRecortado = Bitmap.createBitmap(bitmap, 0, y, anchura, anchura);
            } else if (altura < anchura){
                int anchuraSobrante = anchura - altura;
                int x = anchuraSobrante / 2;
                bitmapRecortado = Bitmap.createBitmap(bitmap, x, 0, altura, altura);
            }
        } else
            bitmapRecortado = bitmap;

        //Escala la imagen a 142px
        Bitmap bitmapescalado = Bitmap.createScaledBitmap(bitmapRecortado, 142, 142, false);

        return bitmapescalado;
    }
}
