package melerospaw.deudapp.iu.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import java.util.List;
import melerospaw.deudapp.R;
import melerospaw.deudapp.constants.ConstantesGenerales;
import melerospaw.deudapp.iu.vo.EntidadVO;
import melerospaw.deudapp.modelo.Entidad;
import melerospaw.deudapp.utils.CurrencyUtilKt;
import melerospaw.deudapp.utils.DecimalFormatUtils;
import melerospaw.deudapp.utils.EntidadesUtilKt;
import melerospaw.deudapp.utils.ExtensionFunctionsKt;
import melerospaw.deudapp.utils.InfinityManagerKt;
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
    public EntidadNuevaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_nuevo_concepto, parent, false);
        return new EntidadNuevaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EntidadNuevaViewHolder holder, int position) {
        Entidad entidad = mDatos.get(position).getEntidad();
        holder.bindView(entidad);
    }

    @Override
    public int getItemCount() {
        return mDatos.size();
    }

    public void alterItemInPosition(int position, EntidadVO entidad) {
        mDatos.set(position, entidad);
        notifyItemChanged(position);
    }

    /**
     * Añade un nuevo hueco para acreedor a la lista.
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
        return EntidadesUtilKt.getEntidades(mDatos);
    }

    public List<EntidadVO> getEntidadesVO() {
        return EntidadesUtilKt.getEntidadesVO(mDatos);
    }

    public boolean hayEntidadesIncompletas() {
        return EntidadesUtilKt.hayEntidadesIncompletas(mDatos);
    }

    public boolean hayConceptosRepetidos() {
        return EntidadesUtilKt.hayEntidadesRepetidas(mDatos);
    }

    public boolean estaVacio() {
        return mDatos.isEmpty();
    }

    public boolean hayDeudas() {
        return EntidadesUtilKt.hayDelTipo(mDatos, Entidad.DEUDA);
    }

    public boolean hayDerechosCobro() {
        return EntidadesUtilKt.hayDelTipo(mDatos, Entidad.DERECHO_COBRO);
    }

    public boolean hayEntidadesGrupales() {
        return EntidadesUtilKt.hayEntidadesGrupales(mDatos);
    }

    public void eliminarItem(RecyclerView.ViewHolder holder) {
        int posicion = holder.getAdapterPosition();
        mDatos.remove(holder.getAdapterPosition());
        notifyItemRemoved(posicion);
        ((EntidadNuevaViewHolder) holder).clear();
    }

    private List<EntidadVO> toEntidadVOList(List<Entidad> entidades) {
        return EntidadesUtilKt.toEntidadVOList(entidades);
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
    class EntidadNuevaViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        private ViewGroup root;
        private EditText etConcepto;
        private EditText etCantidad;
        private TextView tvMoneda;
        private CheckBox chkDeudaGrupal;

        private Entidad entidad;

        EntidadNuevaViewHolder(View itemView) {
            super(itemView);
            bindViews();
            setUpListeners();
        }

        private void bindViews() {
            root = itemView.findViewById(R.id.root);
            etConcepto = itemView.findViewById(R.id.et_concepto);
            etCantidad = itemView.findViewById(R.id.etCantidad);
            tvMoneda = itemView.findViewById(R.id.tv_moneda);
            chkDeudaGrupal = itemView.findViewById(R.id.chk_deuda_grupal);
        }

        private void setUpListeners() {
            etCantidad.setOnFocusChangeListener(this::cerrarEdicion);
            etConcepto.setOnFocusChangeListener(this::cerrarEdicion);
            chkDeudaGrupal.setOnCheckedChangeListener(this);
        }

        private void bindView(Entidad entidad) {
            this.entidad = entidad;
            setTextColor();
            CurrencyUtilKt.setUpAmount(mContext, null, root, etCantidad, tvMoneda);

            if (!StringUtils.isCadenaVacia(entidad.getConcepto())) {
                etConcepto.setText(entidad.getConcepto());
            } else {
                etConcepto.setText("");
            }

            if (entidad.getCantidad() != ConstantesGenerales.NO_MONEY) {
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

            ExtensionFunctionsKt.hidden(chkDeudaGrupal, !mostrarNuevasDeudas);
            if (mostrarNuevasDeudas) {
                chkDeudaGrupal.setChecked(getEntidadByPosition(getAdapterPosition()).getEsGrupal());
            }
        }

        private void setTextColor() {
            @ColorRes final int colorId = entidad.getTipoEntidad() == Entidad.DEUDA ?
                    R.color.red : R.color.green;
            @ColorInt final int colorResource = ContextCompat.getColor(mContext, colorId);
            etCantidad.setTextColor(colorResource);
            tvMoneda.setTextColor(colorResource);
            etConcepto.setTextColor(colorResource);
        }

        private void cerrarEdicion(View v, boolean hasFocus) {

            if (!hasFocus) {
                if (v.getId() == R.id.et_concepto) {
                    cerrarConcepto();
                } else if (v.getId() == R.id.etCantidad) {
                    cerrarCantidad();
                }
            } else if (v.getId() == R.id.etCantidad &&
                    entidad.getTipoEntidad() == Entidad.DEUDA &&
                    entidad.getCantidad() != 0F) {
                abrirCantidad();
            }
        }

        private void cerrarConcepto() {
            String concepto = etConcepto.getText().toString();
            if (!TextUtils.isEmpty(concepto)) {
                entidad.setConcepto(concepto);
            } else {
                entidad.setConcepto(null);
            }
        }

        private void cerrarCantidad() {
            String cantidadDecimal = StringUtils.prepararDecimal(etCantidad.getText().toString());

            if (!StringUtils.esConvertible(cantidadDecimal).equals("string")) {
                final float cantidad = Float.parseFloat(cantidadDecimal);

                if (InfinityManagerKt.isInfiniteFloat(cantidad)) {
                    InfinityManagerKt.mostrarInfinityDialog(mContext, null,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            modificarCantidad(cantidad);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            modificarCantidad(-0F);
                        }
                    });

                } else if (cantidad != 0F) {
                    modificarCantidad(cantidad);
                }

            } else {
                setCantidad(-0F);
            }
        }

        private void modificarCantidad(float cantidad) {
            setCantidad(cantidad);
            displayCantidad();
        }

        private void setCantidad(float cantidad) {
            entidad.setCantidad(cantidad);
        }

        private void displayCantidad() {
            etCantidad.setText(DecimalFormatUtils.decimalToStringIfZero(entidad.getCantidad(), 2, ".", ","));
        }

        private void abrirCantidad() {
            etCantidad.setText(DecimalFormatUtils.decimalToStringIfZero(entidad.getCantidad() * -1, 2, ".", ","));
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                mostrarDialogoExplicativoListener.onMostrarCuadroIndicativo();
            }
            getEntidadByPosition(getAdapterPosition()).setEsGrupal(isChecked);
        }

        private void clear() {
            etCantidad.setText("");
            etConcepto.setText("");
        }

        private EntidadVO getEntidadByPosition(int position) {
            return mDatos.get(position);
        }
    }
}