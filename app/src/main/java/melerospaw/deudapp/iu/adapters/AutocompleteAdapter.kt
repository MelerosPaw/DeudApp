package melerospaw.deudapp.iu.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import melerospaw.deudapp.R
import melerospaw.deudapp.modelo.Persona
import java.util.*


class AutocompleteAdapter(private val mContext: Context,
                          private val layout: Int,
                          private val contactos: List<Persona>) : BaseAdapter(), Filterable {

    private val suggestions: MutableList<Persona>
    private val filter: Filter = FiltroContactos()


    init {
        suggestions = LinkedList(contactos)
    }

    override fun getFilter() = filter

    override fun getItem(p0: Int) : Persona = suggestions[p0]

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

    inner class ViewHolder(val view: View) {

        private var ivFoto: ImageView? = null
        private var tvNombre: TextView? = null

        fun bindView(persona: Persona) {
            ivFoto = view.findViewById(R.id.iv_foto)
            tvNombre = view.findViewById(R.id.tv_nombre)

//            Glide.with(view.context)
//                    .load(contact.photoThumbnailUri)
//                    .into(ivFoto)
            tvNombre?.text = persona.nombre
        }
    }

    inner class FiltroContactos : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            suggestions.clear()

            if (constraint != null) {
                suggestions.addAll(contactos.filter { it.nombre.toLowerCase().startsWith(constraint.toString().toLowerCase()) })
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
            val persona = resultValue as Persona
            return persona.nombre
        }
    }
}