package melerospaw.deudapp.data

import android.content.Context
import android.provider.ContactsContract
import melerospaw.deudapp.modelo.Contact
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
                if (name != null) {
                    contacts.add(Contact(name, photoThumbnailUri))
                }
            }

            return contacts
        }

        @JvmStatic fun getContactNames(context: Context): List<String> = obtainContacts(context).map { it.name }
    }
}