package melerospaw.deudapp.iu.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import melerospaw.deudapp.R
import melerospaw.deudapp.modelo.Contact
import melerospaw.deudapp.modelo.Persona
import melerospaw.deudapp.utils.TextDrawableManager
import java.util.*


class AutocompleteAdapter(private val mContext: Context,
                          private val layout: Int,
                          private val contactos: List<Contact>) : BaseAdapter(), Filterable {

    private val suggestions: MutableList<Contact>
    private val filter: Filter = FiltroContactos()


    init {
        suggestions = LinkedList(contactos)
    }

    override fun getFilter() = filter

    override fun getItem(p0: Int) : Contact = suggestions[p0]

    override fun getItemId(p0: Int) = 0L

    override fun getCount() = suggestions.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(layout, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        holder.bindView(suggestions[position])

        return view
    }


    inner class ViewHolder(private val itemView: View) {

        private var ivFoto: ImageView? = null
        private var tvNombre: TextView? = null

        fun bindView(contacto: Contact) {
            ivFoto = itemView.findViewById(R.id.iv_foto)
            tvNombre = itemView.findViewById(R.id.tv_nombre)

            if (TextUtils.isEmpty(contacto.photoThumbnailUri)) {
                if (contacto.color == -1) {
                    contacto.color = ColorGenerator.MATERIAL.randomColor
                }
                ivFoto?.setImageDrawable(TextDrawableManager.createDrawable(contacto.name.first(),
                        contacto.color))
            } else {
                Glide.with(itemView.context)
                        .load(contacto.photoThumbnailUri)
                        .into(ivFoto)
            }

            tvNombre?.text = contacto.name
        }
    }

    inner class FiltroContactos : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            suggestions.clear()

            if (constraint != null) {
                suggestions.addAll(contactos.filter { it.name.toLowerCase().contains(constraint.toString().toLowerCase()) })
            }

            val results = FilterResults()
            results.values = suggestions
            results.count = suggestions.size

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            val persona = resultValue as Contact
            return persona.name
        }
    }
}