package melerospaw.deudapp.utils

import melerospaw.deudapp.iu.vo.EntidadVO
import melerospaw.deudapp.modelo.Entidad
import java.util.*

class EntidadesUtil {

    companion object {

        // Una deuda está repetida cuando es el mismo concepto para el mismo día
        @JvmStatic
        fun hayEntidadesRepetidas(entidades: List<EntidadVO>): Boolean {
            for (i in 0 until entidades.size) {
                val entidad: Entidad = entidades[i].entidad
                for (j in i + 1 until entidades.size) {
                    if (entidad.concepto == entidades[j].entidad.concepto
                            && entidad.esMismoDia(entidades[j].entidad)) {
                        return true
                    }
                }
            }

            return false
        }

        @JvmStatic
        fun getEntidadesVO(entidades: List<EntidadVO>) =
                entidades.filter { it.entidad.estaCompleta() }

        @JvmStatic
        fun getEntidades(entidades: List<EntidadVO>) =
                entidades.filter { it.entidad.estaCompleta() }.map { it.entidad }

        @JvmStatic
        fun hayEntidadesIncompletas(entidades: List<EntidadVO>) =
                entidades.any { (it.entidad.concepto == null || it.entidad.concepto.isBlank())
                        || it.entidad.cantidad == 0.00F }

        @JvmStatic
        fun hayDelTipo(entidades: List<EntidadVO>, @Entidad.TipoEntidad tipo: Int) =
                entidades.any { it.entidad.tipoEntidad == tipo && it.entidad.estaDefinida() }

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

        @JvmStatic
        fun toEntidadVOList(entidades: List<Entidad>) = entidades.map { EntidadVO(it) }

        @JvmStatic
        fun hayEntidadesGrupales(entidades: List<EntidadVO>) = entidades.any{ it.esGrupal }

        @JvmStatic
        fun repartirEntidadesGrupales(entidades: List<EntidadVO>, cantidadDeudores: Int) {
            entidades.forEach {
                if (it.esGrupal) {
                    it.entidad.cantidad /= cantidadDeudores
                }
            }
        }
    }
}