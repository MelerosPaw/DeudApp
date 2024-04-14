package melerospaw.deudapp.iu.adapters

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import melerospaw.deudapp.modelo.Entidad

class DiffUtilBaseCallback(private val oldList: List<Entidad>, private val newList: List<Entidad>) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].concepto == newList[newItemPosition].concepto &&
                    oldList[oldItemPosition].fecha == newList[newItemPosition].fecha
    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) =
            super.getChangePayload(oldItemPosition, newItemPosition)

}