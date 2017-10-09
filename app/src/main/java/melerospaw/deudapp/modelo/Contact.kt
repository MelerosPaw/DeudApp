package melerospaw.deudapp.modelo

data class Contact(var name: String, val photoThumbnailUri: String?, var photoUri: String?, var color: Int = -1) {

    constructor() : this("", "", "")

}
