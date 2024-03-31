package melerospaw.deudapp.iu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import java.util.ArrayList;
import java.util.List;
import melerospaw.deudapp.R;
import melerospaw.deudapp.data.ContactManager;
import melerospaw.deudapp.modelo.Contact;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.utils.ExtensionFunctionsKt;
import melerospaw.deudapp.utils.StringUtils;
import melerospaw.deudapp.utils.TecladoUtils;

public class AdaptadorPersonasNuevas
        extends RecyclerView.Adapter<AdaptadorPersonasNuevas.NuevaPersonaViewHolder> {

    private List<Contact> mDatos;
    private List<Contact> listaContactos;
    private Context mContext;
    private AutocompleteAdapter adapter;
    private boolean focus;
    private boolean isJustAdded;

    public AdaptadorPersonasNuevas(Context context, List<Contact> mDatos,
                                   List<Contact> personas) {
        this.mContext = context;
        this.mDatos = mDatos;
        this.listaContactos = personas;
    }

    @Override
    public NuevaPersonaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_nueva_persona, parent, false);
        return new NuevaPersonaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NuevaPersonaViewHolder holder, int position) {
        Contact contact = mDatos.get(position);
        if (position == mDatos.size() - 1)
            holder.bindView(contact, focus);
        else
            holder.bindView(contact, false);
    }

    @Override
    public int getItemCount() {
        return mDatos.size();
    }

    /**Añade un nuevo hueco para contact a la lista*/
    public void nuevaPersona() {
        mDatos.add(new Contact());
        notifyItemInserted(mDatos.size());
        focus = true;
    }

    public List<Persona> getPersonas() {
        List<Persona> personas = new ArrayList<>();

        for (Contact contact : mDatos){
            if (!contact.getName().trim().isEmpty())
                personas.add(new Persona(contact.getName(), contact.getPhotoUri(), contact.getColor()));
        }

        return personas;
    }

    public boolean hayAlguien() {
        return !getPersonas().isEmpty();
    }

    public boolean hayNombresRepetidos() {
        int size = mDatos.size();
        for (int i = 0; i < size; i++) {
            for (int j = size - 1; j > i; j--) {
                if (mDatos.get(i).getName().equals(mDatos.get(j).getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void eliminarItem(RecyclerView.ViewHolder holder) {
        int posicion = holder.getAdapterPosition();
        mDatos.remove(holder.getAdapterPosition());
        notifyItemRemoved(posicion);
    }



    /**VIEWHOLDER*/
    class NuevaPersonaViewHolder extends RecyclerView.ViewHolder {

        private AutoCompleteTextView actvAcreedor;

        private Contact contact;

        private NuevaPersonaViewHolder(View itemView) {
            super(itemView);
            bindViews();
            setUpListeners();
        }

        private void bindViews() {
            actvAcreedor = itemView.findViewById(R.id.actv_persona);
        }

        private void setUpListeners() {
            actvAcreedor.setOnFocusChangeListener((view, b) -> cerrarEdicion(b));
        }

        private void bindView(Contact contact, boolean focus){

            this.contact = contact;

            actvAcreedor.setText(contact.getName());
            filtrarLista();
            adapter = new AutocompleteAdapter(mContext, R.layout.item_autocomplete, listaContactos);
            actvAcreedor.setAdapter(adapter);
            actvAcreedor.setOnItemClickListener((adapterView, view, i, l) -> {
                Contact contactoSeleccionado = adapter.getItem(i);
                NuevaPersonaViewHolder.this.contact.setColor(contactoSeleccionado.getColor());
//                    NuevaPersonaViewHolder.this.contact.setPhotoUri(contactoSeleccionado.getPhotoUri());
            });

            if (StringUtils.isCadenaVacia(this.contact.getName()) && focus) {
                ExtensionFunctionsKt.visible(actvAcreedor);
                actvAcreedor.requestFocus();
                TecladoUtils.mostrarTeclado(actvAcreedor);
            }

            actvAcreedor.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    cerrarEdicion(false);
                }
                return false;
            });
        }

        private void cerrarEdicion(boolean hasFocus) {
            if (!hasFocus){
                String nombre = actvAcreedor.getText().toString();
                contact.setName(nombre);
            }
        }
    }

    public void filtrarLista() {
        ContactManager.eliminarRepetidos(listaContactos);
        ContactManager.ordenar(listaContactos);
    }
}

