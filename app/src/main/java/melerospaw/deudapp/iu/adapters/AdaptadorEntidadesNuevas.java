package melerospaw.deudapp.iu.adapters;

import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import melerospaw.deudapp.R;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.utils.DecimalFormatUtils;
import melerospaw.deudapp.utils.StringUtils;

public class AdaptadorEntidadesNuevas extends RecyclerView.Adapter<AdaptadorEntidadesNuevas.ViewHolder> {

    private List<Entidad> mData;
    private AppCompatActivity mContext;

    public AdaptadorEntidadesNuevas(AppCompatActivity context, List<Entidad> datos) {
        this.mContext = context;
        this.mData = datos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_nuevo_concepto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Entidad entidad = mData.get(position);
        holder.bindView(entidad);
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

    /**
     * Añade un nuevo hueco para acreedor a la lista
     */
    public void nuevaEntidad(@Entidad.TipoEntidad int tipoEntidad) {
        mData.add(new Entidad(tipoEntidad));
        notifyItemInserted(mData.size());
    }

    public List<Entidad> getEntidades() {
        List<Entidad> entidades = new LinkedList<>();

        for (Entidad entidad : mData) {
            if (!StringUtils.isCadenaVacia(entidad.getConcepto()) && entidad.getCantidad() != 0.00f)
                entidades.add(entidad);
        }

        return entidades;
    }

    public boolean hayAlgo() {
        return !getEntidades().isEmpty();
    }

    public boolean hayDeudas() {
        for (Entidad entidad : mData) {
            if (entidad.getTipoEntidad() == Entidad.DEUDA && entidad.estaDefinida()) {
                return true;
            }
        }

        return false;
    }

    public boolean hayDerechosCobro() {
        for (Entidad entidad : mData) {
            if (entidad.getTipoEntidad() == Entidad.DERECHO_COBRO && entidad.estaDefinida()) {
                return true;
            }
        }

        return false;
    }



    /**
     * VIEWHOLDER
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.et_concepto) EditText etConcepto;
        @Bind(R.id.et_cantidad) EditText etCantidad;
        @Bind(R.id.tv_euro)     TextView tvEuro;

        private Entidad entidad;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bindView(Entidad entidad) {
            this.entidad = entidad;
            setTextColor();

            if (!StringUtils.isCadenaVacia(entidad.getConcepto())) {
                etConcepto.setText(entidad.getConcepto());
            }

            if (entidad.getCantidad() != 0.00f) {
                String cantidad = DecimalFormatUtils.decimalToStringIfZero(entidad.getCantidad(), 2, ".", ",");
                etCantidad.setText(cantidad);
            }

            etConcepto.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_NEXT) {
                        cerrarEdicion(etConcepto, false);
                    }
                    return false;
                }
            });
        }

        private void setTextColor() {
            @ColorRes int color = entidad.getTipoEntidad() == Entidad.DEUDA ?
                    R.color.red : R.color.green;
            etCantidad.setTextColor(ContextCompat.getColor(mContext, color));
            tvEuro.setTextColor(ContextCompat.getColor(mContext, color));
        }

        @OnFocusChange({R.id.et_cantidad, R.id.et_concepto})
        public void cerrarEdicion(View v, boolean hasFocus) {

            if (!hasFocus) {
                if (v.getId() == R.id.et_concepto) {
                    String concepto = etConcepto.getText().toString();
                    if (!StringUtils.isCadenaVacia(concepto)) {
                        entidad.setConcepto(concepto);
                    }
                } else if (v.getId() == R.id.et_cantidad) {
                    String cantidad = etCantidad.getText().toString().replaceAll(",", ".");
                    if (!cantidad.isEmpty() && !StringUtils.convertible(cantidad).equals("string")) {
                        if (Float.parseFloat(cantidad) != 0) {
                            entidad.setCantidad(Float.parseFloat(cantidad));
                            etCantidad.setText(DecimalFormatUtils.decimalToStringIfZero(entidad.getCantidad(), 2, ".", ","));
                        } else {
                            entidad.setCantidad(-0f);
                        }
                    }
                }
            } else if (v.getId() == R.id.et_cantidad && entidad.getTipoEntidad() == Entidad.DEUDA && entidad.getCantidad() != 0f) {
                etCantidad.setText(DecimalFormatUtils.decimalToStringIfZero(entidad.getCantidad() * -1, 2, ".", ","));
            }
        }
    }
}