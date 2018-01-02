package melerospaw.deudapp.iu.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import melerospaw.deudapp.R;
import melerospaw.deudapp.iu.widgets.ContextRecyclerView;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.utils.DecimalFormatUtils;


public class AdaptadorDeudas extends ContextRecyclerView.Adapter<AdaptadorDeudas.ViewHolder> {

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_deuda_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
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
            } else if (posicionOriginal < mData.size() - 1) {
                notifyItemChanged(posicionOriginal + 1);
            }

            if (posicionNueva < mData.size() - 1) {
                notifyItemChanged(posicionNueva + 1);
            }
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
        void onAumentarDedudaSeleccionado(Entidad entidad, int adapterPosition);
        void onDescontarDedudaSeleccionado(Entidad entidad, int adapterPosition);
        void onCancelarDedudaSeleccionado(Entidad entidad, int adapterPosition);
        void onLongClick(Entidad entidad, int adapterPosition);
    }


    /**
     * VIEWHOLDER
     */
    class ViewHolder extends ContextRecyclerView.ViewHolder {

        @BindView(R.id.cv_item)             LinearLayout cvItem;
        @BindView(R.id.tv_fecha)            TextView tvFecha;
        @BindView(R.id.tv_concepto)         TextView tvConcepto;
        @BindView(R.id.tv_cantidad)         TextView tvCantidad;
        @BindView(R.id.ll_opciones_entidad) LinearLayout llOpcionesEntidad;
        @BindView(R.id.tv_aumentar)         TextView tvAumentar;
        @BindView(R.id.tv_descontar)        TextView tvDescontar;
        @BindView(R.id.tv_cancelar)         TextView tvCancelar;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindView(final Entidad entidad, final Entidad anterior) {
            if ((anterior != null && anterior.esMismoDia(entidad)))
                tvFecha.setVisibility(View.GONE);
            else {
                tvFecha.setVisibility(View.VISIBLE);
                tvFecha.setText(entidad.getReadableDate());
            }

            tvConcepto.setText(entidad.getConcepto());
            String cantidad = String.format(mContext.getString(R.string.cantidad),
                    DecimalFormatUtils.decimalToStringIfZero(entidad.getCantidad(), 2, ".", ","));
            tvCantidad.setText(cantidad);
            if (entidad.getCantidad() == 0.00f) {
                tvCantidad.setTextColor(ContextCompat.getColor(mContext, R.color.inactive));
                tvCantidad.setText(R.string.cancelada);
            } else {
                tvCantidad.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            }

            mostrarOpciones();
            configurarOpciones(entidad.estaCancelada());
        }

        private void mostrarOpciones() {
            int visibility = elementosAbiertos.get(getAdapterPosition()) ?
                    View.VISIBLE : View.GONE;
            if (visibility != llOpcionesEntidad.getVisibility()) {
                llOpcionesEntidad.setVisibility(visibility);
            }
        }

        private void configurarOpciones(boolean estaCancelada) {
            tvDescontar.setVisibility(estaCancelada ? View.GONE : View.VISIBLE);
            tvCancelar.setVisibility(estaCancelada ? View.GONE : View.VISIBLE);
        }

        @OnClick({R.id.cv_item, R.id.tv_aumentar, R.id.tv_descontar, R.id.tv_cancelar})
        public void onViewClicked(View view) {
            switch (view.getId()) {
                case R.id.cv_item:
                    toggleOptions();
                    break;
                case R.id.tv_aumentar:
                    callback.onAumentarDedudaSeleccionado(getEntidadByPosition(getAdapterPosition()), getAdapterPosition());
                    break;
                case R.id.tv_descontar:
                    callback.onDescontarDedudaSeleccionado(getEntidadByPosition(getAdapterPosition()) ,getAdapterPosition());
                    break;
                case R.id.tv_cancelar:
                    callback.onCancelarDedudaSeleccionado(getEntidadByPosition(getAdapterPosition()), getAdapterPosition());
                    break;
            }
        }

        @OnLongClick(R.id.cv_item)
        public boolean onLongClick() {
            callback.onLongClick(getEntidadByPosition(getAdapterPosition()), getAdapterPosition());
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

