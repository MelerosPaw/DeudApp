package melerospaw.deudapp.iu

import melerospaw.deudapp.data.ContactManager
import melerospaw.deudapp.modelo.Persona

fun main(args: Array<String>) {
    var list = mutableListOf(Persona("San", 2), Persona("Manolo", 1), Persona("Rafael", 0),
            Persona("Juan", 1), Persona("Quique", 2), Persona("Rafael", 3))
    ContactManager.eliminarRepetidos(list)
    ContactManager.ordenar(list)
    list.forEach { println(it.nombre) }
}
