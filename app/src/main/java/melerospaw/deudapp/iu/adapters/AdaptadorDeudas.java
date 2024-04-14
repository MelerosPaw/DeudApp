package melerospaw.deudapp.iu.adapters;

import android.content.Context;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;
import melerospaw.deudapp.R;
import melerospaw.deudapp.constants.ConstantesGenerales;
import melerospaw.deudapp.iu.widgets.ContextRecyclerView;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.utils.CurrencyUtilKt;
import melerospaw.deudapp.utils.ExtensionFunctionsKt;


public class AdaptadorDeudas extends ContextRecyclerView.Adapter<AdaptadorDeudas.ViewHolder> {

    @IntDef({BACKGROUND_BORRAR, BACKGROUND_DUPLICAR})
    @Retention(RetentionPolicy.SOURCE)
    private @interface BackgroundOption{}

    public static final int BACKGROUND_BORRAR = 0;
    public static final int BACKGROUND_DUPLICAR = 1;

    private List<Entidad> mData;
    private Context mContext;
    private AdaptadorEntidadesCallback callback;
    private SparseBooleanArray elementosAbiertos;
    private Entidad itemProvisional;
    private int posicionProvisional;

    public AdaptadorDeudas(AppCompatActivity context, List<Entidad> datos) {
        this.mContext = context;
        this.mData = datos;
        this.elementosAbiertos = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_deuda_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Entidad entidad = mData.get(position);
        final Entidad anterior = position > 0 ? mData.get(position - 1) : null;
        holder.bindView(entidad, anterior);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public boolean isValidViewHolder(RecyclerView.ViewHolder viewHolder) {
        return viewHolder instanceof AdaptadorDeudas.ViewHolder;
    }

    public View getForegroundView(RecyclerView.ViewHolder viewHolder) {
        return ((AdaptadorDeudas.ViewHolder) viewHolder).foregroundView;
    }

    public void setBackgroundView(RecyclerView.ViewHolder holder, @BackgroundOption int opcion) {
        if (opcion == BACKGROUND_BORRAR &&
                ExtensionFunctionsKt.isHidden(((ViewHolder)holder).tvDelete)) {
            ExtensionFunctionsKt.visible(((ViewHolder)holder).tvDelete);
            ExtensionFunctionsKt.hide(((ViewHolder)holder).tvDuplicate);
        } else if (opcion == BACKGROUND_DUPLICAR &&
                ExtensionFunctionsKt.isHidden(((ViewHolder)holder).tvDuplicate)) {
            ExtensionFunctionsKt.hide(((ViewHolder)holder).tvDelete);
            ExtensionFunctionsKt.visible(((ViewHolder)holder).tvDuplicate);
        }
    }

    public Entidad getEntidadByPosition(int position) {
        return mData.get(position);
    }

    public void alterItemInPosition(int position, Entidad entidad) {
        mData.set(position, entidad);
        notifyItemChanged(position);
    }

    public void ordenar(int posicionOriginal, Entidad entidad) {
        if (mData.size() > 1) {
            Collections.sort(mData, Entidad.COMPARATOR);
            int posicionNueva = mData.indexOf(entidad);

            reasignarAbiertos(posicionOriginal, posicionNueva);
            notifyItemMoved(posicionOriginal, posicionNueva);

            if (posicionOriginal == 0) {
                notifyItemChanged(0);
            } else {
                actualizarPosterior(posicionOriginal);
            }

            actualizarPosterior(posicionNueva);
        }
    }

    private void reasignarAbiertos(int posicionOriginal, int posicionNueva) {
        boolean estaAbierto = elementosAbiertos.get(posicionOriginal);
        if (posicionOriginal < posicionNueva) {
            for (int i = posicionOriginal + 1; i <= posicionNueva; i++) {
                elementosAbiertos.put(i - 1, elementosAbiertos.get(i));
            }
        } else if (posicionOriginal > posicionNueva) {
            for (int i = posicionOriginal - 1; i >= posicionNueva; i--) {
                elementosAbiertos.put(i + 1, elementosAbiertos.get(i));
            }
        }
        elementosAbiertos.put(posicionNueva, estaAbierto);
    }

    private void reasignarAbiertos(int posicionInsercion) {
        if (posicionInsercion < mData.size() - 1) {
            for (int i = elementosAbiertos.size() - 1 ; i >= 0; i--) {
                int posicionAlmacenada = elementosAbiertos.keyAt(i);
                if (posicionAlmacenada < posicionInsercion) {
                    break;
                } else if (elementosAbiertos.valueAt(i)) {
                    elementosAbiertos.put(posicionAlmacenada + 1, true);
                    elementosAbiertos.put(posicionAlmacenada, false);
                }
            }
        }
    }

    private void reasignarAbiertosTrasEliminacion() {
        elementosAbiertos.delete(posicionProvisional);
        if (posicionProvisional < mData.size() - 1) {
            for (int i = elementosAbiertos.size() - 1 ; i >= 0; i--) {
                int posicionAlmacenada = elementosAbiertos.keyAt(i);
                if (posicionAlmacenada < posicionProvisional) {
                    break;
                } else if (elementosAbiertos.valueAt(i)) {
                    elementosAbiertos.put(posicionAlmacenada - 1, true);
                    elementosAbiertos.put(posicionAlmacenada, false);
                }
            }
        }
    }

    // Indica si una posición existe en el adaptador.
    public boolean isPositionInAdapter(int position) {
        return getItemCount() > 0 && position >= 0 && position < getItemCount();
    }

    public void nuevasEntidades(List<Entidad> entidades) {
        mData.addAll(entidades);
        Collections.sort(mData, Entidad.COMPARATOR);
        for (Entidad entidad : entidades) {
            int posicionInsertada = mData.indexOf(entidad);
            reasignarAbiertos(posicionInsertada);
            notifyItemInserted(posicionInsertada);
            actualizarPosterior(posicionInsertada);
        }
    }

    private void actualizarPosterior(int posicionInsertada) {
        if (mData.size() >= posicionInsertada + 1) {
           notifyItemChanged(posicionInsertada + 1);
        }
    }

    public void eliminarItem(RecyclerView.ViewHolder holder) {
        posicionProvisional = holder.getAdapterPosition();
        itemProvisional = mData.get(posicionProvisional);
        mData.remove(holder.getAdapterPosition());
        reasignarAbiertosTrasEliminacion();
        notifyItemRemoved(posicionProvisional);
        notifyItemChanged(posicionProvisional);
    }

    public Entidad getItemProvisional() {
        return itemProvisional;
    }

    public void deshacerEliminar() {
        mData.add(posicionProvisional, itemProvisional);
        reasignarAbiertos(posicionProvisional);
        notifyItemInserted(posicionProvisional);
        notifyItemChanged(posicionProvisional + 1);
        eliminarProvisionales();
    }

    public void eliminarProvisionales() {
        itemProvisional = null;
        posicionProvisional = -1;
    }

    public void setCallbacks(AdaptadorEntidadesCallback callback) {
        this.callback = callback;
    }

    public interface AdaptadorEntidadesCallback {
        boolean sizeAboutToChange();
        void onAumentarDeudaSeleccionado(Entidad entidad, int adapterPosition);
        void onDescontarDeudaSeleccionado(Entidad entidad, int adapterPosition);
        void onCancelarDeudaSeleccionado(Entidad entidad, int adapterPosition);
        void onLongClick(View view, Entidad entidad, int adapterPosition);
    }


    /**
     * VIEWHOLDER
     */
    class ViewHolder extends ContextRecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView tvDuplicate;
        private TextView tvDelete;
        private CardView foregroundView;
        private LinearLayout llItem;
        private TextView tvFecha;
        private TextView tvConcepto;
        private LinearLayout llAmountRoot;
        private TextView tvCantidad;
        private TextView tvMoneda;
        private LinearLayout llOpcionesEntidad;
        private TextView tvAumentar;
        private TextView tvDescontar;
        private TextView tvCancelar;

        ViewHolder(View itemView) {
            super(itemView);
            bindViews();
            setUpListeners();
        }

        private void bindViews() {
            tvDuplicate = itemView.findViewById(R.id.tv_swipe_option_duplicate);
            tvDelete = itemView.findViewById(R.id.tv_swipe_option_delete);
            foregroundView = itemView.findViewById(R.id.foreground_view);
            llItem = itemView.findViewById(R.id.ll_item);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvConcepto = itemView.findViewById(R.id.tv_concepto);
            llAmountRoot = itemView.findViewById(R.id.ll_amount_root);
            tvCantidad = itemView.findViewById(R.id.tv_cantidad);
            tvMoneda = itemView.findViewById(R.id.tv_moneda);
            llOpcionesEntidad = itemView.findViewById(R.id.ll_opciones_entidad);
            tvAumentar = itemView.findViewById(R.id.tv_aumentar);
            tvDescontar = itemView.findViewById(R.id.tv_descontar);
            tvCancelar = itemView.findViewById(R.id.tv_cancelar);
        }

        private void setUpListeners() {
            llItem.setOnLongClickListener(this);
            llItem.setOnClickListener(this);
            tvAumentar.setOnClickListener(this);
            tvDescontar.setOnClickListener(this);
            tvCancelar.setOnClickListener(this);
        }

        void bindView(final Entidad entidad, final Entidad anterior) {
            if ((anterior != null && anterior.esMismoDia(entidad.getFecha())))
                ExtensionFunctionsKt.hide(tvFecha);
            else {
                ExtensionFunctionsKt.visible(tvFecha);
                tvFecha.setText(entidad.getReadableDate());
            }

            tvConcepto.setText(entidad.getConcepto());
            if (entidad.getCantidad() == ConstantesGenerales.NO_MONEY) {
                tvCantidad.setTextColor(ContextCompat.getColor(mContext, R.color.inactive));
                tvCantidad.setText(R.string.cancelada);
                ExtensionFunctionsKt.hide(tvMoneda);
            } else {
                CurrencyUtilKt.setUpAmount(mContext, entidad.getCantidad(), llAmountRoot, tvCantidad, tvMoneda);
                tvCantidad.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                ExtensionFunctionsKt.visible(tvMoneda);
            }

            mostrarOpciones();
            configurarOpciones(entidad.estaCancelada());
        }

        private void mostrarOpciones() {
            ExtensionFunctionsKt.hidden(llOpcionesEntidad, !elementosAbiertos.get(getAdapterPosition()));
        }

        private void configurarOpciones(boolean estaCancelada) {
            ExtensionFunctionsKt.hidden(tvDescontar, estaCancelada);
            ExtensionFunctionsKt.hidden(tvCancelar, estaCancelada);
        }

        private void toggleOptions() {
            if (callback.sizeAboutToChange()) {
                elementosAbiertos.put(getAdapterPosition(), !elementosAbiertos.get(getAdapterPosition()));
                mostrarOpciones();
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ll_item:
                    toggleOptions();
                    break;
                case R.id.tv_aumentar:
                    callback.onAumentarDeudaSeleccionado(getEntidadByPosition(getAdapterPosition()), getAdapterPosition());
                    break;
                case R.id.tv_descontar:
                    callback.onDescontarDeudaSeleccionado(getEntidadByPosition(getAdapterPosition()) ,getAdapterPosition());
                    break;
                case R.id.tv_cancelar:
                    callback.onCancelarDeudaSeleccionado(getEntidadByPosition(getAdapterPosition()), getAdapterPosition());
                    break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            callback.onLongClick(llItem, getEntidadByPosition(getAdapterPosition()), getAdapterPosition());
            return true;
        }
    }
}

