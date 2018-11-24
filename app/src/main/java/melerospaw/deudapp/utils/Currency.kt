package melerospaw.deudapp.utils

import java.lang.IllegalStateException

/**
 * Esta clase está debe contener los mismos valores que los arrays {@code currencies} y
 * {@code currency_values} del archivo <i>arrays.xml</i>.
 */
enum class Currency(val signo: String, val descripcion: String, val caracterDecimal: CaracterDecimal,
                    val posicion: Position) {

    BOLIVAR("Bs.S.", "Bolivares", CaracterDecimal.COMA, Position.DELANTE_CON_ESPACIO),
    COLON("₡", "Colones", CaracterDecimal.COMA, Position.DELANTE_CON_ESPACIO),
    CORDOBA("C$", "Córdobas", CaracterDecimal.COMA, Position.DELANTE_CON_ESPACIO),
    DOLAR("$", "Dólares", CaracterDecimal.PUNTO, Position.DELANTE_SIN_ESPACIO),
    ESCUDO("Esc.", "Escudos", CaracterDecimal.COMA, Position.DELANTE_CON_ESPACIO),
    EURO("€", "Euros", CaracterDecimal.COMA, Position.DETRAS),
    LEMPIRA("L", "Lempiras", CaracterDecimal.COMA, Position.DELANTE_CON_ESPACIO),
    LIBRA("£", "Libras", CaracterDecimal.PUNTO, Position.DELANTE_SIN_ESPACIO),
    PESO("$", "Pesos", CaracterDecimal.COMA, Position.DELANTE_CON_ESPACIO),
    PESO_FILIPINO("₱", "Pesos filipinos", CaracterDecimal.COMA, Position.DELANTE_CON_ESPACIO),
    QUETZAL("Q", "Quetzales", CaracterDecimal.COMA, Position.DELANTE_CON_ESPACIO),
    SOL("S/.", "Soles", CaracterDecimal.COMA, Position.DELANTE_CON_ESPACIO),
    UNIDAD_DE_FOMENTO("UF", "Unidades de Fomento", CaracterDecimal.COMA, Position.DELANTE_CON_ESPACIO);

    val nombreCompleto: String
        get() = "$signo - $descripcion"

    companion object {
        fun getCurrencyByDescription(descripcion: String) =
                values().firstOrNull { it.descripcion == descripcion }
                        ?: throw IllegalStateException("Enum with description \"$descripcion\" " +
                                "could not be found. Make sure this description matches the ones " +
                                "in currency_values array.")
    }

    enum class Position {
        DELANTE_SIN_ESPACIO,
        DELANTE_CON_ESPACIO,
        DETRAS
    }

    enum class CaracterDecimal(val caracter: Char) {
        PUNTO('.'),
        COMA(','),
        APOSTROFE('\'')
    }
}