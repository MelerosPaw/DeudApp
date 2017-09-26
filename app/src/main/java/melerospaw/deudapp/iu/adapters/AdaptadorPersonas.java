package melerospaw.deudapp.iu.adapters;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.otto.Subscribe;

import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import melerospaw.deudapp.R;
import melerospaw.deudapp.constants.ConstantesGenerales;
import melerospaw.deudapp.data.GestorDatos;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.task.BusProvider;
import melerospaw.deudapp.task.EventoDeudaModificada;
import melerospaw.deudapp.utils.DecimalFormatUtils;

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

    public AdaptadorPersonas(Activity context, List<Persona> datos, String tipo) {
        this.context = context;
        this.mDatos = datos;
        this.gestor = GestorDatos.getGestor(context);
        this.seleccionados = new SparseBooleanArray();
        this.tipoPersonas = getTipo(tipo);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate( R.layout.item_acreedores_layout, parent, false);
        return new PersonViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PersonViewHolder holder, int position) {
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

    public void eliminar (List<Persona> personas) {
        for (Persona persona : personas) {
            int posicion = mDatos.indexOf(persona);
            mDatos.remove(persona);
            notifyItemRemoved(posicion);
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

    private void insertarPersona(Persona persona) {
        mDatos.add(persona);
        notifyItemInserted(mDatos.size() - 1);
    }

    private void seleccionarItem(ImageView v, String text, int color) {
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .endConfig()
                .buildRound(text, color);
        v.setImageDrawable(drawable);
    }

    public void eliminarPersona(Persona persona) {
        int posicion = mDatos.indexOf(persona);
        if (posicion != -1) {
            mDatos.remove(persona);
            notifyItemRemoved(posicion);
        }
    }

    private boolean haySeleccionados() {
        for (int i = 0; i <= mDatos.size(); i++) {
            if (seleccionados.get(i))
                return true;
        }
        return false;
    }

    private void deseleccionarTodo() {
        for (int i = 0; i < mDatos.size(); i++) {
            if (seleccionados.get(i)) {
                seleccionados.put(i, false);
                notifyItemChanged(i);
            }
        }
    }

    public void recargarPosicion(Persona persona, int posicion) {
        mDatos.set(posicion, persona);
        notifyItemChanged(posicion);
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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        BusProvider.getBus().register(this);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        BusProvider.getBus().unregister(this);
    }

    @Subscribe
    public void onEventoDeudaModificada(EventoDeudaModificada evento) {
        if (evento.getPersona() != null) {
            Persona persona = evento.getPersona();
            gestor.recargarPersona(persona);
            modificarItemPersona(persona);
        }
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



    /**
     * VIEWHOLDER
     */
    class PersonViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_nombre)           TextView tvNombre;
        @Bind(R.id.tv_deudaRestante)    TextView tvDeudaRestante;
        @Bind(R.id.iv_letra)            ImageView ivLetra;

        private View item;

        PersonViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.item = itemView;
        }

        void bindView(final Persona persona) {
            tvNombre.setText(persona.getNombre());
            tvDeudaRestante.setText(String.format(context.getString(R.string.cantidad),
                    DecimalFormatUtils.decimalToStringIfZero(persona.getCantidadTotal(), 2, ".", ",")));

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
    }
}