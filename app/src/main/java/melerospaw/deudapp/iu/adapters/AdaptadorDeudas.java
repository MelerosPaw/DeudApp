package melerospaw.deudapp.iu.adapters;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.cert.Extension;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import melerospaw.deudapp.R;
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
    private AppCompatActivity mContext;
    private AdaptadorEntidadesCallback callback;
    private SparseBooleanArray elementosAbiertos;
    private Entidad itemProvisional;
    private int posicionProvisional;

    public AdaptadorDeudas(AppCompatActivity context, List<Entidad> datos) {
        this.mContext = context;
        this.mData = datos;
        this.elementosAbiertos = new SparseBooleanArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_deuda_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entidad entidad = mData.get(position);
        if (position > 0) {
            Entidad anterior = mData.get(position - 1);
            holder.bindView(entidad, anterior);
        } else
            holder.bindView(entidad, null);
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

    public void setCallback(AdaptadorEntidadesCallback callback) {
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
    class ViewHolder extends ContextRecyclerView.ViewHolder {

        @BindView(R.id.tv_swipe_option_duplicate)   TextView tvDuplicate;
        @BindView(R.id.tv_swipe_option_delete)      TextView tvDelete;
        @BindView(R.id.foreground_view)             CardView foregroundView;
        @BindView(R.id.ll_item)                     LinearLayout llItem;
        @BindView(R.id.tv_fecha)                    TextView tvFecha;
        @BindView(R.id.tv_concepto)                 TextView tvConcepto;
        @BindView(R.id.ll_amount_root)              LinearLayout llAmountRoot;
        @BindView(R.id.tv_cantidad)                 TextView tvCantidad;
        @BindView(R.id.tv_moneda)                   TextView tvMoneda;
        @BindView(R.id.ll_opciones_entidad)         LinearLayout llOpcionesEntidad;
        @BindView(R.id.tv_aumentar)                 TextView tvAumentar;
        @BindView(R.id.tv_descontar)                TextView tvDescontar;
        @BindView(R.id.tv_cancelar)                 TextView tvCancelar;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindView(final Entidad entidad, final Entidad anterior) {
            if ((anterior != null && anterior.esMismoDia(entidad.getFecha())))
                ExtensionFunctionsKt.hide(tvFecha);
            else {
                ExtensionFunctionsKt.visible(tvFecha);
                tvFecha.setText(entidad.getReadableDate());
            }

            tvConcepto.setText(entidad.getConcepto());
            if (entidad.getCantidad() == 0.00f) {
                tvCantidad.setTextColor(ContextCompat.getColor(mContext, R.color.inactive));
                tvCantidad.setText(R.string.cancelada);
            } else {
                CurrencyUtilKt.setUpAmount(mContext, entidad.getCantidad(), llAmountRoot, tvCantidad, tvMoneda);
                tvCantidad.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
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

        @OnClick({R.id.ll_item, R.id.tv_aumentar, R.id.tv_descontar, R.id.tv_cancelar})
        public void onViewClicked(View view) {
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

        @OnLongClick(R.id.ll_item)
        public boolean onLongClick() {
            callback.onLongClick(llItem, getEntidadByPosition(getAdapterPosition()), getAdapterPosition());
            return true;
        }


        private void toggleOptions() {
            if (callback.sizeAboutToChange()) {
                elementosAbiertos.put(getAdapterPosition(), !elementosAbiertos.get(getAdapterPosition()));
                mostrarOpciones();
            }
        }
    }
}

