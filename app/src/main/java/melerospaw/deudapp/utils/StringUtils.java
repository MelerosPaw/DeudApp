package melerospaw.deudapp.utils;

import java.util.List;

public class StringUtils {

    private StringUtils(){}

    //Devuelve true si "busqueda" se encuentra en "elementos"
    public static boolean containsIgnoreCase(List<String> elementos, String busqueda) {
        for (int i = 0; i < elementos.size(); i++)
            if (elementos.get(i).equalsIgnoreCase(busqueda))
                return true;
        return false;
    }


    //Devuelve un boolean indicando si la cadena es un número o no
    public static boolean isNumero(String valor) {
        try {
            Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    //Devuelve un Integer a partir de una cadena
    public static Integer getInteger(String valor) {
        if (!valor.contains(".") && !valor.contains(","))
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                return null;
            }
        else
            return (int) Float.parseFloat(valor);
    }

    /**
     * Indica si una cadena se puede transformar a <i>int</i> o <i>float</i> según si tiene o no
     * decimales. En caso contrario, indicará que es un <i>string</i>.
     *
     * @param cadena Cadena que queremos comprobar.
     * @return Devuelve una cadena que valdrá "integer" si es esConvertible en <i>int</i>; <i>float</i>
     * si es esConvertible en <i>float</i> o <i>string</i> si no es esConvertible en ninguno de los dos.
     */
    public static String esConvertible(String cadena) {

        if (cadena.isEmpty()) {
            return "string";
        }

        String resultado;
        try {
            Integer.parseInt(cadena);
            resultado = "integer";
        } catch (NumberFormatException e1) {
            try {
                cadena = cadena.replaceAll(",", ".");
                Float.parseFloat(cadena);
                resultado = "float";
            } catch (NumberFormatException e2) {
                resultado = "string";
            }
        }

        return resultado;
    }

    /**
     * Comprueba si la cadena recibida está vacía o no.
     *
     * @param cadena La cadena que queremos comprobar
     * @return <i>true</i>* si la cadena está vacía
     */
    public static boolean isCadenaVacia(String cadena) {
        return cadena == null || cadena.trim().length() == 0;
    }


    /**
     * Comprueba si las cadenas recibidas están vacías o no.
     *
     * @param cadenas Las cadenas que queremos comprobar.
     * @return <i>true</i>* si alguna de las cadenas está vacía
     */
    public static boolean isCadenaVacia(String... cadenas) {
        for (String cadena : cadenas) {
            if (cadena.length() == 0)
                return true;
        }
        return false;
    }


    /**
     * Elimimina los espacios de un grupo de cadenas
     *
     * @param cadenas Array con las cadenas que queremos eliminarVarios
     * @return Devuelve las mismas cadenas pero sin espacios. Si no había espacios, las devuelve
     * igualmente
     */
    public static String[] eliminarEspacios(String[] cadenas) {
        for (int i = 0; i < cadenas.length; i++) {
            cadenas[i] = cadenas[i].replaceAll(" ", "");
        }

        return cadenas;
    }

    /**
     * Reemplaza todas las comas por puntos para la conversión a <i>Float</i> o a <i>Double</i>.
     *
     * @param cadena Cadena que queremos modificar.
     */
    public static String prepararDecimal(String cadena) {
        return cadena.replaceAll(",", ".");
    }
}
