package melerospaw.deudapp.iu.adapters;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnFocusChange;
import melerospaw.deudapp.R;
import melerospaw.deudapp.iu.vo.EntidadVO;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.utils.DecimalFormatUtils;
import melerospaw.deudapp.utils.EntidadesUtil;
import melerospaw.deudapp.utils.StringUtils;
import melerospaw.deudapp.utils.TecladoUtils;

public class AdaptadorNuevasDeudas
        extends RecyclerView.Adapter<AdaptadorNuevasDeudas.EntidadNuevaViewHolder> {

    private List<EntidadVO> mDatos;
    private Context mContext;
    private OnMostrarDialogoExplicativoListener mostrarDialogoExplicativoListener;
    private boolean justAdded;
    private boolean mostrarNuevasDeudas;

    public AdaptadorNuevasDeudas(Context context, List<Entidad> datos, boolean mostrarNuevasDeudas) {
        this.mContext = context;
        this.mDatos = toEntidadVOList(datos);
        this.mostrarNuevasDeudas = mostrarNuevasDeudas;
    }

    @Override
    public EntidadNuevaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_nuevo_concepto, parent, false);
        return new EntidadNuevaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EntidadNuevaViewHolder holder, int position) {
        Entidad entidad = mDatos.get(position).getEntidad();
        holder.bindView(entidad);
    }

    @Override
    public int getItemCount() {
        return mDatos.size();
    }

    public EntidadVO getEntidadByPosition(int position) {
        return mDatos.get(position);
    }

    public void alterItemInPosition(int position, EntidadVO entidad) {
        mDatos.set(position, entidad);
        notifyItemChanged(position);
    }

    /**
     * Añade un nuevo hueco para acreedor a la lista
     */
    public void nuevaEntidad(@Entidad.TipoEntidad int tipoEntidad) {
        setJustAdded(true);
        mDatos.add(new EntidadVO(new Entidad(tipoEntidad)));
        notifyItemInserted(mDatos.size());
    }

    private void setJustAdded(boolean justAdded) {
        this.justAdded = justAdded;
    }

    public List<Entidad> getEntidades() {
        return EntidadesUtil.getEntidades(mDatos);
    }

    public List<EntidadVO> getEntidadesVO() {
        return EntidadesUtil.getEntidadesVO(mDatos);
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

    public boolean hayEntidadesGrupales() {
        return EntidadesUtil.hayEntidadesGrupales(mDatos);
    }

    public void eliminarItem(RecyclerView.ViewHolder holder) {
        int posicion = holder.getAdapterPosition();
        mDatos.remove(holder.getAdapterPosition());
        notifyItemRemoved(posicion);
        ((EntidadNuevaViewHolder) holder).clear();
    }

    private List<EntidadVO> toEntidadVOList(List<Entidad> entidades) {
        return EntidadesUtil.toEntidadVOList(entidades);
    }

    public void setMostrarDialogoExplicativoListener(OnMostrarDialogoExplicativoListener mostrarDialogoExplicativoListener) {
        this.mostrarDialogoExplicativoListener = mostrarDialogoExplicativoListener;
    }

    public interface OnMostrarDialogoExplicativoListener {
        void onMostrarCuadroIndicativo();
    }


    /**
     * VIEWHOLDER
     */
    class EntidadNuevaViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.et_concepto)         EditText etConcepto;
        @BindView(R.id.et_cantidad)         EditText etCantidad;
        @BindView(R.id.tv_euro)             TextView tvEuro;
        @BindView(R.id.chk_deuda_grupal)    CheckBox chkDeudaGrupal;

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

            chkDeudaGrupal.setVisibility(mostrarNuevasDeudas ? View.VISIBLE : View.GONE);
            if (mostrarNuevasDeudas) {
                chkDeudaGrupal.setChecked(getEntidadByPosition(getAdapterPosition()).getEsGrupal());
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

        @OnCheckedChanged(R.id.chk_deuda_grupal)
        public void onCheckedChanged(boolean isCheked) {
            if (isCheked) {
                mostrarDialogoExplicativoListener.onMostrarCuadroIndicativo();
            }
            getEntidadByPosition(getAdapterPosition()).setEsGrupal(isCheked);
        }

        private void clear() {
            etCantidad.setText("");
            etConcepto.setText("");
        }
    }
}