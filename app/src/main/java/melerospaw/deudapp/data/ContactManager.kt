package melerospaw.deudapp.data

import android.content.Context
import android.provider.ContactsContract
import melerospaw.deudapp.modelo.Contact
import melerospaw.deudapp.modelo.Persona
import java.text.Normalizer
import java.util.*

class ContactManager {

    companion object {

        @JvmStatic
        fun obtainContacts(context: Context): List<Contact> {
            val contacts = LinkedList<Contact>()
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val photoThumbnailUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                if (name != null) {
                    contacts.add(Contact(name, photoThumbnailUri, photoUri))
                }
            }
            cursor.close()

            return contacts
        }

        @JvmStatic
        fun eliminarRepetidos(contactos: MutableList<Contact>) {
            for (i in 0 until contactos.size) {
                for (j in contactos.size - 1 downTo i + 1) {
                    if (contactos[i].name == contactos[j].name) {
                        contactos.remove(contactos[i])
                        break
                    }
                }
            }
        }

        @JvmStatic
        fun ordenar(contactos: MutableList<Contact>) {
            contactos.sortBy { Normalizer.normalize(it.name, Normalizer.Form.NFD).toLowerCase() }
        }

        @JvmStatic
        fun parsePersonas(personas: List<Persona>): List<Contact> {
            val contacts: MutableList<Contact> = ArrayList()
            personas.mapTo(contacts) { Contact(it.nombre, null, it.imagen) }
            return contacts
        }
    }
}