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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import melerospaw.deudapp.R;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.utils.DecimalFormatUtils;
import melerospaw.deudapp.utils.EntidadesUtil;
import melerospaw.deudapp.utils.StringUtils;
import melerospaw.deudapp.utils.TecladoUtils;

public class AdaptadorEntidadesNuevas
        extends RecyclerView.Adapter<AdaptadorEntidadesNuevas.EntidadNuevaViewHolder> {

    private List<Entidad> mDatos;
    private AppCompatActivity mContext;
    private boolean justAdded;

    public AdaptadorEntidadesNuevas(AppCompatActivity context, List<Entidad> datos) {
        this.mContext = context;
        this.mDatos = datos;
    }

    @Override
    public EntidadNuevaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_nuevo_concepto, parent, false);
        return new EntidadNuevaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EntidadNuevaViewHolder holder, int position) {
        Entidad entidad = mDatos.get(position);
        holder.bindView(entidad);
    }

    @Override
    public int getItemCount() {
        return mDatos.size();
    }

    public Entidad getEntidadByPosition(int position) {
        return mDatos.get(position);
    }

    public void alterItemInPosition(int position, Entidad entidad) {
        mDatos.set(position, entidad);
        notifyItemChanged(position);
    }

    /**
     * Añade un nuevo hueco para acreedor a la lista
     */
    public void nuevaEntidad(@Entidad.TipoEntidad int tipoEntidad) {
        setJustAdded(true);
        mDatos.add(new Entidad(tipoEntidad));
        notifyItemInserted(mDatos.size());
    }

    private void setJustAdded(boolean justAdded) {
        this.justAdded = justAdded;
    }

    public List<Entidad> getEntidades() {
        return EntidadesUtil.getEntidades(mDatos);
    }

    public boolean hayEntidadesIncompletas() {
        return EntidadesUtil.hayEntidadesIncompletas(mDatos);
    }

    public boolean hayEntidadesRepetidas() {
        return EntidadesUtil.hayEntidadesRepetidas(mDatos);
    }

    public boolean hayAlgo() {
        return !mDatos.isEmpty();
    }

    public boolean hayDeudas() {
        return EntidadesUtil.hayDelTipo(mDatos, Entidad.DEUDA);
    }

    public boolean hayDerechosCobro() {
        return EntidadesUtil.hayDelTipo(mDatos, Entidad.DERECHO_COBRO);
    }

    public void eliminarItem(RecyclerView.ViewHolder holder) {
        int posicion = holder.getAdapterPosition();
        mDatos.remove(holder.getAdapterPosition());
        notifyItemRemoved(posicion);
        ((EntidadNuevaViewHolder) holder).clear();
    }


    /**
     * VIEWHOLDER
     */
    class EntidadNuevaViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.et_concepto) EditText etConcepto;
        @BindView(R.id.et_cantidad) EditText etCantidad;
        @BindView(R.id.tv_euro)     TextView tvEuro;

        private Entidad entidad;

        EntidadNuevaViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bindView(Entidad entidad) {
            this.entidad = entidad;
            setTextColor();

            if (!StringUtils.isCadenaVacia(entidad.getConcepto())) {
                etConcepto.setText(entidad.getConcepto());
            } else {
                etConcepto.setText("");
            }

            if (entidad.getCantidad() != 0.00f) {
                String cantidad = DecimalFormatUtils.decimalToStringIfZero(entidad.getCantidad(), 2, ".", ",");
                etCantidad.setText(cantidad);
            } else {
                etCantidad.setText("");
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

            if (justAdded) {
                TecladoUtils.mostrarTeclado(etConcepto);
                setJustAdded(false);
            }
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
                    } else {
                        entidad.setConcepto(null);
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

        private void clear() {
            etCantidad.setText("");
            etConcepto.setText("");
        }
    }
}