package melerospaw.deudapp.utils

enum class Currency(val signo: String, val posicion: Position) {
    EURO("€", Position.DETRAS),
    PESO("$", Position.DELANTE),
    PESO_FILIPINO("₱", Position.DELANTE),
    BOLIVAR("Bs.S.", Position.DELANTE),
    ESCUDO("Esc.", Position.DELANTE),
    UNIDAD_DE_FOMENTO("UF", Position.DELANTE),
    COLON("₡", Position.DELANTE),
    LIBRA("£", Position.DELANTE),
    QUETZAL("Q", Position.DELANTE),
    LEMPIRA("L", Position.DELANTE),
    CORDOBA("C$", Position.DELANTE);

    companion object {
        fun getCurrencyBySign(signo: String) = values().first { it.signo == signo }
    }

    enum class Position {
        DELANTE, DETRAS
    }
}