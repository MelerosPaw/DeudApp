package melerospaw.deudapp.data

import android.content.Context
import android.media.ThumbnailUtils
import android.provider.ContactsContract
import com.amulyakhare.textdrawable.TextDrawable
import melerospaw.deudapp.modelo.Contact
import melerospaw.deudapp.modelo.Persona
import melerospaw.deudapp.utils.TextDrawableManager
import java.text.Normalizer
import java.util.*

class ContactManager {

    companion object {

        @JvmStatic fun obtainContacts(context: Context): List<Contact> {
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

            return contacts
        }

        @JvmStatic fun eliminarRepetidos(personas: MutableList<Persona>) {
            for (i in 0 until personas.size) {
                for (j in personas.size - 1 downTo i + 1) {
                    if (personas[i] == personas[j]) {
                        personas.remove(personas[i])
                        break
                    }
                }
            }
        }

        @JvmStatic fun ordenar(personas: MutableList<Persona>) {
            personas.sortBy { Normalizer.normalize(it.nombre, Normalizer.Form.NFD).toLowerCase() }
        }
    }
}