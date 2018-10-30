package melerospaw.deudapp.iu.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.otto.Subscribe;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import melerospaw.deudapp.R;
import melerospaw.deudapp.constants.ConstantesGenerales;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.task.BusProvider;
import melerospaw.deudapp.task.EventoDeudaModificada;
import melerospaw.deudapp.utils.CurrencyUtilKt;
import melerospaw.deudapp.utils.SecureOperationKt;
import melerospaw.deudapp.utils.TextDrawableManager;

public class AdaptadorPersonas extends RecyclerView.Adapter<AdaptadorPersonas.PersonViewHolder> {

    private GestorDatos gestor;
    private List<Persona> mDatos;
    private Activity context;
    private boolean modoEliminarActivado;
    private @Persona.TipoPersona int tipoPersonas;
    private SparseBooleanArray seleccionados;
    private ContextualMenuInterface contextualMenuInterface;
    private OnItemClickListener itemClickListener;
    private OnPersonaSeleccionadaListener onPersonaSeleccionadaListener;
    private OnDeudaModificadaListener onDeudaModificadaListener;

    public AdaptadorPersonas(Activity context, List<Persona> datos, String tipo) {
        this.context = context;
        this.mDatos = datos;
        this.gestor = GestorDatos.getGestor(context);
        this.seleccionados = new SparseBooleanArray();
        this.tipoPersonas = getTipo(tipo);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate( R.layout.item_acreedores_layout, parent, false);
        return new PersonViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        Persona persona = mDatos.get(position);
        holder.bindView(persona);
    }

    @Override
    public int getItemCount() {
        return mDatos.size();
    }

    private  @Persona.TipoPersona int getTipo(String tipo) {

        @Persona.TipoPersona int tipoInferido = Persona.INACTIVO;

        if (ConstantesGenerales.DEBO.equals(tipo)) {
            tipoInferido = Persona.ACREEDOR;
        } else if (ConstantesGenerales.ME_DEBEN.equals(tipo)) {
            tipoInferido = Persona.DEUDOR;
        } else if (ConstantesGenerales.AMBOS.equals(tipo)) {
            tipoInferido = Persona.AMBOS;
        }

        return tipoInferido;
    }

    private boolean personaIsInAdapter(Persona person) {
        return mDatos.indexOf(person) != -1;
    }

    public List<Persona> obtenerMarcados() {
        LinkedList<Persona> marcados = new LinkedList<>();
        for (int i = 0; i < mDatos.size(); i++) {
            if (seleccionados.get(i)) {
                marcados.add(mDatos.get(i));
            }
        }

        return marcados;
    }

    public int getPosition(Persona persona) {
        return mDatos.indexOf(persona);
    }

    private void insertarPersona(Persona persona) {
        mDatos.add(persona);
        notifyItemInserted(mDatos.size() - 1);
    }

    public void eliminarPersona(Persona persona) {
        int posicion = mDatos.indexOf(persona);

        if (posicion != -1) {
            mDatos.remove(persona);
            notifyItemRemoved(posicion);
            if (seleccionados.get(posicion)) {
                seleccionados.put(posicion, false);
            }
            if (haySeleccionados()) {
                reassignSelected(posicion);
                onPersonaSeleccionadaListener.personaDeseleccionada(false);
            } else {
                desactivarModoEliminacion();
                onPersonaSeleccionadaListener.personaDeseleccionada(true);
            }
        }

        if (getItemCount() == 0) {
            desactivarModoEliminacion();
        }
    }

    public void eliminarVarios(List<Persona> personas) {
        for (Persona persona : personas) {
            int posicion = mDatos.indexOf(persona);
            mDatos.remove(persona);
            notifyItemRemoved(posicion);
            seleccionados.put(posicion, false);
            reassignSelected(posicion);
        }

    }

    private void reassignSelected(int deletedPosition) {
        int size = seleccionados.size();
        for (int i = 0; i < size; i++) {
            int key = seleccionados.keyAt(i);
            if (key > mDatos.size()) {
                seleccionados.put(key, false);
            } else if (key > deletedPosition && seleccionados.get(key)) {
                seleccionados.put(key, false);
                if (seleccionados.indexOfKey(key - 1) < 0) {
                    i++;
                    size++;
                }
                seleccionados.put(key - 1, true);
            }
        }
    }

    public void desactivarModoEliminacion() {
        modoEliminarActivado = false;
        seleccionados.clear();
    }

    private void actualizarCantidad(Persona persona) {
        int posicionPersonaCambiada = mDatos.indexOf(persona);
        if (posicionPersonaCambiada != -1) {
            mDatos.set(posicionPersonaCambiada, persona);
            notifyItemChanged(posicionPersonaCambiada);
        }
    }

    private boolean haySeleccionados() {
        for (int i = 0; i <= mDatos.size(); i++) {
            if (seleccionados.get(i))
                return true;
        }
        return false;
    }

    public void deseleccionarTodo() {
        for (int i = 0; i < mDatos.size(); i++) {
            if (seleccionados.get(i)) {
                notifyItemChanged(i);
            }
        }
        desactivarModoEliminacion();
    }

    public void seleccionarTodo() {
        for (int i = 0; i < mDatos.size(); i++) {
            if (!seleccionados.get(i)) {
                seleccionados.put(i, true);
                notifyItemChanged(i);
            }
        }
    }

    public void recargarPosicion(Persona persona, int posicion) {
        mDatos.set(posicion, persona);
        notifyItemChanged(posicion);
    }

    public float obtenerTotal() {
        float total = 0;
        for (Persona persona : mDatos) {
            total = SecureOperationKt.secureAdd(total, persona.getCantidadTotal());
        }

        return total;
    }

    public float obtenerSubtotal() {
        float subtotal = 0;

        List<Persona> marcados = obtenerMarcados();
        for (Persona persona : marcados) {
            subtotal += persona.getCantidadTotal();
        }

        return subtotal;
    }

    public void setContextualMenuInterface(ContextualMenuInterface interfaz) {
        this.contextualMenuInterface = interfaz;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setOnPersonaSeleccionadaListener(OnPersonaSeleccionadaListener onPersonaSeleccionadaListener) {
        this.onPersonaSeleccionadaListener = onPersonaSeleccionadaListener;
    }

    public void setOnDeudaModificadaListener(OnDeudaModificadaListener onDeudaModificadaListener) {
        this.onDeudaModificadaListener = onDeudaModificadaListener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        BusProvider.getBus().register(this);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        BusProvider.getBus().unregister(this);
    }

    private void modificarItemPersona(Persona persona) {
        if (persona.getTipo() == tipoPersonas) {
            if (personaIsInAdapter(persona)) {
                actualizarCantidad(persona);
            } else {
                insertarPersona(persona);
            }
        } else {
            eliminarPersona(persona);
        }
    }

    @Subscribe
    public void onEventoDeudaModificada(EventoDeudaModificada evento) {
        if (evento.getPersona() != null) {
            Persona persona = evento.getPersona();
            gestor.recargarPersona(persona);
            modificarItemPersona(persona);
            onDeudaModificadaListener.onDeudaModificada(obtenerTotal());
        }
    }

    public interface ContextualMenuInterface {
        void mostrarMenuContextual(Persona persona, int posicionEnAdapter);
    }

    public interface OnItemClickListener {
        void onClick(Persona persona);
    }

    public interface OnPersonaSeleccionadaListener {
        void personaSeleccionada(boolean activarModoEliminacion);
        void personaDeseleccionada(boolean desactivarModoEliminacion);
    }

    public interface OnDeudaModificadaListener {
        void onDeudaModificada(float totalActualizado);
    }



    /**
     * VIEWHOLDER
     */
    class PersonViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_nombre)           TextView tvNombre;
        @BindView(R.id.tv_deudaRestante)    TextView tvDeudaRestante;
        @BindView(R.id.iv_letra)            ImageView ivLetra;

        private View item;

        PersonViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.item = itemView;
        }

        void bindView(final Persona persona) {
            tvNombre.setText(persona.getNombre());
            tvDeudaRestante.setText(CurrencyUtilKt.formatAmount(context, persona.getCantidadTotal()));

            ColorGenerator cg = ColorGenerator.MATERIAL;
            if (persona.getColor() == -1) {
                persona.setColor(cg.getRandomColor());
                gestor.actualizarPersona(persona, persona.getTipo());
            }

            if (seleccionados.get(getAdapterPosition())) {
                seleccionarItem(ivLetra, "✓", ContextCompat.getColor(context, R.color.inactive));
            } else {
                seleccionarItem(ivLetra, String.valueOf(persona.getNombre().charAt(0)).toUpperCase(),
                        persona.getColor());
            }

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onClick(persona);
                    }
                }
            });

            item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (contextualMenuInterface != null) {
                        contextualMenuInterface.mostrarMenuContextual(persona, getAdapterPosition());
                        return true;
                    }
                    return false;
                }
            });

            ivLetra.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gestionarModoEliminar(ivLetra, getAdapterPosition());
                }
            });
        }

        private void gestionarModoEliminar(ImageView v, int posicionPulsada) {
            if (!modoEliminarActivado) {
                activarModoEliminar(v, posicionPulsada);
            } else {
                modificarSeleccion(v, posicionPulsada);
            }
        }

        private void modificarSeleccion(View v, int posicionPulsada) {
            if (!seleccionados.get(posicionPulsada)) {
                seleccionados.put(posicionPulsada, true);
                seleccionarItem(((ImageView) v), "✓", ContextCompat.getColor(context, R.color.inactive));
                if (onPersonaSeleccionadaListener != null) {
                    onPersonaSeleccionadaListener.personaSeleccionada(false);
                }
            } else {
                seleccionados.put(posicionPulsada, false);
                seleccionarItem((ImageView) v, String.valueOf(mDatos.get(posicionPulsada).getNombre().charAt(0)).toUpperCase(),
                        mDatos.get(posicionPulsada).getColor());
                if (!haySeleccionados()) {
                    modoEliminarActivado = false;
                    if (onPersonaSeleccionadaListener != null) {
                        onPersonaSeleccionadaListener.personaDeseleccionada(true);
                    }
                } else {
                    if (onPersonaSeleccionadaListener != null) {
                        onPersonaSeleccionadaListener.personaDeseleccionada(false);
                    }
                }
            }
        }

        private void activarModoEliminar(ImageView v, int posicionPulsada) {
            modoEliminarActivado = true;
            seleccionarItem(v, "✓", ContextCompat.getColor(context, R.color.inactive));
            seleccionados.put(posicionPulsada, true);
            if (onPersonaSeleccionadaListener != null) {
                onPersonaSeleccionadaListener.personaSeleccionada(true);
            }
        }

        private void seleccionarItem(ImageView v, String text, int color) {
            v.setImageDrawable(TextDrawableManager.createDrawable(text, color));
        }
    }
}