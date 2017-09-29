package melerospaw.deudapp.iu.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import melerospaw.deudapp.R;
import melerospaw.deudapp.iu.widgets.ContextRecyclerView;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.utils.DecimalFormatUtils;


public class AdaptadorEntidades extends ContextRecyclerView.Adapter<AdaptadorEntidades.ViewHolder> {

    private List<Entidad> mData;
    private AppCompatActivity mContext;
    private AdaptadorEntidadesCallback callback;
    private SparseBooleanArray elementosAbiertos;

    public AdaptadorEntidades(AppCompatActivity context, List<Entidad> datos) {
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

    // Indica si una posición existe en el adaptador.
    public boolean isPositionInAdapter(int position) {
        return getItemCount() > 0 && position >= 0 && position < getItemCount();
    }

    public void nuevasEntidades(List<Entidad> entidades) {
        mData.addAll(0, entidades);
        notifyItemRangeInserted(0, entidades.size());
        notifyItemChanged(entidades.size());
    }

    public void setCallback(AdaptadorEntidadesCallback callback) {
        this.callback = callback;
    }

    public interface AdaptadorEntidadesCallback {
        boolean sizeAboutToChange();
        void onAumentarDedudaSeleccionado(Entidad entidad, int adapterPosition);
        void onDescontarDedudaSeleccionado(Entidad entidad, int adapterPosition);
        void onCancelarDedudaSeleccionado(Entidad entidad, int adapterPosition);
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
            if ((anterior != null && anterior.compareTo(entidad) == 0))
                tvFecha.setVisibility(View.GONE);
            else {
                tvFecha.setVisibility(View.VISIBLE);
                tvFecha.setText(entidad.getReadableDate());
            }

            tvConcepto.setText(entidad.getConcepto());
            String cantidad = DecimalFormatUtils.decimalToStringIfZero(entidad.getCantidad(), 2, ".", ",") + " €";
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

        private void toggleOptions() {
            if (callback.sizeAboutToChange()) {
                elementosAbiertos.put(getAdapterPosition(), !elementosAbiertos.get(getAdapterPosition()));
                mostrarOpciones();
            }
        }
    }
}

