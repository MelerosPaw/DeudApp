package melerospaw.deudapp.utils

import melerospaw.deudapp.modelo.Entidad
import java.util.*

class EntidadesUtil {

    companion object {
        @JvmStatic
        fun hayEntidadesRepetidas(entidades: List<Entidad>): Boolean {
            for (i in 0 until entidades.size) {
                val concepto = entidades[i].concepto
                for (j in i + 1 until entidades.size) {
                    if (concepto == entidades[j].concepto) {
                        return true
                    }
                }
            }

            return false
        }

        @JvmStatic
        fun getEntidades(entidades: List<Entidad>) =
                entidades.filter { it.estaCompleta() }

        @JvmStatic
        fun hayEntidadesIncompletas(entidades: List<Entidad>) =
                entidades.any { (it.concepto == null || it.concepto.isBlank()) || it.cantidad == 0.00F }

        @JvmStatic
        fun hayDelTipo(entidades: List<Entidad>, @Entidad.TipoEntidad tipo: Int) =
                entidades.any { it.tipoEntidad == tipo && it.estaDefinida() }

        @JvmStatic
        fun getRepetidas(entidades: List<Entidad>): List<Entidad> {
            val repetidas = LinkedList<Entidad>()
            (0 until entidades.size).forEach {
                val concepto = entidades[it].concepto
                for (j in it + 1 until entidades.size) {
                    if (concepto == entidades[j].concepto && repetidas.none { it.concepto == concepto }) {
                        repetidas.add(entidades[it])
                    }
                }
            }

            return repetidas
        }

        @JvmStatic
        fun getRepetidos(entidades: List<Entidad>) = getRepetidas(entidades).map { it.concepto }

        @JvmStatic
        fun getIds(entidades: List<Entidad>) = entidades.map { it.id } as ArrayList
    }
}