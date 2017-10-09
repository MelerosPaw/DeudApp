package melerospaw.deudapp.iu.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnFocusChange;
import melerospaw.deudapp.R;
import melerospaw.deudapp.data.ContactManager;
import melerospaw.deudapp.modelo.Contact;
import melerospaw.deudapp.modelo.Persona;
import melerospaw.deudapp.utils.StringUtils;

public class AdaptadorPersonasNuevas extends RecyclerView.Adapter<AdaptadorPersonasNuevas.ViewHolder> {

    private List<Contact> mDatos;
    private List<Contact> listaContactos;
    private Context mContext;
    private AutocompleteAdapter adapter;
    private boolean focus;

    public AdaptadorPersonasNuevas(Context context, List<Contact> mDatos,
                                   List<Contact> personas) {
        this.mContext = context;
        this.mDatos = mDatos;
        this.listaContactos = personas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_nueva_persona, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact contact = mDatos.get(position);
        if (position == mDatos.size() - 1)
            holder.bindView(contact, focus);
        else
            holder.bindView(contact, null);
    }

    @Override
    public int getItemCount() {
        return mDatos.size();
    }

    /**Añade un nuevo hueco para contact a la lista*/
    public void nuevaPersona() {
        Contact contact = new Contact();
        mDatos.add(contact);
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




    /**VIEWHOLDER*/
    class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.actv_persona)    AutoCompleteTextView actvAcreedor;

        private Contact contact;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bindView(Contact contact, Boolean focus){

            this.contact = contact;

            actvAcreedor.setText(contact.getName());
            filtrarLista();
            adapter = new AutocompleteAdapter(mContext, R.layout.item_autocomplete, listaContactos);
            actvAcreedor.setAdapter(adapter);
            actvAcreedor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Contact contactoSeleccionado = adapter.getItem(i);
                    ViewHolder.this.contact.setColor(contactoSeleccionado.getColor());
                    ViewHolder.this.contact.setPhotoUri(contactoSeleccionado.getPhotoUri());
                }
            });

            if (StringUtils.isCadenaVacia(this.contact.getName()) && focus != null && focus) {
                actvAcreedor.setVisibility(View.VISIBLE);
                actvAcreedor.requestFocus();
            }

            actvAcreedor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        cerrarEdicion(false);
                    }
                    return false;
                }
            });
        }

        @OnFocusChange(R.id.actv_persona)
        public void cerrarEdicion(boolean hasFocus) {
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

